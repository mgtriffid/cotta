package com.mgtriffid.games.cotta.client.guice

import com.google.inject.*
import com.google.inject.name.Names
import com.mgtriffid.games.cotta.client.ClientSimulation
import com.mgtriffid.games.cotta.client.ClientSimulationInputProvider
import com.mgtriffid.games.cotta.client.CottaClient
import com.mgtriffid.games.cotta.client.CottaClientInput
import com.mgtriffid.games.cotta.client.impl.ClientSimulationImpl
import com.mgtriffid.games.cotta.client.impl.ClientSimulationInputProviderImpl
import com.mgtriffid.games.cotta.client.impl.CottaClientImpl
import com.mgtriffid.games.cotta.client.impl.IncomingDataBuffer
import com.mgtriffid.games.cotta.core.CottaEngine
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.entities.impl.AtomicLongTickProvider
import com.mgtriffid.games.cotta.core.entities.impl.CottaStateImpl
import com.mgtriffid.games.cotta.core.guice.SerializationModule
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
import com.mgtriffid.games.cotta.core.simulation.SimulationInputHolder
import com.mgtriffid.games.cotta.core.simulation.impl.SimulationInputHolderImpl
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
        bind(Int::class.java).annotatedWith(Names.named("historyLength")).toInstance(8)

        bind(ClientSimulationInputProvider::class.java).to(ClientSimulationInputProviderImpl::class.java).`in`(Scopes.SINGLETON)
        bind(ClientSimulation::class.java).to(ClientSimulationImpl::class.java).`in`(Scopes.SINGLETON)
        bind(TickProvider::class.java).to(AtomicLongTickProvider::class.java).`in`(Scopes.SINGLETON)
        bind(CottaState::class.java).to(CottaStateImpl::class.java).`in`(Scopes.SINGLETON)
        bind(SimulationInputHolder::class.java).to(SimulationInputHolderImpl::class.java).`in`(Scopes.SINGLETON)
        bind(object : TypeLiteral<IncomingDataBuffer<MapsStateRecipe, MapsDeltaRecipe, MapsInputRecipe>>() {})
            .toInstance(IncomingDataBuffer())
        install(SerializationModule())
    }
}
