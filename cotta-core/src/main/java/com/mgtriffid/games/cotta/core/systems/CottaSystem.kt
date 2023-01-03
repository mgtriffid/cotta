package com.mgtriffid.games.cotta.core.systems

import com.mgtriffid.games.cotta.core.entities.Entity

interface CottaSystem {
    fun update(e: Entity)
}
