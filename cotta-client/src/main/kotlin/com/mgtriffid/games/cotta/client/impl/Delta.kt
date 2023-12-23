package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.core.entities.Entities

sealed interface Delta {
    class Present(val apply: (Entities) -> Unit) : Delta
    object Absent : Delta
}
