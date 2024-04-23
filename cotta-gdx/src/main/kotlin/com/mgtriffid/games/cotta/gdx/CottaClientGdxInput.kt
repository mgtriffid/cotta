package com.mgtriffid.games.cotta.gdx

import com.mgtriffid.games.cotta.client.CottaClientInput

// TODO reconsider this, it's weird to have clear in implementation but
//  accumulate defined here, very confusing
interface CottaClientGdxInput : CottaClientInput {
    fun accumulate()
}
