package com.mgtriffid.games.cotta.client

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.id.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.id.PredictedEntityId

// TODO provide ability to show authoritative versions of entities together with predicted so that user could debug
interface DrawableState {
    object EMPTY : DrawableState {
        override val entities: List<Entity> = emptyList()
        // TODO this is a bit odd, it exposes internal implementation details which dev should not have to care about.
        //  Would be nice to have just one `entityId` that is always used by graphics (GDX client).
        override val authoritativeToPredictedEntityIds: Map<AuthoritativeEntityId, PredictedEntityId> = emptyMap()
        override val effects = DrawableEffects.EMPTY
    }

    val entities: List<Entity>
    val authoritativeToPredictedEntityIds: Map<AuthoritativeEntityId, PredictedEntityId>

    // Need to add effects now.
    // Effects can come from prediction or from authoritative simulation.
    // Those coming from prediction can be in fact mis-predicted, and then we should allow developer to handle that:
    // like, stop playing music, or stop showing some animation.
    // For now we'll make two sets of Effects: real and predicted. And will figure out what to draw outside of this
    // interface and implementation. Then, when the algorithm and convenient structure is more clear, we'll add some
    // component that manages visual effects automatically.
    val effects: DrawableEffects
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
