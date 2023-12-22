package com.mgtriffid.games.cotta.core.registry

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import kotlin.reflect.KClass

interface EffectRegistrationListener {
    fun onEffectRegistration(effectClass: KClass<out CottaEffect>, descriptor: EffectSpec)
}