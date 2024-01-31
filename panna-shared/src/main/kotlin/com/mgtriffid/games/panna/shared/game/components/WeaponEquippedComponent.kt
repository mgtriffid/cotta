package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.core.entities.MutableComponent

interface WeaponEquippedComponent : MutableComponent<WeaponEquippedComponent> {
    @ComponentData var equipped: Byte

    companion object {
        fun create(equipped: Byte): WeaponEquippedComponent = WeaponEquippedComponentImpl(equipped)
    }

    override fun copy(): WeaponEquippedComponent {
        return WeaponEquippedComponentImpl(equipped)
    }
}

private data class WeaponEquippedComponentImpl(override var equipped: Byte): WeaponEquippedComponent

const val WEAPON_PISTOL: Byte = 0x02
const val WEAPON_RAILGUN: Byte = 0x03
