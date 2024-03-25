package com.mgtriffid.games.cotta.core.serialization

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.id.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.id.PredictedEntityId

// TODO not necessarily the most fitting package
interface IdsRemapper {
    fun remap(c: Component<*>, ids: (PredictedEntityId) -> AuthoritativeEntityId?): Component<*>
    fun remap(e: CottaEffect, ids: (PredictedEntityId) -> AuthoritativeEntityId?): CottaEffect
}
