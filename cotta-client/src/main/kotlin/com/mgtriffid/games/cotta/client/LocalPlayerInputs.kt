package com.mgtriffid.games.cotta.client

import com.mgtriffid.games.cotta.core.input.PlayerInput

interface LocalPlayerInputs {
    fun get(tick: Long): PlayerInput
    fun all(): Map<Long, PlayerInput>
    fun collect()
}
