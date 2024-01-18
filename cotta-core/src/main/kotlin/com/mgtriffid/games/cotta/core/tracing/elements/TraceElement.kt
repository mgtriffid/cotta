package com.mgtriffid.games.cotta.core.tracing.elements

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.id.EntityId

sealed interface TraceElement {
    data class InputTraceElement(
        // BUG: When Input comes from predicted Entity then it's not matched correctly.
        //      So it's possible to spawn two bullets on Client and only one on Server.
        //      This is because we don't match traces of bullet creation properly..
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
