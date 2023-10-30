package com.mgtriffid.games.cotta.server.guice

import com.google.inject.Binder
import com.google.inject.Module
import com.google.inject.Provides
import com.google.inject.Scopes
import com.google.inject.Singleton
import com.google.inject.TypeLiteral
import com.google.inject.name.Names.named
import com.mgtriffid.games.cotta.core.CottaEngine
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.NonPlayerInputProvider
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.effects.impl.EffectBusImpl
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.entities.impl.AtomicLongTickProvider
import com.mgtriffid.games.cotta.core.entities.impl.CottaStateImpl
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
import com.mgtriffid.games.cotta.core.simulation.EffectsHistory
import com.mgtriffid.games.cotta.core.simulation.EntityOwnerSawTickProvider
import com.mgtriffid.games.cotta.core.simulation.PlayersSawTicks
import com.mgtriffid.games.cotta.core.simulation.SimulationInputHolder
import com.mgtriffid.games.cotta.core.simulation.impl.EffectsHistoryImpl
import com.mgtriffid.games.cotta.core.simulation.impl.EntityOwnerSawTickProviderImpl
import com.mgtriffid.games.cotta.core.simulation.impl.PlayersSawTickImpl
import com.mgtriffid.games.cotta.core.simulation.invokers.*
import com.mgtriffid.games.cotta.core.simulation.invokers.context.InputProcessingContext
import com.mgtriffid.games.cotta.core.simulation.invokers.context.impl.InputProcessingContextImpl
import com.mgtriffid.games.cotta.core.simulation.invokers.impl.LagCompensatingInputProcessingSystemInvokerImpl
import com.mgtriffid.games.cotta.network.CottaServerNetwork
import com.mgtriffid.games.cotta.network.kryonet.KryonetCottaServerNetwork
import com.mgtriffid.games.cotta.server.*
import com.mgtriffid.games.cotta.server.impl.*

class CottaServerModule(
    private val game: CottaGame
) : Module {
    override fun configure(binder: Binder) {
        with (binder) {
            bind(CottaGameInstance::class.java).to(object : TypeLiteral<CottaGameInstanceImpl<MapsStateRecipe, MapsDeltaRecipe, MapsInputRecipe>>() {})
            bind(CottaGame::class.java).toInstance(game)
            bind(ClientsGhosts::class.java).`in`(Scopes.SINGLETON)

            bind(ClientsInputProvider::class.java)
                .to(object : TypeLiteral<ClientsInputProviderImpl<MapsInputRecipe>>() {})
                .`in`(Scopes.SINGLETON)

            bind(ComponentsRegistryImpl::class.java).`in`(Scopes.SINGLETON)
            bind(TickProvider::class.java).to(AtomicLongTickProvider::class.java).`in`(Scopes.SINGLETON)
            bind(Int::class.java).annotatedWith(named("historyLength")).toInstance(8)
            bind(CottaState::class.java).to(CottaStateImpl::class.java).`in`(Scopes.SINGLETON)
            bind(ServerToClientDataChannel::class.java)
                .to(object : TypeLiteral<ServerToClientDataChannelImpl<MapsStateRecipe, MapsDeltaRecipe, MapsInputRecipe>>(){})

            bind(ServerSimulation::class.java).to(ServerSimulationImpl::class.java).`in`(Scopes.SINGLETON)

            bind(NonPlayerInputProvider::class.java).toInstance(game.nonPlayerInputProvider)
            bind(ServerSimulationInputProvider::class.java).to(ServerSimulationInputProviderImpl::class.java).`in`(Scopes.SINGLETON)
            bind(SimulationInputHolder::class.java).to(SimulationInputHolderImpl::class.java).`in`(Scopes.SINGLETON)
            bind(MetaEntities::class.java).to(MetaEntitiesImpl::class.java).`in`(Scopes.SINGLETON)
            bind(PlayersSawTicks::class.java).to(PlayersSawTickImpl::class.java).`in`(Scopes.SINGLETON)
            bind(InvokersFactory::class.java).to(InvokersFactoryImpl::class.java).`in`(Scopes.SINGLETON)
            bind(InvokersFactoryImpl.SawTickHolder::class.java).toInstance(InvokersFactoryImpl.SawTickHolder(null))
            bind(EffectsHistory::class.java).to(EffectsHistoryImpl::class.java).`in`(Scopes.SINGLETON)
            bind(EffectBus::class.java).to(EffectBusImpl::class.java).`in`(Scopes.SINGLETON)
            bind(LagCompensatingEffectBus::class.java).annotatedWith(named("historical")).to(HistoricalLagCompensatingEffectBus::class.java).`in`(Scopes.SINGLETON)
            bind(LagCompensatingEffectBus::class.java).annotatedWith(named("lagCompensated")).to(LagCompensatingEffectBusImpl::class.java).`in`(Scopes.SINGLETON)
            bind(LagCompensatingInputProcessingSystemInvoker::class.java).to(LagCompensatingInputProcessingSystemInvokerImpl::class.java).`in`(Scopes.SINGLETON)
            bind(EntityOwnerSawTickProvider::class.java).to(EntityOwnerSawTickProviderImpl::class.java).`in`(Scopes.SINGLETON)
            bind(Entities::class.java).annotatedWith(named("latest")).to(LatestEntities::class.java)
            bind(InputProcessingContext::class.java).to(InputProcessingContextImpl::class.java).`in`(Scopes.SINGLETON)

            bindEngineParts()
        }
    }

    private fun Binder.bindEngineParts() {
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

    @Provides @Singleton // TODO game instance scoped
    fun provideCottaServerNetwork(): CottaServerNetwork {
        val network = KryonetCottaServerNetwork()
        network.initialize()
        return network
    }

    @Provides @Singleton
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
