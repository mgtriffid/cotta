package com.mgtriffid.games.cotta.experimental.guice.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.mgtriffid.games.cotta.experimental.guice.api.JCottaGame;
import com.mgtriffid.games.cotta.experimental.guice.beans.ClientGhosts;
import com.mgtriffid.games.cotta.experimental.guice.beans.ComponentsRegistry;
import com.mgtriffid.games.cotta.experimental.guice.beans.EffectBus;
import com.mgtriffid.games.cotta.experimental.guice.beans.JCottaDataBoundary;
import com.mgtriffid.games.cotta.experimental.guice.beans.JCottaServerGameInstance;
import com.mgtriffid.games.cotta.experimental.guice.beans.ServerToClientDataSender;
import com.mgtriffid.games.cotta.experimental.guice.beans.PlayerIdGenerator;
import com.mgtriffid.games.cotta.experimental.guice.beans.ServerNetwork;
import com.mgtriffid.games.cotta.experimental.guice.beans.ServerSimulation;
import com.mgtriffid.games.cotta.experimental.guice.beans.SimulationInputs;
import com.mgtriffid.games.cotta.experimental.guice.beans.impl.ClientGhostsImpl;
import com.mgtriffid.games.cotta.experimental.guice.beans.impl.ComponentsRegistryImpl;
import com.mgtriffid.games.cotta.experimental.guice.beans.impl.JCottaDataBoundaryImpl;
import com.mgtriffid.games.cotta.experimental.guice.beans.impl.JCottaServerGameInstanceImpl;
import com.mgtriffid.games.cotta.experimental.guice.beans.SimulationTick;
import com.mgtriffid.games.cotta.experimental.guice.beans.impl.ServerToClientDataSenderImpl;
import com.mgtriffid.games.cotta.experimental.guice.beans.impl.KryonetServerNetwork;
import com.mgtriffid.games.cotta.experimental.guice.beans.impl.LinearPlayerIdGenerator;
import com.mgtriffid.games.cotta.experimental.guice.beans.impl.NoOpEffectBusImpl;
import com.mgtriffid.games.cotta.experimental.guice.beans.impl.ServerSimulationImpl;
import com.mgtriffid.games.cotta.experimental.guice.beans.impl.SimulationInputsImpl;
import com.mgtriffid.games.cotta.experimental.guice.beans.state.JCottaState;
import com.mgtriffid.games.cotta.experimental.guice.beans.state.impl.JCottaStateImpl;
import com.mgtriffid.games.cotta.experimental.guice.loop.GameLoop;
import com.mgtriffid.games.cotta.experimental.guice.loop.impl.FixedRateGameLoop;
import com.mgtriffid.games.cotta.experimental.guice.scopes.GameInstanceScope;
import com.mgtriffid.games.cotta.experimental.guice.scopes.GameInstanceScoped;
import jakarta.inject.Named;

public class CottaServerModule extends AbstractModule {
    private final GameInstanceScope gameInstanceScope = new GameInstanceScope();
    private final JCottaGame game;

    public CottaServerModule(JCottaGame game) {
        this.game = game;
    }

    @Override
    protected void configure() {
        bindScope(GameInstanceScoped.class, gameInstanceScope);
    }

    @Provides
    @GameInstanceScoped
    SimulationTick provideSimulationTick() {
        return new SimulationTick();
    }

    @Provides
    @Singleton
    JCottaDataBoundary provideJCottaDataBoundary() {
        return new JCottaDataBoundaryImpl();
    }

    @Provides
    @Named("gameInstanceScope")
    GameInstanceScope provideGameInstanceScope() {
        return gameInstanceScope;
    }

    @Provides
    @GameInstanceScoped
    ServerNetwork provideServerNetwork() {
        return new KryonetServerNetwork();
    }

    @Provides
    @GameInstanceScoped
    ServerSimulation provideServerSimulation(
            JCottaState state,
            PlayerIdGenerator playerIdGenerator,
            SimulationTick tick,
            EffectBus effectBus
    ) {
        return new ServerSimulationImpl(
                state,
                game.getMetaEntitiesInputComponents(),
                playerIdGenerator,
                tick,
                effectBus
        );
    }

    @Provides
    @GameInstanceScoped
    PlayerIdGenerator providePlayerIdGenerator() {
        return new LinearPlayerIdGenerator();
    }

    @Provides
    @GameInstanceScoped
    ServerToClientDataSender provideServerToClientDataSender() {
        return new ServerToClientDataSenderImpl();
    }

    @Provides
    @Singleton
    ComponentsRegistry provideComponentsRegistry() {
        return new ComponentsRegistryImpl();
    }

    @Provides
    @Singleton
    JCottaGame provideGame() {
        return this.game;
    }

    @Provides
    @GameInstanceScoped
    JCottaServerGameInstance provideJCottaServerGameInstance(
            JCottaGame game,
            JCottaDataBoundary dataBoundary,
            ServerNetwork network,
            ServerSimulation simulation,
            ServerToClientDataSender serverToClientDataSender,
            ComponentsRegistry componentsRegistry,
            JCottaState state,
            GameLoop loop,
            ClientGhosts clientGhosts,
            SimulationInputs simulationInputs
    ) {
        return new JCottaServerGameInstanceImpl(
                game,
                dataBoundary,
                network,
                simulation,
                serverToClientDataSender,
                componentsRegistry,
                state,
                loop,
                clientGhosts,
                simulationInputs
        );
    }

    @Provides
    @GameInstanceScoped
    JCottaState provideJCottaState(
            JCottaGame game,
            SimulationTick tick
    ) {
        JCottaState state = new JCottaStateImpl(
                game.getConfig().getHistoryLength(),
                tick
        );
        game.initializeServerState(state);
        System.out.println("Initializing server state");
        return state;
    }

    @Provides
    @GameInstanceScoped
    GameLoop provideGameLoop() {
        return new FixedRateGameLoop(game.getConfig().getTickLengthMs());
    }

    @Provides
    @GameInstanceScoped
    ClientGhosts provideClientGhosts() {
        return new ClientGhostsImpl();
    }

    @Provides
    @GameInstanceScoped
    EffectBus provideEffectBus() {
        return new NoOpEffectBusImpl();
    }

    @Provides @GameInstanceScoped
    SimulationInputs provideSimulationInputs() {
        return new SimulationInputsImpl();
    }
}
