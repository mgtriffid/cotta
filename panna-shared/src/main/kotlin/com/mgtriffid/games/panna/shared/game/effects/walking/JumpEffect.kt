package com.mgtriffid.games.panna.shared.game.effects.walking

import com.mgtriffid.games.cotta.EffectData
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.id.EntityId

interface JumpEffect : CottaEffect {
    @EffectData val entityId: EntityId
    companion object {
        fun create(entityId: EntityId): JumpEffect {
            return JumpEffectImpl(entityId)
        }
    }
}

private data class JumpEffectImpl(
    override val entityId: EntityId
) : JumpEffect
