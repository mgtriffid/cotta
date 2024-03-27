package com.mgtriffid.games.cotta.client.guice

import com.google.inject.AbstractModule
import com.google.inject.Scopes
import com.google.inject.TypeLiteral
import com.google.inject.name.Names
import com.mgtriffid.games.cotta.client.*
import com.mgtriffid.games.cotta.client.impl.*
import com.mgtriffid.games.cotta.client.interpolation.Interpolators
import com.mgtriffid.games.cotta.client.invokers.PredictionEffectsConsumerSystemInvoker
import com.mgtriffid.games.cotta.client.invokers.PredictionEntityProcessingSystemInvoker
import com.mgtriffid.games.cotta.client.invokers.PredictionInvokersFactory
import com.mgtriffid.games.cotta.client.invokers.impl.*
import com.mgtriffid.games.cotta.client.network.NetworkClient
import com.mgtriffid.games.cotta.client.network.impl.NetworkClientImpl
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.SIMULATION
import com.mgtriffid.games.cotta.core.clock.CottaClock
import com.mgtriffid.games.cotta.core.clock.impl.CottaClockImpl
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.effects.impl.EffectBusImpl
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.entities.impl.AtomicLongTickProvider
import com.mgtriffid.games.cotta.core.entities.impl.CottaStateImpl
import com.mgtriffid.games.cotta.core.guice.BytesSerializationModule
import com.mgtriffid.games.cotta.core.input.InputProcessing
import com.mgtriffid.games.cotta.core.registry.ComponentRegistry
import com.mgtriffid.games.cotta.core.registry.impl.ComponentRegistryImpl
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesCreatedEntitiesWithTracesRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesDeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesInputRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesStateRecipe
import com.mgtriffid.games.cotta.core.serialization.IdsRemapperImpl
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesMetaEntitiesDeltaRecipe
import com.mgtriffid.games.cotta.core.simulation.EffectsHistory
import com.mgtriffid.games.cotta.core.simulation.EntityOwnerSawTickProvider
import com.mgtriffid.games.cotta.core.simulation.PlayersSawTicks
import com.mgtriffid.games.cotta.core.simulation.SimulationInputHolder
import com.mgtriffid.games.cotta.core.simulation.impl.EffectsHistoryImpl
import com.mgtriffid.games.cotta.core.simulation.impl.EntityOwnerSawTickProviderImpl
import com.mgtriffid.games.cotta.core.simulation.impl.PlayersSawTickImpl
import com.mgtriffid.games.cotta.core.simulation.impl.SimulationInputHolderImpl
import com.mgtriffid.games.cotta.core.simulation.invokers.*
import com.mgtriffid.games.cotta.core.simulation.invokers.context.CreateEntityStrategy
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EntityProcessingContext
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import com.mgtriffid.games.cotta.core.simulation.invokers.context.impl.*
import com.mgtriffid.games.cotta.network.CottaClientNetworkTransport
import com.mgtriffid.games.cotta.network.kryonet.KryonetCottaTransportFactory

