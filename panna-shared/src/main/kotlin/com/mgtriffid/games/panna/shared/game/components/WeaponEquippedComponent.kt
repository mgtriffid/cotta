package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.core.entities.MutableComponent

interface WeaponEquippedComponent : MutableComponent<WeaponEquippedComponent> {
    @ComponentData var equipped: Byte
    @ComponentData var wantToEquip: Byte
    @ComponentData var cooldownUntil: Long

    companion object {
        fun create(equipped: Byte, wantToEquip: Byte, cooldownUntil: Long): WeaponEquippedComponent {
            return WeaponEquippedComponentImpl(equipped, wantToEquip, cooldownUntil)
        }
    }

    override fun copy(): WeaponEquippedComponent {
        return WeaponEquippedComponentImpl(equipped, wantToEquip, cooldownUntil)
    }
}

private data class WeaponEquippedComponentImpl(
    override var equipped: Byte,
    override var wantToEquip: Byte,
    override var cooldownUntil: Long
): WeaponEquippedComponent

const val WEAPON_PISTOL: Byte = 0x02
const val WEAPON_RAILGUN: Byte = 0x03

object WeaponCooldowns {
    const val PISTOL: Long = 250
    const val RAILGUN: Long = 1200
}
