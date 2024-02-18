package com.mgtriffid.games.cotta.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.mgtriffid.games.cotta.core.codegen.Constants.EFFECTS_CLASS_SUFFIX
import com.mgtriffid.games.cotta.core.codegen.Constants.FACTORY_METHOD_PREFIX
import com.mgtriffid.games.cotta.core.codegen.Constants.GET_EFFECTS_METHOD
import com.mgtriffid.games.cotta.core.codegen.Constants.IMPL_SUFFIX
import com.mgtriffid.games.cotta.core.effects.CottaEffect
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

class EffectProcessor(
    private val resolver: Resolver,
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) {
    fun process(game: KSClassDeclaration) {
        val effects = getEffectInterfaces(game)
        effects.forEach { effect ->
            writeEffectImplementation(effect)
        }
        writeEffectsClassRegistry(effects, game)
    }

    private fun writeEffectImplementation(effect: KSClassDeclaration) {
        val pkg = effect.packageName.asString()
        val effectName = effect.simpleName.asString()

        val properties: List<ProcessableEffectFieldSpec> = getProcessableEffectFieldSpecs(effect)
        val fileSpecBuilder = FileSpec.builder(pkg, "$effectName$IMPL_SUFFIX")
        if (properties.isNotEmpty()) {
            buildDataClassImplementation(fileSpecBuilder, effectName, effect, properties)
        } else {
            buildSingletonImplementation(fileSpecBuilder, effectName, effect)
        }
        logger.warn("Processing effect $effectName in package $pkg")
        fileSpecBuilder.build().writeTo(codeGenerator, false)
    }

    private fun buildDataClassImplementation(
        fileSpecBuilder: FileSpec.Builder,
        effectName: String,
        effect: KSClassDeclaration,
        properties: List<ProcessableEffectFieldSpec>
    ) {
        fileSpecBuilder
            .addFunction(factoryMethod(effectName, effect, properties))
            .addType(implementation(effectName, effect, properties))
    }

    private fun buildSingletonImplementation(
        fileSpecBuilder: FileSpec.Builder,
        effectName: String,
        effect: KSClassDeclaration
    ) {
        val name = "$effectName$IMPL_SUFFIX"
        fileSpecBuilder.addFunction(
            FunSpec.builder("$FACTORY_METHOD_PREFIX$effectName").addModifiers(KModifier.PUBLIC)
                .returns(effect.asStarProjectedType().toTypeName())
                .addStatement("return $name")
                .build()
        )
            .addType(
                TypeSpec.objectBuilder(name)
                    .addSuperinterface(effect.asStarProjectedType().toTypeName())
                    .build()
            )
    }

    private fun implementation(
        effectName: String,
        effect: KSClassDeclaration,
        properties: List<ProcessableEffectFieldSpec>
    ) = TypeSpec.classBuilder("$effectName$IMPL_SUFFIX")
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

    private fun implPrimaryConstructor(properties: List<ProcessableEffectFieldSpec>) =
        FunSpec.constructorBuilder()
            .addParameters(
                properties.map { prop ->
                    ParameterSpec.builder(prop.name, prop.type)
                        .build()
                }
            )
            .build()

    private fun factoryMethod(
        effectName: String,
        effect: KSClassDeclaration,
        properties: List<ProcessableEffectFieldSpec>
    ) = FunSpec.builder("$FACTORY_METHOD_PREFIX$effectName").addModifiers(KModifier.PUBLIC)
        .returns(effect.asStarProjectedType().toTypeName())
        .addParameters(
            properties.map { spec ->
                ParameterSpec.builder(spec.name, spec.type).build()
            }
        )
        .addStatement("return $effectName$IMPL_SUFFIX(${properties.joinToString(", ") { it.name }})")
        .build()


    private fun getEffectInterfaces(game: KSClassDeclaration) =
        resolver.getAllFiles().flatMap { it.declarations }
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.superTypes.any { superType -> superType.resolve().declaration.qualifiedName?.asString() == CottaEffect::class.qualifiedName } }
            .filter { it.packageName.asString().startsWith(game.packageName.asString()) }
            .toList()

    private fun writeEffectsClassRegistry(effects: List<KSClassDeclaration>, game: KSClassDeclaration) {
        if (effects.isEmpty()) return
        val pkg = game.packageName.asString()
        val gameName = game.simpleName.asString()
        val registryClassName = "$gameName$EFFECTS_CLASS_SUFFIX"
        val fileSpecBuilder = FileSpec.builder(pkg, registryClassName)
        fileSpecBuilder.addType(
            TypeSpec.classBuilder(registryClassName)
                .addFunction(
                    FunSpec.builder(GET_EFFECTS_METHOD)
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
                            effects.joinToString(", ") { "${it.qualifiedName!!.asString()}::class" } +
                            ")")
                        .build()
                ).build()
        )
        fileSpecBuilder.build().writeTo(codeGenerator, false)    }
}
