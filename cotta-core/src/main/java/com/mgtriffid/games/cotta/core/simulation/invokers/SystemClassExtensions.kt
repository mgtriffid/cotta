package com.mgtriffid.games.cotta.core.simulation.invokers

import com.mgtriffid.games.cotta.core.systems.CottaSystem
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

fun <T : CottaSystem> KClass<T>.getConstructor() =
    this.primaryConstructor ?: throw IllegalArgumentException(
        "Class ${this.qualifiedName} must have a primary constructor"
    )
