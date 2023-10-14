package com.mgtriffid.games.cotta.client.guice

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Scopes
import com.google.inject.Singleton
import com.google.inject.TypeLiteral
import com.mgtriffid.games.cotta.client.CottaClient
import com.mgtriffid.games.cotta.client.CottaClientInput
import com.mgtriffid.games.cotta.client.impl.CottaClientImpl
import com.mgtriffid.games.cotta.core.CottaEngine
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.impl.CottaEngineImpl
import com.mgtriffid.games.cotta.core.registry.ComponentsRegistryImpl
import com.mgtriffid.games.cotta.core.serialization.InputSerialization
import com.mgtriffid.games.cotta.core.serialization.InputSnapper
import com.mgtriffid.games.cotta.core.serialization.SnapsSerialization
import com.mgtriffid.games.cotta.core.serialization.StateSnapper
import com.mgtriffid.games.cotta.core.serialization.impl.MapsInputSerialization
import com.mgtriffid.games.cotta.core.serialization.impl.MapsInputSnapper
import com.mgtriffid.games.cotta.core.serialization.impl.MapsSnapsSerialization
import com.mgtriffid.games.cotta.core.serialization.impl.MapsStateSnapper
import com.mgtriffid.games.cotta.core.serialization.impl.recipe.MapsDeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.impl.recipe.MapsInputRecipe
import com.mgtriffid.games.cotta.core.serialization.impl.recipe.MapsStateRecipe
import com.mgtriffid.games.cotta.network.CottaClientNetwork
import com.mgtriffid.games.cotta.network.kryonet.KryonetCottaClientNetwork

class CottaClientModule(
    private val game: CottaGame,
    private val input: CottaClientInput
) : AbstractModule() {
    override fun configure() {
        bind(CottaGame::class.java).toInstance(game)
        bind(CottaClientInput::class.java).toInstance(input)
        bind(CottaClientNetwork::class.java).to(KryonetCottaClientNetwork::class.java)
        bind(CottaClient::class.java)
            .to(object: TypeLiteral<CottaClientImpl<MapsStateRecipe, MapsDeltaRecipe, MapsInputRecipe>>(){})
        bindEngineParts()
    }

    private fun bindEngineParts() {
        val stateSnapper = MapsStateSnapper()
        val snapsSerialization = MapsSnapsSerialization()
        val inputSnapper = MapsInputSnapper()
        val inputSerialization = MapsInputSerialization()
        bind(MapsStateSnapper::class.java).toInstance(stateSnapper)
        bind(object : TypeLiteral<StateSnapper<MapsStateRecipe, MapsDeltaRecipe>>() {}).toInstance(stateSnapper)
        bind(MapsSnapsSerialization::class.java).`in`(Scopes.SINGLETON)
        bind(object : TypeLiteral<SnapsSerialization<MapsStateRecipe, MapsDeltaRecipe>>() {}).toInstance(snapsSerialization)
        bind(MapsInputSnapper::class.java).toInstance(inputSnapper)
        bind(object : TypeLiteral<InputSnapper<MapsInputRecipe>>(){}).toInstance(inputSnapper)
        bind(MapsInputSerialization::class.java).toInstance(inputSerialization)
        bind(object : TypeLiteral<InputSerialization<MapsInputRecipe>>(){}).toInstance(inputSerialization)
    }

    @Provides
    @Singleton
    fun provideCottaEngine(
        componentRegistry: ComponentsRegistryImpl,
        stateSnapper: MapsStateSnapper,
        snapsSerialization: MapsSnapsSerialization,
        inputSnapper: MapsInputSnapper,
        inputSerialization: MapsInputSerialization,
    ): CottaEngine<MapsStateRecipe, MapsDeltaRecipe, MapsInputRecipe> {
        return CottaEngineImpl(
            componentRegistry,
            stateSnapper,
            snapsSerialization,
            inputSnapper,
            inputSerialization
        )
    }
}