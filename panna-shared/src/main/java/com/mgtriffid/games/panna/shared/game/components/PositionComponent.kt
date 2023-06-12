package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.core.entities.MutableComponent
import java.awt.ComponentOrientation

// very much mutable, we need to send only deltas
// also need to provide instantiation
// need to provide accessor in the past
// so it is generally an interface and used as interface in code but should generate serde, serde for deltas, different
// mutators potentially powered by backing arrays
// so first we have an interface, it has mutable fields and immutable fields
// second we have a version that only has val accessors and no var accessors - that's for historic data
// third we have a serializer and a deserializer somehow
// and deltas, deltas!

const val ORIENTATION_LEFT = 0
const val ORIENTATION_RIGHT = 1

interface PositionComponent : MutableComponent<PositionComponent> {
    companion object {
        fun create(xPos: Int, yPos: Int, orientation: Int): PositionComponent {
            return PositionComponentImpl(xPos, yPos, orientation)
        }
    }

    @ComponentData var xPos: Int
    @ComponentData var yPos: Int
    @ComponentData var orientation: Int
}
private data class PositionComponentImpl(
    override var xPos: Int,
    override var yPos: Int,
    override var orientation: Int
) : PositionComponent {
    override fun copy(): PositionComponent = this.copy(xPos = xPos, yPos = yPos, orientation = orientation)
}
