package com.mgtriffid.games.panna.shared.lobby

import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.ServerInputProvider
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.panna.shared.game.components.DrawableComponent
import com.mgtriffid.games.panna.shared.game.components.GraverobberNpcComponent
import com.mgtriffid.games.panna.shared.game.components.ORIENTATION_LEFT
import com.mgtriffid.games.panna.shared.game.components.PannaTextureIds
import com.mgtriffid.games.panna.shared.game.components.PositionComponent
import com.mgtriffid.games.panna.shared.game.components.SteamManPlayerComponent
import com.mgtriffid.games.panna.shared.game.components.WalkingComponent
import com.mgtriffid.games.panna.shared.game.components.input.JoinBattleMetaEntityInputComponent
import com.mgtriffid.games.panna.shared.game.components.input.WALKING_DIRECTION_LEFT
import com.mgtriffid.games.panna.shared.game.components.input.WALKING_DIRECTION_RIGHT
import com.mgtriffid.games.panna.shared.game.components.input.WalkingInputComponent
import com.mgtriffid.games.panna.shared.game.systems.JoinBattleEffectConsumer
import com.mgtriffid.games.panna.shared.game.systems.JoinBattleSystem
import com.mgtriffid.games.panna.shared.game.systems.MovementEffectConsumer
import com.mgtriffid.games.panna.shared.game.systems.WalkingInputProcessingSystem

class PannaGame : CottaGame {
    override val serverSystems = listOf(
        WalkingInputProcessingSystem::class,
        MovementEffectConsumer::class,
        JoinBattleSystem::class,
        JoinBattleEffectConsumer::class,
    )

    override fun initializeServerState(state: CottaState) {
        // Adding a graverobber, owner should be system
        val graverobber = state.entities().createEntity()
        graverobber.addComponent(GraverobberNpcComponent.create())
        graverobber.addInputComponent(WalkingInputComponent::class)
        graverobber.addComponent(WalkingComponent.create(30))
        graverobber.addComponent(PositionComponent.create(300, 200, ORIENTATION_LEFT))
        graverobber.addComponent(DrawableComponent.create(PannaTextureIds.TEXTURE_ID_FOO_ENTITY))
    }

    override val componentClasses = setOf(
        PositionComponent::class,
        DrawableComponent::class,
        WalkingComponent::class,
        GraverobberNpcComponent::class, // TODO be able to mark as server-only
        SteamManPlayerComponent::class,
    )

    override val inputComponentClasses = setOf(
        WalkingInputComponent::class,
        JoinBattleMetaEntityInputComponent::class,
    )

    override val metaEntitiesInputComponents = setOf(
        JoinBattleMetaEntityInputComponent::class
    )

    override val serverInputProvider = object: ServerInputProvider {
        var goingLeft = false
        override fun input(entities: Entities): Map<EntityId, Collection<InputComponent<*>>> {
            return entities.all().filter { it.hasComponent(GraverobberNpcComponent::class) }.associate {
                val xPos = it.getComponent(PositionComponent::class).xPos
                if (xPos > 800 && !goingLeft) {
                    goingLeft = true
                }
                if (xPos < 200 && goingLeft) {
                    goingLeft = false
                }
                it.id to listOf(WalkingInputComponent.create(
                    if (goingLeft) WALKING_DIRECTION_LEFT else WALKING_DIRECTION_RIGHT
                ))
            }
        }
    }
}
