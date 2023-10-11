package com.mgtriffid.games.cotta.experimental.guice.beans.impl;

import com.mgtriffid.games.cotta.experimental.guice.api.JCottaGame;
import com.mgtriffid.games.cotta.experimental.guice.beans.ClientGhosts;
import com.mgtriffid.games.cotta.experimental.guice.beans.ComponentsRegistry;
import com.mgtriffid.games.cotta.experimental.guice.beans.JCottaDataBoundary;
import com.mgtriffid.games.cotta.experimental.guice.beans.JCottaServerGameInstance;
import com.mgtriffid.games.cotta.experimental.guice.beans.ServerNetwork;
import com.mgtriffid.games.cotta.experimental.guice.beans.ServerSimulation;
import com.mgtriffid.games.cotta.experimental.guice.beans.ServerToClientDataSender;
import com.mgtriffid.games.cotta.experimental.guice.beans.SimulationInputs;
import com.mgtriffid.games.cotta.experimental.guice.beans.state.JCottaState;
import com.mgtriffid.games.cotta.experimental.guice.data.EnterGameIntent;
import com.mgtriffid.games.cotta.experimental.guice.loop.GameLoop;
import jakarta.inject.Inject;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JCottaServerGameInstanceImpl implements JCottaServerGameInstance {
    private final JCottaDataBoundary dataBoundary;
    private final JCottaGame game;
    private final ServerNetwork network;
    private final ServerSimulation simulation;
    private final ServerToClientDataSender serverToClientDataSender;
    private final ComponentsRegistry componentsRegistry;
    private final JCottaState state;
    private final GameLoop loop;
    private final ClientGhosts clientGhosts;
    private final SimulationInputs simulationInputs;

    @Inject
    public JCottaServerGameInstanceImpl(
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
        this.game = game;
        this.dataBoundary = dataBoundary;
        this.network = network;
        this.simulation = simulation;
        this.serverToClientDataSender = serverToClientDataSender;
        this.componentsRegistry = componentsRegistry;
        this.state = state;
        this.loop = loop;
        this.clientGhosts = clientGhosts;
        this.simulationInputs = simulationInputs;
    }

    @Override
    public void run() {
        try {
            network.initialize();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        registerComponents();
        registerSystems();
        loop.start(this::tick);

        game.getConfig().print();
    }

    private void tick() {
        fetchInput();
        simulation.tick();
        dispatchDataToClients();
    }

    private void fetchInput() {
        List<EnterGameIntent> etgIntents = network.drainEnterGameIntents();
        for (EnterGameIntent intent : etgIntents) {
            registerPlayer(intent.connectionId, intent.params);
        }
        simulationInputs.fetch();
    }

    private void registerPlayer(int connectionId, Map<String, String> params) {
        // enter game
        int playerId = simulation.enterGame(params);
        clientGhosts.addGhost(playerId, connectionId);
        // track ghost
    }

    private void registerComponents() {
        for (Class componentClass : game.getComponentClasses()) {
            componentsRegistry.registerClass(componentClass);
        }
        // same for input
    }

    private void registerSystems() {
        for (Class system : game.getServerSystems()) {
            simulation.registerSystem(system);
        }
    }

    private void dispatchDataToClients() {
        serverToClientDataSender.send();
    }
}
