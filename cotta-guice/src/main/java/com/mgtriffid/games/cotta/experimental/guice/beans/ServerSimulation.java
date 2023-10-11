package com.mgtriffid.games.cotta.experimental.guice.beans;

import java.util.Map;

public interface ServerSimulation {
    void registerSystem(Class system);

    int enterGame(Map<String, String> params);

    void tick();
}