class CottaClientModule(
    private val game: CottaGame,
    private val input: CottaClientInput
) : AbstractModule() {
    override fun configure() {
        bind(CottaGame::class.java).toInstance(game)
        bind(CottaClientInput::class.java).toInstance(input)
        bindNetwork()
        bind(CottaClient::class.java)
            .to(object : TypeLiteral<CottaClientImpl>() {})
        bind(Int::class.java).annotatedWith(Names.named("historyLength")).toInstance(8)
        bind(Int::class.java).annotatedWith(Names.named("stateHistoryLength")).toInstance(128)

        bind(Simulations::class.java).to(SimulationsImpl::class.java).`in`(Scopes.SINGLETON)

        bind(AuthoritativeSimulation::class.java).to(AuthoritativeSimulationImpl::class.java).`in`(Scopes.SINGLETON)

        bind(GuessedSimulation::class.java).to(GuessedSimulationImpl::class.java).`in`(Scopes.SINGLETON)

        val simulationTickProvider = AtomicLongTickProvider()
        bind(TickProvider::class.java)
            .annotatedWith(Names.named(SIMULATION))
            .toInstance(simulationTickProvider)
        bind(CottaClock::class.java).toInstance(CottaClockImpl(simulationTickProvider, game.config.tickLength))
        bind(SimulationDirector::class.java).to(SimulationDirectorImpl::class.java).`in`(Scopes.SINGLETON)
        bind(DeltasPresent::class.java).to(DeltasPresentImpl::class.java).`in`(Scopes.SINGLETON)

        bind(CottaState::class.java).annotatedWith(Names.named("simulation")).to(CottaStateImpl::class.java)
            .`in`(Scopes.SINGLETON)
        bind(SimulationInputHolder::class.java).to(SimulationInputHolderImpl::class.java).`in`(Scopes.SINGLETON)

        bind(InvokersFactory::class.java)
            .annotatedWith(Names.named("simulation"))
            .to(SimulationInvokersFactory::class.java)
            .`in`(Scopes.SINGLETON)

        bind(InvokersFactory::class.java)
            .annotatedWith(Names.named("prediction"))
            .to(PredictionInvokersFactory::class.java)
            .`in`(Scopes.SINGLETON)

        bind(SawTickHolder::class.java).toInstance(SawTickHolder(null))
        bind(EffectsHistory::class.java).to(EffectsHistoryImpl::class.java).`in`(Scopes.SINGLETON)
        bind(EffectBus::class.java).to(EffectBusImpl::class.java).`in`(Scopes.SINGLETON)
        bind(EffectBus::class.java).annotatedWith(Names.named("prediction")).to(EffectBusImpl::class.java)
            .`in`(Scopes.SINGLETON)
        bind(LagCompensatingEffectBus::class.java).annotatedWith(Names.named("historical")).to(
            HistoricalLagCompensatingEffectBus::class.java
        ).`in`(Scopes.SINGLETON)
        bind(LagCompensatingEffectBus::class.java).annotatedWith(Names.named("lagCompensated")).to(
            LagCompensatingEffectBusImpl::class.java
        ).`in`(Scopes.SINGLETON)
        bind(EntityOwnerSawTickProvider::class.java).to(EntityOwnerSawTickProviderImpl::class.java)
            .`in`(Scopes.SINGLETON)
        bind(Entities::class.java).annotatedWith(Names.named("latest")).to(LatestEntities::class.java)
        bind(EntityProcessingContext::class.java).to(EntityProcessingContextImpl::class.java).`in`(Scopes.SINGLETON)
        bind(EffectProcessingContext::class.java)
            .annotatedWith(Names.named("lagCompensated"))
            .to(LagCompensatingEffectProcessingContext::class.java)
            .`in`(Scopes.SINGLETON)
        bind(EffectProcessingContext::class.java)
            .annotatedWith(Names.named("simple"))
            .to(SimpleEffectProcessingContext::class.java)
            .`in`(Scopes.SINGLETON)
        bind(PlayersSawTicks::class.java).to(PlayersSawTickImpl::class.java).`in`(Scopes.SINGLETON)
        bind(ClientPlayers::class.java).to(ClientPlayersImpl::class.java).`in`(Scopes.SINGLETON)
        bind(InputProcessing::class.java).toInstance(game.inputProcessing)

        bind(PredictionSimulation::class.java).to(PredictionSimulationImpl::class.java).`in`(Scopes.SINGLETON)
        bind(CottaState::class.java).annotatedWith(Names.named("prediction")).to(CottaStateImpl::class.java)
            .`in`(Scopes.SINGLETON)
        val predictionTickProvider = AtomicLongTickProvider()
        bind(TickProvider::class.java)
            .annotatedWith(Names.named("prediction"))
            .toInstance(predictionTickProvider)
        bind(TickProvider::class.java)
            .annotatedWith(Names.named("localInput"))
            .toInstance(AtomicLongTickProvider())
        val predictionClock = CottaClockImpl(predictionTickProvider, game.config.tickLength)
        bind(CottaClock::class.java)
            .annotatedWith(Names.named("prediction"))
            .toInstance(predictionClock)
        bind(Entities::class.java).annotatedWith(Names.named("prediction")).to(PredictedLatestEntities::class.java)
        bind(PredictionEffectsConsumerSystemInvoker::class.java)
            .to(PredictionEffectsConsumerSystemInvokerImpl::class.java)
            .`in`(Scopes.SINGLETON)
        bind(PredictionEntityProcessingSystemInvoker::class.java)
            .to(PredictionEntityProcessingSystemInvokerImpl::class.java)
            .`in`(Scopes.SINGLETON)
        bind(EffectProcessingContext::class.java)
            .annotatedWith(Names.named("prediction"))
            .to(PredictionTracingEffectProcessingContext::class.java)
            .`in`(Scopes.SINGLETON)

        bind(LocalPlayerInputs::class.java).to(LocalPlayerInputsImpl::class.java).`in`(Scopes.SINGLETON)
        bind(CreateEntityStrategy::class.java)
            .annotatedWith(Names.named("effectProcessing"))
            .to(SimpleCreateEntityStrategy::class.java)

        bind(CreateEntityStrategy::class.java)
            .annotatedWith(Names.named("prediction"))
            .to(PredictionCreateEntityStrategy::class.java)
            .`in`(Scopes.SINGLETON)
        bind(PredictedEntityIdGenerator::class.java).to(PredictedEntityIdGeneratorImpl::class.java)
            .`in`(Scopes.SINGLETON)
        bind(Int::class.java).annotatedWith(Names.named("clientInputBufferLength")).toInstance(128)
        bind(LocalPlayer::class.java).toInstance(LocalPlayer())

        bind(DrawableStateProvider::class.java).to(DrawableStateProviderImpl::class.java).`in`(Scopes.SINGLETON)
        bind(Interpolators::class.java).`in`(Scopes.SINGLETON)

        val idsRemapper = IdsRemapperImpl()
        install(BytesSerializationModule(idsRemapper, game.playerInputKClass))
        bind(object : TypeLiteral<
            ClientIncomingDataBuffer<
                BytesStateRecipe,
                BytesDeltaRecipe,
                BytesMetaEntitiesDeltaRecipe
                >>() {})
            .toInstance(ClientIncomingDataBuffer())
        bind(NetworkClient::class.java)
            .to(object :
                TypeLiteral<NetworkClientImpl<
                    BytesStateRecipe,
                    BytesDeltaRecipe,
                    BytesInputRecipe,
                    BytesCreatedEntitiesWithTracesRecipe,
                    BytesMetaEntitiesDeltaRecipe
                    >>() {})
            .`in`(Scopes.SINGLETON)

        bind(ComponentRegistry::class.java).to(ComponentRegistryImpl::class.java).`in`(Scopes.SINGLETON)
    }

    private fun bindNetwork() {
        bind(CottaClientNetworkTransport::class.java)
            .toInstance(KryonetCottaTransportFactory().createClient(game.config.debugConfig.emulatedNetworkConditions))
    }
}
