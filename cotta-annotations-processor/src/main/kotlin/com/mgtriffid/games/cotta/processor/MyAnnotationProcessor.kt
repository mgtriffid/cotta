package com.mgtriffid.games.cotta.processor

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

class MyAnnotationProcessor(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.info("Processing Cotta components...")
        val components = getComponentInterfaces(resolver)
        components.forEach {
            writeComponentImplementation(it)
        }
        return emptyList()
    }

    private fun getComponentInterfaces(resolver: Resolver): Sequence<KSClassDeclaration> {
        return resolver.getSymbolsWithAnnotation(com.mgtriffid.games.cotta.Component::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.superTypes.none { it.resolve().declaration.qualifiedName?.asString() == InputComponent::class.qualifiedName } }
    }

    private fun writeComponentImplementation(component: KSClassDeclaration) {
        val pkg = component.packageName.asString()
        val componentName = component.simpleName.asString()
        val properties: List<ProcessableComponentFieldSpec> = component.getDeclaredProperties().map { prop ->
            ProcessableComponentFieldSpec(prop.simpleName.asString(), prop.type.resolve().toTypeName(), prop.isMutable)
        }.toList()
        if (properties.isNotEmpty()) {
            val fileSpec = FileSpec.builder(pkg, "${componentName}Impl")
                .addFunction(
                    factoryMethod(componentName, component, properties)
                )
                .addType(
                    implementation(componentName, component, properties)
                ).build()
            fileSpec.writeTo(codeGenerator, false)
        } else {
            val fileSpec = FileSpec.builder(pkg, "${componentName}Impl")
                .addFunction(
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
                ).build()
            fileSpec.writeTo(codeGenerator, false)
        }
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
}
