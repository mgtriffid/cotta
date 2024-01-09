package com.mgtriffid.games.panna.shared.game.effects

import com.mgtriffid.games.cotta.EffectData
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.id.EntityId

interface MovementEffect : CottaEffect {
    @EffectData val velocityX: Float
    @EffectData val velocityY: Float
    @EffectData val entityId: EntityId // should be careful when transferring this over the wire
    companion object {
        fun create(velocityX: Float, velocityY: Float, entityId: EntityId): MovementEffect {
            return MovementEffectImpl(velocityX, velocityY, entityId)
        }
    }
}

private data class MovementEffectImpl(
    override val velocityX: Float,
    override val velocityY: Float,
    override val entityId: EntityId
) : MovementEffect
