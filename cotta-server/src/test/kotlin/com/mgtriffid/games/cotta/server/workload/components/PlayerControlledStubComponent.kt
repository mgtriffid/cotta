package com.mgtriffid.games.cotta.server.workload.components

import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.entities.MutableComponent

@com.mgtriffid.games.cotta.Component
interface PlayerControlledStubComponent :
    MutableComponent<PlayerControlledStubComponent> {
    var aim: Int
    var shoot: Boolean
}
