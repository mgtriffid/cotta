package com.mgtriffid.games.panna.shared.game.effects.walking

import com.mgtriffid.games.cotta.EffectData
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.id.EntityId

interface WalkingEffect : CottaEffect {
    @EffectData
    val entityId: EntityId
    @EffectData
    val direction: Byte

    companion object {
        fun create(entityId: EntityId, direction: Byte): WalkingEffect {
            return WalkingEffectImpl(entityId, direction)
        }
    }
}

private data class WalkingEffectImpl(
    override val entityId: EntityId,
    override val direction: Byte
) : WalkingEffect
