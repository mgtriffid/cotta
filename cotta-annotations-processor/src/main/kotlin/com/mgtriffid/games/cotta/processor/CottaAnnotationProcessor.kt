package com.mgtriffid.games.cotta.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.mgtriffid.games.cotta.Game

class CottaAnnotationProcessor(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val game = getGame(resolver) ?: return emptyList()
        ComponentProcessor(resolver, codeGenerator, logger).process(game)
        InputComponentProcessor(resolver, codeGenerator, logger).process(game)
        EffectProcessor(resolver, codeGenerator, logger).process(game)
        return emptyList()
    }

    private fun getGame(resolver: Resolver): KSClassDeclaration? {
        val games = resolver.getSymbolsWithAnnotation(Game::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .toList()
        validate(games)
        return games.firstOrNull()
    }

    private fun validate(games: List<KSClassDeclaration>) {
        if (games.size > 1) {
            logger.error("Found more than one game class annotated with @${Game::class.simpleName}")
            throw IllegalStateException("There should be exactly one class annotated with @${Game::class.simpleName}")
        }
    }
}
