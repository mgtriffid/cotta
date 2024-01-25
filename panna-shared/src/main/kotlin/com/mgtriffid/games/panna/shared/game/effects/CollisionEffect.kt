package com.mgtriffid.games.panna.shared.game.effects

import com.mgtriffid.games.cotta.EffectData
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.id.EntityId

interface CollisionEffect : CottaEffect {
    @EffectData
    val id: EntityId

    @EffectData
    val id2: EntityId

    @EffectData
    val x: Float

    @EffectData
    val y: Float

    companion object {
        fun create(id: EntityId, id2: EntityId, x: Float, y: Float): CollisionEffect {
            return CollisionEffectImpl(id, id2, x, y)
        }
    }
}

private data class CollisionEffectImpl(
    override val id: EntityId, override val id2: EntityId, override val x: Float, override val y: Float
) : CollisionEffect
