package com.mgtriffid.games.cotta.experimental.guice.beans.impl;

import com.mgtriffid.games.cotta.experimental.guice.beans.EffectBus;
import com.mgtriffid.games.cotta.experimental.guice.beans.PlayerIdGenerator;
import com.mgtriffid.games.cotta.experimental.guice.beans.ServerSimulation;
import com.mgtriffid.games.cotta.experimental.guice.beans.SimulationTick;
import com.mgtriffid.games.cotta.experimental.guice.beans.state.JCottaState;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerSimulationImpl implements ServerSimulation {
    private final JCottaState state;
    private final List<Class> metaEntitiesInputComponents;
    private final PlayerIdGenerator playerIdGenerator;
    private final SimulationTick tick;
    private final EffectBus effectBus;
    private final Map<Integer, Map<String, String>> enterGameIntents = new HashMap<>();

    public ServerSimulationImpl(
            JCottaState state,
            List<Class> metaEntitiesInputComponents,
            PlayerIdGenerator playerIdGenerator,
            SimulationTick tick,
            EffectBus effectBus
    ) {
        this.state = state;
        this.metaEntitiesInputComponents = new ArrayList(metaEntitiesInputComponents);
        this.playerIdGenerator = playerIdGenerator;
        this.tick = tick;
        this.effectBus = effectBus;
    }

    @Override
    public void registerSystem(Class system) {

    }

    @Override
    public int enterGame(Map<String, String> params) {
        int playerId = playerIdGenerator.id();
        enterGameIntents.put(playerId, params);
        return playerId;
    }

    @Override
    public void tick() {
        effectBus.clear();
        state.advance(tick.tick);
        tick.tick++;
        putInputIntoEntities();
        invokeSystems();
        processEnterGameIntents();
    }

    private void putInputIntoEntities() {
        state.getEntities(); // and we put input in
    }
    private void invokeSystems() {

    }

    private void processEnterGameIntents() {

    }
}
