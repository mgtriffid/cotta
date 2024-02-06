package com.mgtriffid.games.cotta.processor

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.mgtriffid.games.cotta.Game
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import kotlin.reflect.KClass

class CottaAnnotationProcessor(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.info("Processing Cotta components...")
        val components = getComponentInterfaces(resolver).toList()
        val game = getGame(resolver) ?: return emptyList()
        components.forEach {
            writeComponentImplementation(it)
        }
        writeComponentsClassRegistry(components, game)
        return emptyList()
    }

    private fun getComponentInterfaces(resolver: Resolver): Sequence<KSClassDeclaration> {
        return resolver.getSymbolsWithAnnotation(com.mgtriffid.games.cotta.Component::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.superTypes.none { it.resolve().declaration.qualifiedName?.asString() == InputComponent::class.qualifiedName } }
    }

    private fun getGame(resolver: Resolver): KSClassDeclaration? {
        val games = resolver.getSymbolsWithAnnotation(com.mgtriffid.games.cotta.Game::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .toList()
        if (games.size > 1) {
            logger.error("Found more than one game class annotated with @${com.mgtriffid.games.cotta.Game::class.simpleName}")
            throw IllegalStateException("There should be exactly one class annotated with @Game")
        }
        return games.firstOrNull()
    }

    private fun writeComponentImplementation(component: KSClassDeclaration) {
        val pkg = component.packageName.asString()
        val componentName = component.simpleName.asString()
        val properties: List<ProcessableComponentFieldSpec> = component.getDeclaredProperties().map { prop ->
            ProcessableComponentFieldSpec(prop.simpleName.asString(), prop.type.resolve().toTypeName(), prop.isMutable)
        }.toList()
        val fileSpecBuilder = FileSpec.builder(pkg, "${componentName}Impl")
        if (properties.isNotEmpty()) {
            buildDataClassImplementation(fileSpecBuilder, componentName, component, properties)
        } else {
            buildSingletonImplementation(fileSpecBuilder, componentName, component)
        }
        fileSpecBuilder.build().writeTo(codeGenerator, false)
    }

    private fun buildSingletonImplementation(
        fileSpecBuilder: FileSpec.Builder,
        componentName: String,
        component: KSClassDeclaration
    ) {
        fileSpecBuilder.addFunction(
            FunSpec.builder("create${componentName}").addModifiers(KModifier.PUBLIC)
                .returns(component.asStarProjectedType().toTypeName())
                .addStatement("return ${componentName}Instance")
                .build()
        )
            .addType(
                TypeSpec.objectBuilder("${componentName}Instance")
                    .addSuperinterface(component.asStarProjectedType().toTypeName())
                    .addFunction(
                        FunSpec.builder("copy").addStatement("return this")
                            .addModifiers(KModifier.OVERRIDE)
                            .returns(component.asStarProjectedType().toTypeName())
                            .build()
                    )
                    .build()
            )
    }

    private fun buildDataClassImplementation(
        fileSpecBuilder: FileSpec.Builder,
        componentName: String,
        component: KSClassDeclaration,
        properties: List<ProcessableComponentFieldSpec>
    ) {
        fileSpecBuilder.addFunction(
            factoryMethod(componentName, component, properties)
        )
            .addType(
                implementation(componentName, component, properties)
            )
    }

    private fun implementation(
        componentName: String,
        component: KSClassDeclaration,
        properties: List<ProcessableComponentFieldSpec>
    ) = TypeSpec.classBuilder("${componentName}Impl")
        .addSuperinterface(component.asStarProjectedType().toTypeName())
        .addModifiers(KModifier.DATA, KModifier.PRIVATE)
        .primaryConstructor(
            implPrimaryConstructor(properties)
        )
        .addFunction(
            copy(properties, component)
        )
        .addProperties(
            properties.map { prop ->
                PropertySpec.builder(prop.name, prop.type)
                    .initializer(prop.name)
                    .addModifiers(KModifier.OVERRIDE)
                    .also { if (prop.isMutable) it.mutable() }
                    .build()
            }
        )
        .build()

    private fun copy(
        properties: List<ProcessableComponentFieldSpec>,
        component: KSClassDeclaration
    ) = FunSpec.builder("copy").addStatement(
        if (properties.filter { p -> p.isMutable }.isEmpty())
            "return this"
        else
            "return this.copy(${
                properties.filter { p -> p.isMutable }
                    .joinToString(", ") { "${it.name} = ${it.name}" }
            })"
    )
        .addModifiers(KModifier.OVERRIDE)
        .returns(component.asStarProjectedType().toTypeName())
        .build()

    private fun implPrimaryConstructor(properties: List<ProcessableComponentFieldSpec>) =
        FunSpec.constructorBuilder()
            .addParameters(
                properties.map { prop ->
                    ParameterSpec.builder(prop.name, prop.type)
                        .build()
                }
            )
            .build()

    private fun factoryMethod(
        componentName: String,
        component: KSClassDeclaration,
        properties: List<ProcessableComponentFieldSpec>
    ) = FunSpec.builder("create${componentName}").addModifiers(KModifier.PUBLIC)
        .returns(component.asStarProjectedType().toTypeName())
        .addParameters(
            properties.map { spec ->
                ParameterSpec.builder(spec.name, spec.type).build()
            }
        )
        .addStatement("return ${componentName}Impl(${properties.joinToString(", ") { it.name }})")
        .build()

    private fun writeComponentsClassRegistry(
        components: List<KSClassDeclaration>,
        game: KSClassDeclaration
    ) {
        if (components.isEmpty()) return
        val pkg = game.packageName.asString()
        val gameName = game.simpleName.asString()
        val fileSpecBuilder = FileSpec.builder(pkg, "${gameName}Components")
        fileSpecBuilder.addType(
            TypeSpec.classBuilder("${gameName}Components")
                .addFunction(
                    FunSpec.builder("getComponents")
                        .returns(
                            List::class.asTypeName().parameterizedBy(
                                ClassName(
                                    KClass::class.java.`package`.name,
                                    KClass::class.simpleName!!
                                ).parameterizedBy(STAR)
                            )
                        )
                        .addModifiers(KModifier.PUBLIC)
                        .addStatement("return listOf(" +
                            components.joinToString(", ") { "${it.qualifiedName!!.asString()}::class" } +
                            ")")
                        .build()
                ).build()
        )
        fileSpecBuilder.build().writeTo(codeGenerator, false)
    }
}
