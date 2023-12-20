package com.mgtriffid.games.cotta.client

import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import kotlin.reflect.KClass

interface PredictionSimulation {
    fun run(ticks: List<Long>, playerId: PlayerId)
    fun <T : CottaSystem> registerSystem(systemClass: KClass<T>)
    fun startPredictionFrom(entities: Entities, tick: Long)

    fun getLocalPredictedEntities(): Collection<Entity>
    fun getPredictedEntities(): List<Entity>
}
