package com.mgtriffid.games.cotta.core.tracing.elements

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.EntityId

sealed interface TraceElement {
    data class InputTraceElement(
        val entityId: EntityId
    ) : TraceElement

    data class EffectTraceElement(
        val effect: CottaEffect
    ) : TraceElement

    data class EntityProcessingTraceElement(
        val entityId: EntityId,
        val systemId: Int
    ) : TraceElement
}
