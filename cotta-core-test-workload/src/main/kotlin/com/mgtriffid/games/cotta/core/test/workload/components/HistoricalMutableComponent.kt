package com.mgtriffid.games.cotta.core.test.workload.components

import com.mgtriffid.games.cotta.core.annotations.Historical
import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.MutableComponent

@Historical
interface HistoricalMutableComponent : MutableComponent {
    var value: Int
}
