package com.mgtriffid.games.cotta.serialization.workload.components

import com.mgtriffid.games.cotta.Component

@Component
interface TwoNumericalFieldsComponent : com.mgtriffid.games.cotta.core.entities.Component<TwoNumericalFieldsComponent> {
    val a: Int
    val b: Int
}
