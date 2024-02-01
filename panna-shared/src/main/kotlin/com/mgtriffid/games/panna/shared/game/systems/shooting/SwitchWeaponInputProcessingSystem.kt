package com.mgtriffid.games.panna.shared.game.systems.shooting

import com.mgtriffid.games.cotta.core.annotations.Predicted
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.InputProcessingContext
import com.mgtriffid.games.cotta.core.systems.InputProcessingSystem
import com.mgtriffid.games.panna.shared.game.components.WeaponEquippedComponent
import com.mgtriffid.games.panna.shared.game.components.input.CharacterInputComponent

@Predicted
class SwitchWeaponInputProcessingSystem : InputProcessingSystem {
    override fun process(e: Entity, ctx: InputProcessingContext) {
        if (e.hasComponent(WeaponEquippedComponent::class) && e.hasInputComponent(CharacterInputComponent::class)) {
            val input = e.getInputComponent(CharacterInputComponent::class)
            val weaponEquipped = e.getComponent(WeaponEquippedComponent::class)
            if (input.switchWeapon != 0.toByte() && input.switchWeapon != weaponEquipped.equipped) {
                weaponEquipped.wantToEquip = input.switchWeapon
            }
        }
    }
}
