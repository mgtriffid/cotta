package com.mgtriffid.games.panna.shared.lobby

import com.google.gson.Gson
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
import com.mgtriffid.games.panna.shared.game.components.physics.ColliderComponent
import com.mgtriffid.games.panna.shared.game.components.physics.VelocityComponent
import com.mgtriffid.games.panna.shared.game.effects.join.JoinBattleEffect
import com.mgtriffid.games.panna.shared.game.effects.MovementEffect
import com.mgtriffid.games.panna.shared.game.effects.ShootEffect
import com.mgtriffid.games.panna.shared.game.effects.walking.JumpEffect
import com.mgtriffid.games.panna.shared.game.effects.walking.WalkingEffect
import com.mgtriffid.games.panna.shared.game.systems.*
import com.mgtriffid.games.panna.shared.game.systems.join.JoinBattleEffectConsumerSystem
import com.mgtriffid.games.panna.shared.game.systems.join.JoinBattleSystem
import com.mgtriffid.games.panna.shared.game.systems.walking.JumpEffectConsumerSystem
import com.mgtriffid.games.panna.shared.game.systems.walking.WalkingEffectConsumerSystem
import com.mgtriffid.games.panna.shared.game.systems.walking.WalkingInputProcessingSystem
import com.mgtriffid.games.panna.shared.tiled.TiledMap
import java.util.concurrent.atomic.AtomicInteger

class PannaGame : CottaGame {
    override val serverSystems = listOf(
        WalkingInputProcessingSystem::class,
        ShootingInputProcessingSystem::class,
        WalkingEffectConsumerSystem::class,
        JumpEffectConsumerSystem::class,
        MovementSystem::class,
        ShootEffectConsumerSystem::class,
        MovementEffectConsumerSystem::class,
        GravitySystem::class,
        JoinBattleSystem::class,
        JoinBattleEffectConsumerSystem::class,
    )

    override fun initializeServerState(entities: Entities) {
        // Adding a graverobber, owner should be system
//        addGraverobber(entities)
    }

    private fun addGraverobber(entities: Entities) {
        val graverobber = entities.create()
        graverobber.addComponent(GraverobberNpcComponent.create())
        graverobber.addInputComponent(CharacterInputComponent::class)
        graverobber.addComponent(WalkingComponent.create(80))
        graverobber.addComponent(VelocityComponent.create(0, 0))
        graverobber.addComponent(PositionComponent.create(30f, 40f, ORIENTATION_LEFT))
        graverobber.addComponent(DrawableComponent.create(PannaTextureIds.TEXTURE_ID_FOO_ENTITY))
    }

    override fun initializeStaticState(entities: Entities) {
        val ids = AtomicInteger()
        val idGenerator = { ids.incrementAndGet() }

        val tiles = readTerrainFromTiled()
        tiles.forEachIndexed { rowNumber, row ->
            row.forEachIndexed { colNumber, tile ->
                when (tile) {
                    0 -> Unit
                    1 -> createBlock(entities, idGenerator, rowNumber, colNumber)
                    else -> throw RuntimeException("Unknown tile $tile")
                }
            }
        }
    }

    private fun createBlock(entities: Entities, idGenerator: () -> Int, rowNumber: Int, colNumber: Int) {
        val block = entities.createStatic(StaticEntityId(idGenerator()))
        block.addComponent(DrawableComponent.create(PannaTextureIds.Terrain.TEXTURE_ID_BROWN_BLOCK))
        block.addComponent(PositionComponent.create(8 + colNumber * 16f, 8 + rowNumber * 16f, ORIENTATION_LEFT))
        block.addComponent(SolidTerrainComponent.create())
        block.addComponent(ColliderComponent.create(16, 16))
    }

    override val componentClasses = setOf(
        VelocityComponent::class,
        PositionComponent::class,
        DrawableComponent::class,
        WalkingComponent::class,
        GraverobberNpcComponent::class,
        SteamManPlayerComponent::class,
        SolidTerrainComponent::class,
        JumpingComponent::class,
        ColliderComponent::class,
    )

    override val inputComponentClasses = setOf(
        CharacterInputComponent::class,
        ShootInputComponent::class,
        JoinBattleMetaEntityInputComponent::class,
    )

    override val effectClasses = setOf(
        WalkingEffect::class,
        JumpEffect::class,
        JoinBattleEffect::class,
        MovementEffect::class,
        ShootEffect::class,
    )

    override val metaEntitiesInputComponents = setOf(
        JoinBattleMetaEntityInputComponent::class
    )

    override val nonPlayerInputProvider = object : NonPlayerInputProvider {
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
                it.id to listOf(
                    CharacterInputComponent.create(
                        direction = if (goingLeft) WALKING_DIRECTION_LEFT else WALKING_DIRECTION_RIGHT,
                        jump = false
                    )
                )
            }
        }
    }

    override val config: CottaConfig = object : CottaConfig {
        override val tickLength: Long = 100L
    }

    private fun readTerrainFromTiled(): List<List<Int>> {
        val res = javaClass.getResource("/maps/panna-level.tmj")
        val map = Gson().fromJson(res.readText(), TiledMap::class.java)
        return map.layers[0].data.toList().chunked(map.width).reversed()
    }
}
