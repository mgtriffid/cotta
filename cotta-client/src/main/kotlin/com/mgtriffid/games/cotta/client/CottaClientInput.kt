package com.mgtriffid.games.cotta.client

import com.mgtriffid.games.cotta.core.input.PlayerInput

interface CottaClientInput {
    fun input(): PlayerInput
}
