package com.mgtriffid.games.cotta.network.protocol.serialization

import com.mgtriffid.games.cotta.core.entities.Entity

data class StateSnapshot(val entities: Set<Entity>)
