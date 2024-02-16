package com.mgtriffid.games.cotta.core.guice

import com.esotericsoftware.kryo.Kryo
import com.google.inject.Binder
import com.google.inject.Module
import com.google.inject.Scopes
import com.google.inject.TypeLiteral
import com.google.inject.name.Names.named
import com.mgtriffid.games.cotta.core.serialization.IdsRemapper
import com.mgtriffid.games.cotta.core.serialization.SnapsSerialization
import com.mgtriffid.games.cotta.core.serialization.StateSnapper
import com.mgtriffid.games.cotta.core.serialization.bytes.BytesSnapsSerialization
import com.mgtriffid.games.cotta.core.serialization.bytes.BytesStateSnapper
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesDeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesStateRecipe
import com.mgtriffid.games.cotta.core.serialization.maps.IdsRemapperImpl

class BytesSerializationModule(private val idsRemapper: IdsRemapperImpl) : Module {
    override fun configure(binder: Binder) {
        with(binder) {
            val kryo = Kryo()
            val snapsSerialization = BytesSnapsSerialization()
            bind(Kryo::class.java).annotatedWith(named("snapper")).toInstance(kryo)
            bind(BytesStateSnapper::class.java).`in`(Scopes.SINGLETON)
            bind(IdsRemapper::class.java).toInstance(idsRemapper)
            bind(object : TypeLiteral<SnapsSerialization<BytesStateRecipe, BytesDeltaRecipe>>() {})
                .toInstance(snapsSerialization)
        }
    }
}
