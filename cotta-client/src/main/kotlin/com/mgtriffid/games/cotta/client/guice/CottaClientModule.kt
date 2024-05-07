package com.mgtriffid.games.cotta.client.guice

import com.codahale.metrics.Histogram
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.SlidingTimeWindowArrayReservoir
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Scopes
import com.google.inject.Singleton
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
import com.mgtriffid.games.cotta.core.GLOBAL
import com.mgtriffid.games.cotta.core.PlayersHandler
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
import com.mgtriffid.games.cotta.core.entities.impl.EntitiesInternal
import com.mgtriffid.games.cotta.core.guice.BytesSerializationModule
import com.mgtriffid.games.cotta.core.input.InputProcessing
import com.mgtriffid.games.cotta.core.registry.ComponentRegistry
import com.mgtriffid.games.cotta.core.registry.impl.ComponentRegistryImpl
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesDeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesInputRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesStateRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesPlayersDeltaRecipe
import com.mgtriffid.games.cotta.core.simulation.Simulation
import com.mgtriffid.games.cotta.core.simulation.EntityOwnerSawTickProvider
import com.mgtriffid.games.cotta.core.simulation.Players
import com.mgtriffid.games.cotta.core.simulation.PlayersSawTicks
import com.mgtriffid.games.cotta.core.simulation.SimulationInputHolder
import com.mgtriffid.games.cotta.core.simulation.impl.AuthoritativeSimulationImpl
import com.mgtriffid.games.cotta.core.simulation.impl.EntityOwnerSawTickProviderImpl
import com.mgtriffid.games.cotta.core.simulation.impl.PlayersImpl
import com.mgtriffid.games.cotta.core.simulation.impl.PlayersSawTickImpl
import com.mgtriffid.games.cotta.core.simulation.impl.SimulationInputHolderImpl
import com.mgtriffid.games.cotta.core.simulation.invokers.*
import com.mgtriffid.games.cotta.core.simulation.invokers.context.CreateEntityStrategy
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EntityProcessingContext
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import com.mgtriffid.games.cotta.core.simulation.invokers.context.impl.*
import com.mgtriffid.games.cotta.network.CottaClientNetworkTransport
import com.mgtriffid.games.cotta.network.kryonet.KryonetCottaTransportFactory
import jakarta.inject.Named
import java.util.concurrent.TimeUnit

