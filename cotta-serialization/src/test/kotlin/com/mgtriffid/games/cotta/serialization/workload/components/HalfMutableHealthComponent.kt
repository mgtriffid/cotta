package com.mgtriffid.games.cotta.serialization.workload.components

import com.mgtriffid.games.cotta.Component
import com.mgtriffid.games.cotta.core.entities.MutableComponent

@Component
interface HalfMutableHealthComponent : MutableComponent<HalfMutableHealthComponent> {
    val max: Int
    var health: Int
}
