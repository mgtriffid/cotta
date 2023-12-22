package com.mgtriffid.games.cotta.core.serialization

import com.mgtriffid.games.cotta.core.entities.Entity

data class StateSnapshot(val entities: Set<Entity>)
