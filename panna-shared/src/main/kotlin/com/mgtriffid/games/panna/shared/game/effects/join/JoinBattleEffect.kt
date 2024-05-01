package com.mgtriffid.games.panna.shared.game.effects.join

import com.mgtriffid.games.cotta.core.annotations.EffectData
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.PlayerId

interface JoinBattleEffect : CottaEffect {
    @EffectData
    val playerId: PlayerId
}
