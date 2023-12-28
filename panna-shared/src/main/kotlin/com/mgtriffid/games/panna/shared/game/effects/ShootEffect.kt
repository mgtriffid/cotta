package com.mgtriffid.games.panna.shared.game.effects

import com.mgtriffid.games.cotta.EffectData
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.EntityId

interface ShootEffect : CottaEffect {
    @EffectData val shooterId: EntityId

    companion object {
        fun create(shooterId: EntityId): ShootEffect {
            return ShootEffectImpl(shooterId)
        }
    }
}

private data class ShootEffectImpl(
    override val shooterId: EntityId
) : ShootEffect
