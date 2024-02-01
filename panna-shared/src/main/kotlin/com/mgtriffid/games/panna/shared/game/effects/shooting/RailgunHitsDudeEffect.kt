package com.mgtriffid.games.panna.shared.game.effects.shooting

import com.mgtriffid.games.cotta.EffectData
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.id.EntityId

interface RailgunHitsDudeEffect : CottaEffect {
    @EffectData val shotId: EntityId

    companion object {
        fun create(shotId: EntityId): RailgunHitsDudeEffect {
            return RailgunHitsDudeEffectImpl(shotId)
        }
    }
}

private data class RailgunHitsDudeEffectImpl(
    override val shotId: EntityId
) : RailgunHitsDudeEffect
