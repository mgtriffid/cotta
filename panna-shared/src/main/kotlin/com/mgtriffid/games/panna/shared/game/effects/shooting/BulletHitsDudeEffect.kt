package com.mgtriffid.games.panna.shared.game.effects.shooting

import com.mgtriffid.games.cotta.EffectData
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.id.EntityId

interface BulletHitsDudeEffect : CottaEffect {

    @EffectData val shotId: EntityId

    companion object {
        fun create(shotId: EntityId): BulletHitsDudeEffect {
            return BulletHitsDudeEffectImpl(shotId)
        }
    }
}

private data class BulletHitsDudeEffectImpl(
    override val shotId: EntityId
) : BulletHitsDudeEffect
