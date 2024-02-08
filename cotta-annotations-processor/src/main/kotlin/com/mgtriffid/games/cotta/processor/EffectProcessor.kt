package com.mgtriffid.games.cotta.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.mgtriffid.games.cotta.core.effects.CottaEffect

class EffectProcessor(
    private val resolver: Resolver,
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) {
    fun process(game: KSClassDeclaration) {
        val effects = resolver.getAllFiles().flatMap { it.declarations }
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.superTypes.any { superType -> superType.resolve().declaration.qualifiedName?.asString() == CottaEffect::class.qualifiedName } }
            .filter { it.packageName.asString().startsWith(game.packageName.asString()) }
            .toList()
        effects.forEach { effect ->
            val effectName = effect.simpleName.asString()
            val effectPackage = effect.packageName.asString()
            logger.warn("Processing effect $effectName in package $effectPackage")
        }
    }
}
