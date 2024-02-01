package com.mgtriffid.games.panna.shared.game.systems.shooting

import com.mgtriffid.games.cotta.core.annotations.Predicted
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EntityProcessingContext
import com.mgtriffid.games.cotta.core.systems.EntityProcessingSystem
import com.mgtriffid.games.panna.shared.game.components.WeaponEquippedComponent

@Predicted
class SwitchWeaponSystem : EntityProcessingSystem {
    override fun process(e: Entity, ctx: EntityProcessingContext) {
        if (e.hasComponent(WeaponEquippedComponent::class)) {
            val weaponEquipped = e.getComponent(WeaponEquippedComponent::class)
            if (weaponEquipped.wantToEquip != 0.toByte() &&
                weaponEquipped.wantToEquip != weaponEquipped.equipped &&
                weaponEquipped.cooldownUntil < ctx.clock().time()
            ) {
                weaponEquipped.equipped = weaponEquipped.wantToEquip
                weaponEquipped.wantToEquip = 0
            }
        }
    }
}