class CottaClientModule(
    private val game: CottaGame,
    private val input: CottaClientInput
) : AbstractModule() {
    private val metricRegistry = MetricRegistry()

    override fun configure() {
        bind(CottaGame::class.java).toInstance(game)
        bind(PlayersHandler::class.java).toInstance(game.playersHandler)
        bind(CottaClientInput::class.java).toInstance(input)
        bindNetwork()
        bind(CottaClient::class.java)
            .to(object : TypeLiteral<CottaClientImpl>() {})
        bind(Int::class.java).annotatedWith(Names.named("historyLength"))
            .toInstance(8)
        bind(Int::class.java).annotatedWith(Names.named("stateHistoryLength"))
            .toInstance(128)

        bind(Simulations::class.java).to(SimulationsImpl::class.java)
            .`in`(Scopes.SINGLETON)

        bind(Simulation::class.java).annotatedWith(Names.named("simulation"))
            .to(AuthoritativeSimulationImpl::class.java).`in`(Scopes.SINGLETON)
        bind(Players::class.java).to(PlayersImpl::class.java)
            .`in`(Scopes.SINGLETON)

        bind(Simulation::class.java).annotatedWith(Names.named("guessed"))
            .to(GuessedSimulationImpl::class.java).`in`(Scopes.SINGLETON)

        val simulationTickProvider = AtomicLongTickProvider()
        bind(TickProvider::class.java)
            .annotatedWith(Names.named(SIMULATION))
            .toInstance(simulationTickProvider)
        bind(TickProvider::class.java)
            .annotatedWith(Names.named(GLOBAL))
            .toInstance(AtomicLongTickProvider())
        bind(TickProvider::class.java)
            .annotatedWith(Names.named("guessed"))
            .toInstance(AtomicLongTickProvider())
        bind(CottaClock::class.java).toInstance(
            CottaClockImpl(
                simulationTickProvider,
                game.config.tickLength
            )
        )
        bind(SimulationDirector::class.java).to(SimulationDirectorImpl::class.java)
            .`in`(Scopes.SINGLETON)
        bind(DeltasPresent::class.java).to(DeltasPresentImpl::class.java)
            .`in`(Scopes.SINGLETON)
        bind(Deltas::class.java).to(DeltasImpl::class.java)
            .`in`(Scopes.SINGLETON)
        bind(CottaState::class.java).annotatedWith(Names.named("simulation"))
            .to(CottaStateImpl::class.java)
            .`in`(Scopes.SINGLETON)
        bind(CottaState::class.java).annotatedWith(Names.named("guessed"))
            .to(CottaStateImpl::class.java)
            .`in`(Scopes.SINGLETON)
        bind(SimulationInputHolder::class.java).to(SimulationInputHolderImpl::class.java)
            .`in`(Scopes.SINGLETON)

        bind(InvokersFactory::class.java)
            .annotatedWith(Names.named("simulation"))
            .to(SimulationInvokersFactory::class.java)
            .`in`(Scopes.SINGLETON)

        bind(InvokersFactory::class.java)
            .annotatedWith(Names.named("prediction"))
            .to(PredictionInvokersFactory::class.java)
            .`in`(Scopes.SINGLETON)

        bind(SawTickHolder::class.java).toInstance(SawTickHolder(null))
        bind(EffectBus::class.java).to(EffectBusImpl::class.java)
            .`in`(Scopes.SINGLETON)
        bind(EffectBus::class.java).annotatedWith(Names.named("prediction"))
            .to(EffectBusImpl::class.java)
            .`in`(Scopes.SINGLETON)
        bind(EntityOwnerSawTickProvider::class.java).to(
            EntityOwnerSawTickProviderImpl::class.java
        )
            .`in`(Scopes.SINGLETON)
        bind(Entities::class.java).annotatedWith(Names.named("latest"))
            .to(LatestEntities::class.java)
        bind(EntityProcessingContext::class.java).to(EntityProcessingContextImpl::class.java)
            .`in`(Scopes.SINGLETON)
        bind(EffectProcessingContext::class.java)
            .annotatedWith(Names.named("lagCompensated"))
            .to(LagCompensatingEffectProcessingContext::class.java)
            .`in`(Scopes.SINGLETON)
        bind(EffectProcessingContext::class.java)
            .annotatedWith(Names.named("simple"))
            .to(SimpleEffectProcessingContext::class.java)
            .`in`(Scopes.SINGLETON)
        bind(PlayersSawTicks::class.java).to(PlayersSawTickImpl::class.java)
            .`in`(Scopes.SINGLETON)
        bind(InputProcessing::class.java).toInstance(game.inputProcessing)

        bind(PredictionSimulation::class.java).to(PredictionSimulationImpl::class.java)
            .`in`(Scopes.SINGLETON)
        bind(CottaState::class.java).annotatedWith(Names.named("prediction"))
            .to(CottaStateImpl::class.java)
            .`in`(Scopes.SINGLETON)
        val predictionTickProvider = AtomicLongTickProvider()
        bind(TickProvider::class.java)
            .annotatedWith(Names.named("prediction"))
            .toInstance(predictionTickProvider)
        val predictionClock =
            CottaClockImpl(predictionTickProvider, game.config.tickLength)
        bind(CottaClock::class.java)
            .annotatedWith(Names.named("prediction"))
            .toInstance(predictionClock)
        bind(EntitiesInternal::class.java).annotatedWith(Names.named("prediction"))
            .to(PredictedLatestEntities::class.java)
        bind(Entities::class.java).annotatedWith(Names.named("prediction"))
            .to(PredictedLatestEntities::class.java)
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

        bind(LocalPlayerInputs::class.java).to(LocalPlayerInputsImpl::class.java)
            .`in`(Scopes.SINGLETON)
        bind(CreateEntityStrategy::class.java)
            .annotatedWith(Names.named("effectProcessing"))
            .to(SimpleCreateEntityStrategy::class.java)

        bind(CreateEntityStrategy::class.java)
            .annotatedWith(Names.named("prediction"))
            .to(PredictionCreateEntityStrategy::class.java)
            .`in`(Scopes.SINGLETON)
        bind(PredictedEntityIdGenerator::class.java).to(
            PredictedEntityIdGeneratorImpl::class.java
        )
            .`in`(Scopes.SINGLETON)
        bind(Int::class.java).annotatedWith(Names.named("clientInputBufferLength"))
            .toInstance(128)
        bind(LocalPlayer::class.java).toInstance(LocalPlayer())

        bind(DrawableStateProvider::class.java).to(DrawableStateProviderImpl::class.java)
            .`in`(Scopes.SINGLETON)
        bind(Interpolators::class.java).`in`(Scopes.SINGLETON)

        install(BytesSerializationModule(game.playerInputKClass))
        bind(object : TypeLiteral<
            ClientIncomingDataBuffer<
                BytesStateRecipe,
                BytesDeltaRecipe
                >>() {})
            .toInstance(ClientIncomingDataBuffer())
        bind(NetworkClient::class.java)
            .to(object :
                TypeLiteral<NetworkClientImpl<
                    BytesStateRecipe,
                    BytesDeltaRecipe,
                    BytesInputRecipe,
                    BytesPlayersDeltaRecipe
                    >>() {})
            .`in`(Scopes.SINGLETON)

        bind(ComponentRegistry::class.java).to(ComponentRegistryImpl::class.java)
            .`in`(Scopes.SINGLETON)
        bind(MetricRegistry::class.java).toInstance(metricRegistry)
        bind(PaceRegulator::class.java).to(MetricsAwarePaceRegulator::class.java)
            .`in`(Scopes.SINGLETON)
    }

    @Provides
    @Singleton
    private fun provideIncomingDataBufferMonitor(
        incomingDataBuffer: ClientIncomingDataBuffer<
            BytesStateRecipe,
            BytesDeltaRecipe
            >,
        @Named("global") globalTick: TickProvider,
    ): IncomingDataBufferMonitor {
        val bufferHistogram = Histogram(
            SlidingTimeWindowArrayReservoir(
                2000,
                TimeUnit.MILLISECONDS
            )
        )
        metricRegistry.register("buffer_ahead", bufferHistogram)
        // GROOM idiotic place to declare this histogram, need to reorganize
        val serverBufferHistogram = Histogram(
            SlidingTimeWindowArrayReservoir(
                2000,
                TimeUnit.MILLISECONDS
            )
        )
        metricRegistry.register("server_buffer_ahead", serverBufferHistogram)
        return IncomingDataBufferMonitor(
            incomingDataBuffer,
            globalTick,
            bufferHistogram
        )
    }

    private fun bindNetwork() {
        bind(CottaClientNetworkTransport::class.java)
            .toInstance(
                KryonetCottaTransportFactory(metricRegistry).createClient(
                    game.config.debugConfig.emulatedNetworkConditions,
                    game.config.network.ports.tcp,
                    game.config.network.ports.udp,
                    game.config.network.serverHost
                )
            )
    }
}
