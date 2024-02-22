package com.mgtriffid.games.cotta.core.guice

import com.esotericsoftware.kryo.Kryo
import com.google.inject.Binder
import com.google.inject.Module
import com.google.inject.Scopes
import com.google.inject.TypeLiteral
import com.google.inject.name.Names.named
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.id.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.id.PredictedEntityId
import com.mgtriffid.games.cotta.core.entities.id.StaticEntityId
import com.mgtriffid.games.cotta.core.serialization.IdsRemapper
import com.mgtriffid.games.cotta.core.serialization.InputSerialization
import com.mgtriffid.games.cotta.core.serialization.InputSnapper
import com.mgtriffid.games.cotta.core.serialization.SnapsSerialization
import com.mgtriffid.games.cotta.core.serialization.StateSnapper
import com.mgtriffid.games.cotta.core.serialization.bytes.BytesInputSerialization
import com.mgtriffid.games.cotta.core.serialization.bytes.BytesInputSnapper
import com.mgtriffid.games.cotta.core.serialization.bytes.BytesSnapsSerialization
import com.mgtriffid.games.cotta.core.serialization.bytes.BytesStateSnapper
import com.mgtriffid.games.cotta.core.serialization.bytes.DataClassSerializer
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesCreatedEntitiesWithTracesRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesDeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesInputRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesStateRecipe
import com.mgtriffid.games.cotta.core.serialization.IdsRemapperImpl
import com.mgtriffid.games.cotta.core.serialization.MetaEntitiesDeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesMetaEntitiesDeltaRecipe

class BytesSerializationModule(
    private val idsRemapper: IdsRemapperImpl
) : Module {
    override fun configure(binder: Binder) {
        with(binder) {
            val kryo = Kryo()
            registerIds(kryo)
            registerPlayerId(kryo)
            val snapsSerialization = BytesSnapsSerialization()
            bind(Kryo::class.java).annotatedWith(named("snapper")).toInstance(kryo)
            bind(BytesStateSnapper::class.java).`in`(Scopes.SINGLETON)
            bind(IdsRemapper::class.java).toInstance(idsRemapper)
            bind(object : TypeLiteral<SnapsSerialization<
                BytesStateRecipe,
                BytesDeltaRecipe,
                BytesCreatedEntitiesWithTracesRecipe,
                BytesMetaEntitiesDeltaRecipe
                >>() {})
                .toInstance(snapsSerialization)
            bind(object : TypeLiteral<StateSnapper<
                BytesStateRecipe,
                BytesDeltaRecipe,
                BytesCreatedEntitiesWithTracesRecipe,
                BytesMetaEntitiesDeltaRecipe
                >>() {}).to(BytesStateSnapper::class.java).`in`(Scopes.SINGLETON)
            bind(object:TypeLiteral<InputSnapper<BytesInputRecipe>>(){}).to(BytesInputSnapper::class.java)
            bind(object : TypeLiteral<InputSerialization<BytesInputRecipe>>(){}).to(BytesInputSerialization::class.java).`in`(Scopes.SINGLETON)
        }
    }

    private fun registerPlayerId(kryo: Kryo) {
        kryo.register(PlayerId::class.java, DataClassSerializer(PlayerId::class))
    }

    private fun registerIds(kryo: Kryo) {
        kryo.register(AuthoritativeEntityId::class.java, DataClassSerializer(AuthoritativeEntityId::class))
        kryo.register(PredictedEntityId::class.java, DataClassSerializer(PredictedEntityId::class))
        kryo.register(StaticEntityId::class.java, DataClassSerializer(StaticEntityId::class))
    }
}
