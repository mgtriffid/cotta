package com.mgtriffid.games.cotta.core.serialization.impl

import com.mgtriffid.games.cotta.core.serialization.SnapsSerialization

class MapsSnapsSerialization : SnapsSerialization<MapsStateRecipe, MapsDeltaRecipe> {
    override fun serializeDeltaRecipe(recipe: MapsDeltaRecipe): ByteArray {
        TODO("Not yet implemented")
    }

    override fun deserializeDeltaRecipe(bytes: ByteArray): MapsDeltaRecipe {
        TODO("Not yet implemented")
    }

    override fun deserializeStateRecipe(bytes: ByteArray): MapsStateRecipe {
        TODO("Not yet implemented")
    }

    override fun serializeStateRecipe(recipe: MapsStateRecipe): ByteArray {
        TODO("Not yet implemented")
    }

}
