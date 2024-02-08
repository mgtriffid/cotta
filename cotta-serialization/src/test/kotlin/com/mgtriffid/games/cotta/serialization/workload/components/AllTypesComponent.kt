package com.mgtriffid.games.cotta.serialization.workload.components

import com.mgtriffid.games.cotta.Component

@Component
interface AllTypesComponent : com.mgtriffid.games.cotta.core.entities.Component<AllTypesComponent> {
    var a: Byte
    var b: Short
    var c: Int
    var d: Long
    var e: Float
    var f: Double
}