package com.mgtriffid.games.panna.shared.game

import com.google.gson.Gson
import com.mgtriffid.games.cotta.Game
import com.mgtriffid.games.cotta.core.config.CottaConfig
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.config.DebugConfig
import com.mgtriffid.games.cotta.core.config.DebugConfig.EmulatedNetworkConditions.WithIssues.Issues
import com.mgtriffid.games.cotta.core.config.DebugConfig.EmulatedNetworkConditions.WithIssues.Latency
import com.mgtriffid.games.cotta.core.input.NonPlayerInputProvider
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.id.StaticEntityId
import com.mgtriffid.games.cotta.core.input.NonPlayerInput
import com.mgtriffid.games.panna.shared.PannaPlayerInput
import com.mgtriffid.games.panna.shared.SOLID_TERRAIN_TILE_STRATEGY
import com.mgtriffid.games.panna.shared.game.components.*
import com.mgtriffid.games.panna.shared.game.components.physics.createColliderComponent
import com.mgtriffid.games.panna.shared.game.systems.*
import com.mgtriffid.games.panna.shared.game.systems.join.JoinBattleEffectConsumerSystem
import com.mgtriffid.games.panna.shared.game.systems.shooting.BulletCollisionSystem
import com.mgtriffid.games.panna.shared.game.systems.shooting.BulletHitsDudeEffectConsumer
import com.mgtriffid.games.panna.shared.game.systems.shooting.RailgunHitsDudeEffectConsumer
import com.mgtriffid.games.panna.shared.game.systems.shooting.RailgunShotEffectConsumerSystem
import com.mgtriffid.games.panna.shared.game.systems.shooting.ShootBulletEffectConsumer
import com.mgtriffid.games.panna.shared.game.systems.shooting.ShootingProcessingSystem
import com.mgtriffid.games.panna.shared.game.systems.shooting.SwitchWeaponSystem
import com.mgtriffid.games.panna.shared.game.systems.walking.*
import com.mgtriffid.games.panna.shared.tiled.TiledMap
import java.util.concurrent.atomic.AtomicInteger

@Game
class PannaGame : CottaGame {
    override val serverSystems = listOf(
        WalkingInputProcessingSystem::class,
        SwitchWeaponSystem::class,
        ShootingProcessingSystem::class,
        WalkingEffectConsumerSystem::class,
        JumpEffectConsumerSystem::class,
        MovementSystem::class,
        ShootEffectConsumerSystem::class,
        ShootBulletEffectConsumer::class,
        MovementEffectConsumerSystem::class,
        CoyoteSystem::class,
        GravitySystem::class,
        JoinBattleEffectConsumerSystem::class,
        BulletCollisionSystem::class,
        BulletHitsDudeEffectConsumer::class,
        RailgunShotEffectConsumerSystem::class,
        RailgunHitsDudeEffectConsumer::class,
        DeathSystem::class,
    )

    override fun initializeServerState(entities: Entities) {

    }

    override fun initializeStaticState(entities: Entities) {
        val ids = AtomicInteger()
        val idGenerator = { ids.incrementAndGet() }

        val tiles = readTerrainFromTiled()
        tiles.forEachIndexed { rowNumber, row ->
            row.forEachIndexed { colNumber, tile ->
                when (tile) {
                    0 -> Unit
                    1 -> createBlock(
                        entities, idGenerator, rowNumber, colNumber
                    )

                    else -> throw RuntimeException("Unknown tile $tile")
                }
            }
        }
    }

    private fun createBlock(
        entities: Entities,
        idGenerator: () -> Int,
        rowNumber: Int,
        colNumber: Int
    ) {
        val block = entities.createStatic(StaticEntityId(idGenerator()))
        block.addComponent(createDrawableComponent(SOLID_TERRAIN_TILE_STRATEGY))
        block.addComponent(
            createPositionComponent(
                8 + colNumber * 16f, 8 + rowNumber * 16f
            )
        )
        block.addComponent(createSolidTerrainComponent())
        block.addComponent(createColliderComponent(16, 16))
    }

    override val nonPlayerInputProvider = object : NonPlayerInputProvider {
        override fun input(entities: Entities) = object : NonPlayerInput {}
    }

    override val config: CottaConfig = object : CottaConfig {
        override val tickLength: Long = 100L
        override val debugConfig = NetworkWithIssues
    }

    override val playerInputKClass = PannaPlayerInput::class

    private fun readTerrainFromTiled(): List<List<Int>> {
        val res = javaClass.getResource("/maps/panna-level.tmj")
        val map = Gson().fromJson(res.readText(), TiledMap::class.java)
        return map.layers[0].data.toList().chunked(map.width).reversed()
    }

    override val inputProcessing = PannaGameInputProcessing()
}

object NetworkWithIssues : DebugConfig {
    override val emulatedNetworkConditions =
        object : DebugConfig.EmulatedNetworkConditions.WithIssues {
            override val sending = object : Issues {
                override val latency = object : Latency {
                    override val min: Long = 20
                    override val max: Long = 80
                }
                override val packetLoss: Double = 0.0
            }
            override val receiving = object : Issues {
                override val latency = object : Latency {
                    override val min: Long = 20
                    override val max: Long = 80
                }
                override val packetLoss: Double = 0.0
            }
        }
}
