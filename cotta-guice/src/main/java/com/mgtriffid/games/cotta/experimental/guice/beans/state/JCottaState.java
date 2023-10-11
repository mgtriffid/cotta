package com.mgtriffid.games.cotta.experimental.guice.beans.state;

public interface JCottaState {
    JEntities getEntities();
    JEntities getEntities(long atTick);

    void advance(long tick);
}
