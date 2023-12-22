package com.mgtriffid.games.cotta.core.registry

import com.mgtriffid.games.cotta.core.entities.InputComponent
import kotlin.reflect.KClass

interface InputComponentRegistrationListener {
    fun <T: InputComponent<T>> onInputComponentRegistration(kClass: KClass<T>, descriptor: ComponentSpec)
}
