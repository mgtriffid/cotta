package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.core.annotations.Component
import com.mgtriffid.games.cotta.core.annotations.ComponentData
import com.mgtriffid.games.cotta.core.entities.MutableComponent

@Component
interface WeaponEquippedComponent : MutableComponent {
    @ComponentData
    var equipped: Byte
    @ComponentData
    var wantToEquip: Byte
    @ComponentData
    var cooldownUntil: Long
}

const val WEAPON_PISTOL: Byte = 0x02
const val WEAPON_RAILGUN: Byte = 0x03

object WeaponCooldowns {
    const val PISTOL: Long = 250
    const val RAILGUN: Long = 1200
}
