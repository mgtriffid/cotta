package com.mgtriffid.games.panna.shared.game.effects.join

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.PlayerId

interface JoinBattleEffect : CottaEffect {
    val playerId: PlayerId
}
