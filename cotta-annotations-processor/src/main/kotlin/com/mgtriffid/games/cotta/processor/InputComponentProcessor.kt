package com.mgtriffid.games.cotta.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
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

class InputComponentProcessor(
    private val resolver: Resolver,
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) {
    fun process(game: KSClassDeclaration) {
        val components = getInputComponentInterfaces(game)
        components.forEach { component ->
            writeComponentImplementation(component)
        }
    }

    private fun getInputComponentInterfaces(game: KSClassDeclaration): List<KSClassDeclaration> {
        return resolver.getAllFiles()
            .flatMap { it.declarations }
            .filterIsInstance<KSClassDeclaration>()
            .filter {
                it.superTypes.any { superType ->
                    superType.resolve().declaration.qualifiedName?.asString() == InputComponent::class.qualifiedName
                }
            }
            .filter { it.packageName.asString().startsWith(game.packageName.asString()) }
            .toList()
    }

    private fun writeComponentImplementation(component: KSClassDeclaration) {
        val pkg = component.packageName.asString()
        val componentName = component.simpleName.asString()
        val properties: List<ProcessableInputComponentFieldSpec> = getProcessableInputComponentFieldSpecs(component)
        val fileSpecBuilder = FileSpec.builder(pkg, "${componentName}Impl")
        buildDataClassImplementation(fileSpecBuilder, componentName, component, properties)
        fileSpecBuilder.build().writeTo(codeGenerator, false)
    }

    private fun buildDataClassImplementation(
        fileSpecBuilder: FileSpec.Builder,
        componentName: String,
        component: KSClassDeclaration,
        properties: List<ProcessableInputComponentFieldSpec>
    ) {
        fileSpecBuilder
            .addFunction(factoryMethod(componentName, component, properties))
            .addType(implementation(componentName, component, properties))
    }

    private fun factoryMethod(
        componentName: String,
        component: KSClassDeclaration,
        properties: List<ProcessableInputComponentFieldSpec>
    ) = FunSpec.builder("create${componentName}").addModifiers(KModifier.PUBLIC)
        .returns(component.asStarProjectedType().toTypeName())
        .addParameters(
            properties.map { spec ->
                ParameterSpec.builder(spec.name, spec.type).build()
            }
        )
        .addStatement("return ${componentName}$IMPL_SUFFIX(${properties.joinToString(", ") { it.name }})")
        .build()

    private fun implementation(
        effectName: String,
        effect: KSClassDeclaration,
        properties: List<ProcessableInputComponentFieldSpec>
    ) = TypeSpec.classBuilder("${effectName}${IMPL_SUFFIX}")
        .addSuperinterface(effect.asStarProjectedType().toTypeName())
        .addModifiers(KModifier.DATA, KModifier.INTERNAL)
        .primaryConstructor(
            implPrimaryConstructor(properties)
        )
        .addProperties(
            properties.map { prop ->
                PropertySpec.builder(prop.name, prop.type)
                    .initializer(prop.name)
                    .addModifiers(KModifier.OVERRIDE)
                    .build()
            }
        )
        .build()

    private fun implPrimaryConstructor(
        properties: List<ProcessableInputComponentFieldSpec>
    ) = FunSpec.constructorBuilder()
        .addParameters(
            properties.map { prop ->
                ParameterSpec.builder(prop.name, prop.type)
                    .build()
            }
        )
        .build()
}
