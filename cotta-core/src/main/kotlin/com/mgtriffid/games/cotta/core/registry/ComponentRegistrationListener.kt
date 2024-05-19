package com.mgtriffid.games.cotta.core.registry

import com.mgtriffid.games.cotta.core.entities.Component
import kotlin.reflect.KClass

// TODO use or remove
interface ComponentRegistrationListener {
    fun <T: Component> onComponentRegistration(kClass: KClass<T>, descriptor: ComponentSpec)
}
