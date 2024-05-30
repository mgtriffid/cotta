package com.mgtriffid.games.cotta.core.test.workload.components

import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.MutableComponent

// TODO not require MutableComponent
interface NonHistoricalMutableComponent : MutableComponent {
    var value: Int
}
