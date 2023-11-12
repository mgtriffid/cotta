package com.mgtriffid.games.cotta.client

import com.mgtriffid.games.cotta.core.systems.CottaSystem
import kotlin.reflect.KClass

interface PredictionSimulation {
    fun tick()
    fun <T : CottaSystem> registerSystem(systemClass: KClass<T>)
}
