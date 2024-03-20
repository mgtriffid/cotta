package com.mgtriffid.games.panna.shared

import com.mgtriffid.games.cotta.core.input.PlayerInput

data class PannaPlayerInput(
    val walkingDirection: Byte,
    val shootPressed: Boolean,
    val jumpPressed: Boolean,
    val lookAt: Float,
    val joinPressed: Boolean,
    val switchWeapon: Byte
) : PlayerInput
