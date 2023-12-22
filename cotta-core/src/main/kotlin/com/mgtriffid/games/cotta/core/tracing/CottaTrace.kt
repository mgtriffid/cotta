package com.mgtriffid.games.cotta.core.tracing

import com.mgtriffid.games.cotta.core.tracing.elements.TraceElement

data class CottaTrace(
    val elements: List<TraceElement>
) {
    companion object {
        fun from(element: TraceElement): CottaTrace {
            return CottaTrace(listOf(element))
        }
    }

    fun plus(element: TraceElement): CottaTrace {
        return CottaTrace(elements + element)
    }
}
