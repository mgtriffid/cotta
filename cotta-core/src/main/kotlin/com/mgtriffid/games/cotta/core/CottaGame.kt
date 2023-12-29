package com.mgtriffid.games.cotta.core

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.registry.EffectRegistrationListener
import kotlin.reflect.KClass

// Implement this to configure your actual game
interface CottaGame {
    val serverSystems: List<KClass<*>>
    val nonPlayerInputProvider: NonPlayerInputProvider
    // how to start
    fun initializeServerState(entities: Entities)
    fun initializeStaticState(entities: Entities)
    val componentClasses: Set<KClass<out Component<*>>>
    val inputComponentClasses: Set<KClass<out InputComponent<*>>>
    val effectClasses: Set<KClass<out CottaEffect>>
    val metaEntitiesInputComponents: Set<KClass<out InputComponent<*>>>
    val config: CottaConfig
}
