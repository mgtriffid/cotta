package com.mgtriffid.games.cotta.client

import com.mgtriffid.games.cotta.client.impl.Delta

interface Simulations {
    fun integrate(delta: Delta.Present)
}
