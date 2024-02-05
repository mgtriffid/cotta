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
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

class MyAnnotationProcessor(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.warn("Hehe hello processing!")

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
        val properties: List<Triple<String, TypeName, Boolean>> = component.getDeclaredProperties().map { prop ->
            Triple(prop.simpleName.asString(), prop.type.resolve().toTypeName(), prop.isMutable)
        }.toList()
        if (properties.isNotEmpty()) {
        val fileSpec = FileSpec.builder(pkg, "${componentName}Impl")
            .addFunction(FunSpec.builder("create${componentName}").addModifiers(KModifier.PUBLIC)
                .returns(component.asStarProjectedType().toTypeName())
                .addParameters(
                    properties.map { prop ->
                        ParameterSpec.builder(prop.first, prop.second).build()
                    }
                )
                .addStatement("return ${componentName}Impl(${properties.joinToString(", ") { it.first }})")
                .build()
            )
            .addType(
                TypeSpec.classBuilder("${componentName}Impl")
                    .addSuperinterface(component.asStarProjectedType().toTypeName())
                     .addModifiers(KModifier.DATA, KModifier.PRIVATE)
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameters(
                                properties.map { prop ->
                                    ParameterSpec.builder(prop.first, prop.second)
                                        .build()
                                }
                            )
                            .build()
                    )
                    .addFunction(
                        FunSpec.builder("copy").addStatement(
                            if (properties.filter { p -> p.third }.isEmpty())
                                "return this"
                            else
                                "return this.copy(${properties.filter { p -> p.third }.joinToString(", ") { "${it.first} = ${it.first}" }})"
                        )
                            .addModifiers(KModifier.OVERRIDE)
                            .returns(component.asStarProjectedType().toTypeName())
                            .build()
                    )
                    .addProperties(
                        properties.map { prop ->
                            PropertySpec.builder(prop.first, prop.second)
                                .initializer(prop.first)
                                .addModifiers(KModifier.OVERRIDE)
                                .also { if (prop.third) it.mutable() }
                                .build()
                        }
                    )
                    .build()
            ).build()
            fileSpec.writeTo(codeGenerator, false)
        } else {
            val fileSpec = FileSpec.builder(pkg, "${componentName}Impl")
                .addFunction(FunSpec.builder("create${componentName}").addModifiers(KModifier.PUBLIC)
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
}
