package com.mgtriffid.games.cotta.client

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.id.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.id.PredictedEntityId

// TODO provide ability to show authoritative versions of entities together with predicted so that user could debug
sealed interface DrawableState {
    interface Ready : DrawableState {
        val playerId: PlayerId
        val entities: List<Entity>
        val effects: DrawableEffects
    }
    data object NotReady : DrawableState
}

// Visual effects are REALLY data-powered. It's about equality not identity, for they are to be rendered purely based on
// data.
interface DrawableEffects {
    val real: Collection<DrawableEffect>
    val predicted: Collection<DrawableEffect>
    val mispredicted: Collection<DrawableEffect>

    object EMPTY : DrawableEffects {
        override val real: Collection<DrawableEffect> = emptyList()
        override val predicted: Collection<DrawableEffect> = emptyList()
        override val mispredicted: Collection<DrawableEffect> = emptyList()
    }
}

data class DrawableEffect(
    val effect: CottaEffect
)
