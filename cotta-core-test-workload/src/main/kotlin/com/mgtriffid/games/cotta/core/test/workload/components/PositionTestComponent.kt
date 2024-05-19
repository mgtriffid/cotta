package com.mgtriffid.games.cotta.core.test.workload.components

import com.mgtriffid.games.cotta.core.entities.MutableComponent

interface PositionTestComponent : MutableComponent {
    var x: Int
    var y: Int
}
