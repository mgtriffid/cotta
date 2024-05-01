package com.mgtriffid.games.cotta.server.workload.components

import com.mgtriffid.games.cotta.core.entities.Component

@com.mgtriffid.games.cotta.core.annotations.Component
interface VelocityTestComponent : Component<VelocityTestComponent> {
    val velocity: Int
}
