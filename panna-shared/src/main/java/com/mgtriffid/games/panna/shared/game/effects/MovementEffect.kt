package com.mgtriffid.games.panna.shared.game.effects

import com.mgtriffid.games.cotta.EffectData
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.EntityId

interface MovementEffect : CottaEffect {
    @EffectData val direction: Byte
    @EffectData val velocity: Int
    @EffectData val entityId: EntityId // should be careful when transferring this over the wire
    companion object {
        fun create(direction: Byte, velocity: Int, entityId: EntityId): MovementEffect {
            return MovementEffectImpl(direction, velocity, entityId)
        }
    }
}

private data class MovementEffectImpl(
    override val direction: Byte,
    override val velocity: Int,
    override val entityId: EntityId
) : MovementEffect
