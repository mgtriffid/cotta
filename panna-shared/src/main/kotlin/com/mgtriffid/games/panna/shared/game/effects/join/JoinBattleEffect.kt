package com.mgtriffid.games.panna.shared.game.effects.join

import com.mgtriffid.games.cotta.EffectData
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Entity

interface JoinBattleEffect : CottaEffect {
    @EffectData
    val ownedBy: Entity.OwnedBy
    companion object {
        fun create(ownedBy: Entity.OwnedBy): JoinBattleEffect {
            return JoinBattleEffectImpl(ownedBy)
        }
    }
}

private data class JoinBattleEffectImpl(
    override val ownedBy: Entity.OwnedBy
) : JoinBattleEffect
