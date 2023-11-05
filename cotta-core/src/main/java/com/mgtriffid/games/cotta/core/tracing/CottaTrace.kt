package com.mgtriffid.games.cotta.core.tracing

import com.mgtriffid.games.cotta.core.tracing.elements.TraceElement

class CottaTrace private constructor(
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
