package com.mgtriffid.games.cotta.client

import com.mgtriffid.games.cotta.core.entities.Component
import kotlin.reflect.KClass

interface DrawableStateProvider {
    var lastMyInputProcessedByServerSimulation: Long

    fun get(alpha: Float, components: Array<out KClass<out Component<*>>>): DrawableState
}
