package com.mgtriffid.games.cotta.server.workload

import com.mgtriffid.games.cotta.Game
import com.mgtriffid.games.cotta.core.config.CottaConfig
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.NonPlayerInputProvider
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import kotlin.reflect.KClass

@Game
class GameStub : CottaGame {
    override val serverSystems: List<KClass<*>> = emptyList()
    override val nonPlayerInputProvider = object : NonPlayerInputProvider {
        override fun input(entities: Entities) = emptyMap<EntityId, Collection<InputComponent<*>>>()
    }

    override fun initializeServerState(entities: Entities) {}
    override fun initializeStaticState(entities: Entities) {}

    override val metaEntitiesInputComponents: Set<KClass<out InputComponent<*>>> = emptySet()
    override val config: CottaConfig = object : CottaConfig {
        override val tickLength: Long = 20
    }
}
