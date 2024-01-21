package com.mgtriffid.games.cotta.core.serialization

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.entities.id.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.id.PredictedEntityId
import com.mgtriffid.games.cotta.core.tracing.CottaTrace
import com.mgtriffid.games.cotta.core.tracing.elements.TraceElement

// TODO not necessarily the most fitting package
interface IdsRemapper {
    fun remap(c: Component<*>, ids: (PredictedEntityId) -> AuthoritativeEntityId?): Component<*>
    fun remap(e: CottaEffect, ids: (PredictedEntityId) -> AuthoritativeEntityId?): CottaEffect
    fun remap(ic: InputComponent<*>, ids: (PredictedEntityId) -> AuthoritativeEntityId?): InputComponent<*>

    fun remapTrace(trace: CottaTrace, mappings: (PredictedEntityId) -> AuthoritativeEntityId?): CottaTrace {
        return CottaTrace(trace.elements.map { element ->
            when (element) {
                is TraceElement.EffectTraceElement -> TraceElement.EffectTraceElement(remap(element.effect, mappings))
                is TraceElement.EntityProcessingTraceElement -> TODO()
                is TraceElement.InputTraceElement -> if (element.entityId is PredictedEntityId) {
                    TraceElement.InputTraceElement(mappings(element.entityId) ?: element.entityId)
                } else {
                    element
                }
            }
        })
    }
}
