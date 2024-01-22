package com.mgtriffid.games.cotta.client

import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import kotlin.reflect.KClass

interface PredictionSimulation {
    fun <T : CottaSystem> registerSystem(systemClass: KClass<T>)
    fun predict(
        initialEntities: Entities,
        ticks: List<Long>,
    )

    fun getLocalPredictedEntities(): Collection<Entity>
    fun getPreviousLocalPredictedEntities(): List<Entity>
}
