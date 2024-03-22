package com.mgtriffid.games.panna.shared.game

import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.input.InputProcessing
import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import com.mgtriffid.games.panna.shared.PannaPlayerInput
import com.mgtriffid.games.panna.shared.game.components.CharacterInputComponent2
import com.mgtriffid.games.panna.shared.game.components.LookingAtComponent
import com.mgtriffid.games.panna.shared.game.components.ShootComponent
import com.mgtriffid.games.panna.shared.game.components.SteamManPlayerComponent
import com.mgtriffid.games.panna.shared.game.components.WeaponEquippedComponent
import com.mgtriffid.games.panna.shared.game.effects.join.createJoinBattleEffect
import mu.KotlinLogging
import kotlin.math.log

private val logger = KotlinLogging.logger {}

class PannaGameInputProcessing : InputProcessing {
    override fun process(
        input: SimulationInput,
        entities: Entities,
        effectBus: EffectBus
    ) {
        input.inputForPlayers().forEach { (playerId, playerInput) ->
            playerInput as PannaPlayerInput
            if (playerInput.joinPressed && !dudeExists(playerId, entities)) {
                logger.debug { "Creating dude now" }
                createDude(playerId, entities, effectBus)
            }
        }
        entities.all()
            .filter { it.hasComponent(SteamManPlayerComponent::class) }
            .forEach { entity ->
                if (entity.hasComponent(LookingAtComponent::class)) {
                    val playerInput = input.inputForPlayers()[entity.playerId()] as PannaPlayerInput
                    entity.getComponent(LookingAtComponent::class).lookAt = playerInput.lookAt
                }
                if (entity.hasComponent(ShootComponent::class)) {
                    val playerInput = input.inputForPlayers()[entity.playerId()] as PannaPlayerInput
                    entity.getComponent(ShootComponent::class).isShooting = playerInput.shootPressed
                }
                if (entity.hasComponent(CharacterInputComponent2::class)) {
                    val playerInput = input.inputForPlayers()[entity.playerId()] as PannaPlayerInput
                    entity.getComponent(CharacterInputComponent2::class).apply {
                        direction = playerInput.walkingDirection
                        jump = playerInput.jumpPressed
                    }
                }
                if (entity.hasComponent(WeaponEquippedComponent::class)) {
                    val playerInput =
                        input.inputForPlayers()[entity.playerId()] as PannaPlayerInput
                    val weaponEquipped =
                        entity.getComponent(WeaponEquippedComponent::class)
                    if (playerInput.switchWeapon != 0.toByte() && playerInput.switchWeapon != weaponEquipped.equipped) {
                        weaponEquipped.wantToEquip = playerInput.switchWeapon
                    }
                }
            }
        input.inputForPlayers().forEach { (playerId, playerInput) ->
            logger.info { "Processing input for player $playerId: $playerInput"}
        }
    }

    private fun dudeExists(playerId: PlayerId, entities: Entities) =
        entities.all().any {
            it.hasComponent(SteamManPlayerComponent::class) &&
                (it.ownedBy as? Entity.OwnedBy.Player)?.playerId == playerId
        }

    private fun createDude(playerId: PlayerId, entities: Entities, effectBus: EffectBus) {
        effectBus.publisher().fire(createJoinBattleEffect(playerId))
    }

    private fun Entity.playerId() = (ownedBy as Entity.OwnedBy.Player).playerId
}
