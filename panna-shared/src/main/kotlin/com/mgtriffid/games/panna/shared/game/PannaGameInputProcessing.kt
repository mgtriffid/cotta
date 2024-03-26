package com.mgtriffid.games.panna.shared.game

import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.input.InputProcessing
import com.mgtriffid.games.cotta.core.input.NonPlayerInput
import com.mgtriffid.games.cotta.core.input.PlayerInput
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
    override fun processPlayerInput(
        playerId: PlayerId,
        input: PlayerInput,
        entities: Entities,
        effectBus: EffectBus
    ) {
        input as PannaPlayerInput
        if (input.joinPressed && !dudeExists(playerId, entities)) {
            logger.debug { "Creating dude now" }
            createDude(playerId, entities, effectBus)
        }
        entities.all().filter { entity -> entity.playerId() == playerId }
            .filter { it.hasComponent(SteamManPlayerComponent::class) }
            .forEach { entity ->
                if (entity.hasComponent(LookingAtComponent::class)) {
                    entity.getComponent(LookingAtComponent::class).lookAt = input.lookAt
                }
                if (entity.hasComponent(ShootComponent::class)) {
                    entity.getComponent(ShootComponent::class).isShooting = input.shootPressed
                }
                if (entity.hasComponent(CharacterInputComponent2::class)) {
                    entity.getComponent(CharacterInputComponent2::class).apply {
                        direction = input.walkingDirection
                        jump = input.jumpPressed
                    }
                }
                if (entity.hasComponent(WeaponEquippedComponent::class)) {
                    val weaponEquipped =
                        entity.getComponent(WeaponEquippedComponent::class)
                    if (input.switchWeapon != 0.toByte() && input.switchWeapon != weaponEquipped.equipped) {
                        weaponEquipped.wantToEquip = input.switchWeapon
                    }
                }
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

    private fun Entity.playerId() = (ownedBy as? Entity.OwnedBy.Player)?.playerId
}
