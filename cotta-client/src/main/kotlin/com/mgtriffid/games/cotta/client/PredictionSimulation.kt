package com.mgtriffid.games.cotta.client

import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.input.ClientInputId
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import kotlin.reflect.KClass

interface PredictionSimulation {
    fun <T : CottaSystem> registerSystem(systemClass: KClass<T>)
    fun predict(
        initialEntities: Entities,
        lastConfirmedInputId: ClientInputId,
        authoritativeTick: Long,
    )

    fun getLocalPredictedEntities(): Collection<Entity>
    fun getPreviousLocalPredictedEntities(): List<Entity>
    val effectBus: EffectBus
}
