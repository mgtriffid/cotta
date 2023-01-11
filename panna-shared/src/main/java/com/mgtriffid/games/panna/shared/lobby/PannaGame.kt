package com.mgtriffid.games.panna.shared.lobby

import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import com.mgtriffid.games.panna.shared.game.MovementDirection
import com.mgtriffid.games.panna.shared.game.components.BossInputComponent
import com.mgtriffid.games.panna.shared.game.components.PositionComponent
import com.mgtriffid.games.panna.shared.game.components.WalkingComponent
import kotlin.reflect.KClass

class PannaGame : CottaGame {
    override val serverSystems = emptyList<KClass<*>>()

    override fun initializeServerState(state: CottaState) {
        val entity = state.entities().createEntity()
        entity.addComponent(PositionComponent(300f, 200f, PositionComponent.Orientation.RIGHT))
        entity.addComponent(WalkingComponent(MovementDirection.IDLE))
    }
}
