package com.mgtriffid.games.cotta.server.workload

import com.mgtriffid.games.cotta.Game
import com.mgtriffid.games.cotta.core.config.CottaConfig
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.PlayersHandler
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.input.NonPlayerInputProvider
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.input.InputProcessing
import com.mgtriffid.games.cotta.core.input.NonPlayerInput
import com.mgtriffid.games.cotta.core.input.PlayerInput
import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import com.mgtriffid.games.cotta.server.workload.components.PlayerControlledStubComponent

@Game
class GameStub : CottaGame {
    override val systems: List<CottaSystem> = emptyList()
    override val nonPlayerInputProvider = object : NonPlayerInputProvider {
        override fun input(entities: Entities) = object : NonPlayerInput {}
    }
    override val inputProcessing = InputProcessingStub()
    override fun initializeServerState(entities: Entities) {}
    override fun initializeStaticState(entities: Entities) {}

    override val config: CottaConfig = object : CottaConfig {
        override val tickLength: Long = 20
    }
    override val playerInputKClass = PlayerInputStub::class

    override val playersHandler = object : PlayersHandler {
        override fun onLeaveGame(playerId: PlayerId, entities: Entities) {
        }
    }
}

data class PlayerInputStub(
    val aim: Int,
    val shoot: Boolean,
) : PlayerInput

class InputProcessingStub : InputProcessing {
    override fun process(
        input: SimulationInput,
        entities: Entities,
        effectBus: EffectBus
    ) {
        input.inputForPlayers().forEach { (playerId, playerInput) ->
            processPlayerInput(playerId, playerInput, entities, effectBus)
        }
    }

    override fun processPlayerInput(
        playerId: PlayerId,
        input: PlayerInput,
        entities: Entities,
        effectBus: EffectBus
    ) {
        input as PlayerInputStub
        val player = entities.all()
            .filter { it.hasComponent(PlayerControlledStubComponent::class) }
            .first { it.ownedBy == Entity.OwnedBy.Player(playerId) }
        player.getComponent(PlayerControlledStubComponent::class).apply {
            aim = input.aim
            shoot = input.shoot
        }
    }
}
