package com.mgtriffid.games.cotta.server.workload.components

import com.mgtriffid.games.cotta.core.annotations.Component
import com.mgtriffid.games.cotta.core.annotations.Historical
import com.mgtriffid.games.cotta.core.entities.MutableComponent

@Component @Historical
interface LinearPositionTestComponent: MutableComponent {
    var x: Int
}
