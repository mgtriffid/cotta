package com.mgtriffid.games.panna.shared.lobby

import com.mgtriffid.games.cotta.core.CottaConfig
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.NonPlayerInputProvider
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.entities.id.StaticEntityId
import com.mgtriffid.games.panna.shared.game.components.*
import com.mgtriffid.games.panna.shared.game.components.PositionComponent.Companion.ORIENTATION_LEFT
import com.mgtriffid.games.panna.shared.game.components.input.*
import com.mgtriffid.games.panna.shared.game.effects.JoinBattleEffect
import com.mgtriffid.games.panna.shared.game.effects.MovementEffect
import com.mgtriffid.games.panna.shared.game.effects.ShootEffect
import com.mgtriffid.games.panna.shared.game.systems.*

class PannaGame : CottaGame {
    override val serverSystems = listOf(
        WalkingInputProcessingSystem::class,
        ShootingInputProcessingSystem::class,
        ShootEffectConsumerSystem::class,
        MovementSystem::class,
        MovementEffectConsumerSystem::class,
        JoinBattleSystem::class,
        JoinBattleEffectConsumerSystem::class,
    )

    override fun initializeServerState(entities: Entities) {
        // Adding a graverobber, owner should be system
        val graverobber = entities.create()
        graverobber.addComponent(GraverobberNpcComponent.create())
        graverobber.addInputComponent(WalkingInputComponent::class)
        graverobber.addComponent(WalkingComponent.create(30))
        graverobber.addComponent(PositionComponent.create(300, 200, ORIENTATION_LEFT))
        graverobber.addComponent(DrawableComponent.create(PannaTextureIds.TEXTURE_ID_FOO_ENTITY))
    }

    override fun initializeStaticState(entities: Entities) {
        val terrain = entities.createStatic(StaticEntityId(1))
        terrain.addComponent(DrawableComponent.create(PannaTextureIds.TEXTURE_ID_TERRAIN))
        terrain.addComponent(PositionComponent.create(400, 400, ORIENTATION_LEFT))
    }

    override val componentClasses = setOf(
        VelocityComponent::class,
        PositionComponent::class,
        DrawableComponent::class,
        WalkingComponent::class,
        GraverobberNpcComponent::class,
        SteamManPlayerComponent::class,
    )

    override val inputComponentClasses = setOf(
        WalkingInputComponent::class,
        ShootInputComponent::class,
        JoinBattleMetaEntityInputComponent::class,
    )

    override val effectClasses = setOf(
        JoinBattleEffect::class,
        MovementEffect::class,
        ShootEffect::class,
    )

    override val metaEntitiesInputComponents = setOf(
        JoinBattleMetaEntityInputComponent::class
    )

    override val nonPlayerInputProvider = object: NonPlayerInputProvider {
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

    override val config: CottaConfig = object : CottaConfig {
        override val tickLength: Long = 100L
    }
}
