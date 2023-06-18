package com.mgtriffid.games.panna.shared.lobby

import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.panna.shared.game.MovementDirection
import com.mgtriffid.games.panna.shared.game.components.ColliderComponent
import com.mgtriffid.games.panna.shared.game.components.DrawableComponent
import com.mgtriffid.games.panna.shared.game.components.HealthComponent
import com.mgtriffid.games.panna.shared.game.components.ORIENTATION_LEFT
import com.mgtriffid.games.panna.shared.game.components.PannaTextureIds
import com.mgtriffid.games.panna.shared.game.components.PositionComponent
import com.mgtriffid.games.panna.shared.game.components.TerrainComponent
import com.mgtriffid.games.panna.shared.game.components.WalkingComponent
import kotlin.reflect.KClass

class PannaGame : CottaGame {
    override val serverSystems = emptyList<KClass<*>>()

    override fun initializeServerState(state: CottaState) {
        // Adding a graverobber, owner should be system
        val entity = state.entities().createEntity()
        entity.addComponent(PositionComponent.create(300, 200, ORIENTATION_LEFT))
        entity.addComponent(DrawableComponent.create(PannaTextureIds.TEXTURE_ID_FOO_ENTITY))
//        entity.addComponent(WalkingComponent(MovementDirection.IDLE))
    }

    override val componentClasses = listOf(
        PositionComponent::class,
        ColliderComponent::class,
        TerrainComponent::class,
//        WalkingComponent::class,
        HealthComponent::class,
        DrawableComponent::class,
    )
}
