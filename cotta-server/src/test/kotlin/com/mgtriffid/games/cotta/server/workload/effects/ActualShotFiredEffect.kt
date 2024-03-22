package com.mgtriffid.games.cotta.server.workload.effects

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.PlayerId

interface ActualShotFiredEffect : CottaEffect {
    val x: Int
    val shooter: PlayerId
}
