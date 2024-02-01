package com.mgtriffid.games.panna.shared.game.effects.shooting

import com.mgtriffid.games.cotta.EffectData
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.id.EntityId

interface RailgunShotEffect : CottaEffect {
    @EffectData
    val shooterId: EntityId
    @EffectData
    val x1: Float
    @EffectData
    val y1: Float
    @EffectData
    val x2: Float
    @EffectData
    val y2: Float

    companion object {
        fun create(
            shooterId: EntityId,
            x1: Float, y1: Float,
            x2: Float, y2: Float
        ): RailgunShotEffect {
            return RailgunShotEffectImpl(shooterId, x1, y1, x2, y2)
        }
    }
}

private data class RailgunShotEffectImpl(
    override val shooterId: EntityId,
    override val x1: Float,
    override val y1: Float,
    override val x2: Float,
    override val y2: Float
) : RailgunShotEffect
