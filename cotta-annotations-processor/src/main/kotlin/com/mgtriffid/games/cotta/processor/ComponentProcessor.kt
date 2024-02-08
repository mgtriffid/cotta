package com.mgtriffid.games.cotta.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.entities.MutableComponent
import com.mgtriffid.games.cotta.processor.serialization.SerializerGenerator
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import kotlin.reflect.KClass

const val IMPL_SUFFIX = "Impl"

class ComponentProcessor(
    private val resolver: Resolver,
    private val codeGenerator: CodeGenerator
) {
    fun process(game: KSClassDeclaration) {
        val components = getComponentInterfaces(game)
        components.forEach {
            writeComponentImplementation(it)
        }
        writeSerializers(components)
        writeComponentsClassRegistry(components, game)
    }

    private fun getComponentInterfaces(game: KSClassDeclaration): List<KSClassDeclaration> {
        return resolver.getAllFiles()
            .flatMap { it.declarations }
            .filterIsInstance<KSClassDeclaration>()
            .filter {
                it.superTypes.any { superType ->
                    superType.resolve().declaration.qualifiedName?.asString() in
                        listOf(
                            Component::class.qualifiedName,
                            MutableComponent::class.qualifiedName
                        )
                }
            }
            .filter { it.superTypes.none { superType -> superType.resolve().declaration.qualifiedName?.asString() == InputComponent::class.qualifiedName } }
            .filter { it.packageName.asString().startsWith(game.packageName.asString()) }.toList()
    }

    private fun writeComponentImplementation(component: KSClassDeclaration) {
        val pkg = component.packageName.asString()
        val componentName = component.simpleName.asString()
        val properties: List<ProcessableComponentFieldSpec> = getProcessableComponentFieldSpecs(component)
        val fileSpecBuilder = FileSpec.builder(pkg, "${componentName}$IMPL_SUFFIX")
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
    ) = TypeSpec.classBuilder("${componentName}${IMPL_SUFFIX}")
        .addSuperinterface(component.asStarProjectedType().toTypeName())
        .addModifiers(KModifier.DATA, KModifier.INTERNAL)
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
        if (properties.none { p -> p.isMutable })
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
        .addStatement("return ${componentName}$IMPL_SUFFIX(${properties.joinToString(", ") { it.name }})")
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

    private fun writeSerializers(components: List<KSClassDeclaration>) {
        SerializerGenerator(resolver, codeGenerator).generate(components)
    }
}
