package com.mgtriffid.games.panna.shared.game.effects

import com.mgtriffid.games.cotta.EffectData
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.EntityId

interface MovementEffect : CottaEffect {
    @EffectData val velocityX: Int
    @EffectData val velocityY: Int
    @EffectData val entityId: EntityId // should be careful when transferring this over the wire
    companion object {
        fun create(velocityX: Int, velocityY: Int, entityId: EntityId): MovementEffect {
            return MovementEffectImpl(velocityX, velocityY, entityId)
        }
    }
}

private data class MovementEffectImpl(
    override val velocityX: Int,
    override val velocityY: Int,
    override val entityId: EntityId
) : MovementEffect
