package com.mgtriffid.games.cotta.server.workload.components

import com.mgtriffid.games.cotta.core.annotations.Component
import com.mgtriffid.games.cotta.core.entities.MutableComponent

@Component
interface HealthTestComponent : MutableComponent<HealthTestComponent> {
    var health: Int
}
