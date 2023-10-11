package com.mgtriffid.games.cotta.experimental.guice.beans.state.impl;

import com.mgtriffid.games.cotta.experimental.guice.beans.SimulationTick;
import com.mgtriffid.games.cotta.experimental.guice.beans.state.JCottaState;
import com.mgtriffid.games.cotta.experimental.guice.beans.state.JEntities;

public class JCottaStateImpl implements JCottaState {
    private final int historyLength;
    private final SimulationTick tick;
    private final JEntities[] entitiesArray;

    public JCottaStateImpl(int historyLength, SimulationTick tick) {
        this.historyLength = historyLength;
        this.tick = tick;
        entitiesArray = new JEntities[historyLength];
        entitiesArray[0] = new JEntitiesImpl();
    }

    @Override
    public JEntities getEntities() {
        return getEntities(tick.tick);
    }

    @Override
    public JEntities getEntities(long atTick) {
        validateTickWithinRange(atTick);
        return entitiesArray[(int) (atTick % historyLength)];
    }

    @Override
    public void advance(long tick) {
        JEntities entities = getEntities(tick);
        entitiesArray[(int) ((tick + 1) % historyLength)] = entities.deepCopy();
    }

    private void validateTickWithinRange(long atTick) {
        if (atTick > tick.tick) {
            throw new IllegalStateException("Cannot retrieve entities at tick " + atTick + ": current tick is " + tick.tick);
        }
        if (atTick < tick.tick - historyLength) {
            throw new IllegalStateException(
                    "Cannot retrieve entities at tick " + atTick + ": current tick is " + tick.tick + " while history length is " + historyLength
            );
        }
    }
}
