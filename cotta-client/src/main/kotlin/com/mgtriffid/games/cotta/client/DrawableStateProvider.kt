package com.mgtriffid.games.cotta.client

import com.mgtriffid.games.cotta.core.entities.Component
import kotlin.reflect.KClass

interface DrawableStateProvider {
    fun get(alphas: InterpolationAlphas, components: Array<out KClass<out Component>>): DrawableState
}
