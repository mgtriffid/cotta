package com.mgtriffid.games.cotta.client

import com.codahale.metrics.MetricRegistry
import com.mgtriffid.games.cotta.client.impl.LocalPlayer
import com.mgtriffid.games.cotta.core.entities.Component
import kotlin.reflect.KClass

interface CottaClient {
    fun initialize()

    fun update(): UpdateResult
    fun getDrawableState(alpha: Float, vararg components: KClass<out Component<*>>): DrawableState

    // TODO better place or better beans. This is here now only for drawing. Incorrect.
    val localPlayer: LocalPlayer // TODO read-only view
    val debugMetrics: MetricRegistry
}

sealed interface UpdateResult {
    data object AwaitingGameState : UpdateResult
    data object Disconnected : UpdateResult
    data class Running(val alpha: Float) : UpdateResult
}
