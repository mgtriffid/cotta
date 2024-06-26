package com.mgtriffid.games.cotta.core.registry

import com.mgtriffid.games.cotta.core.entities.Component
import kotlin.reflect.KClass

interface ComponentRegistrationListener {
    fun <T: Component<T>> onComponentRegistration(kClass: KClass<T>, descriptor: ComponentSpec)
}
