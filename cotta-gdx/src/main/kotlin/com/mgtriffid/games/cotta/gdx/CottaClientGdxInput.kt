package com.mgtriffid.games.cotta.gdx

import com.mgtriffid.games.cotta.client.CottaClientInput

interface CottaClientGdxInput : CottaClientInput {
    fun accumulate()
    fun clear()
}
