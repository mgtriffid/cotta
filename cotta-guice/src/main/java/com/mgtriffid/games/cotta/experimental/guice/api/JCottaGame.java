package com.mgtriffid.games.cotta.experimental.guice.api;

import com.mgtriffid.games.cotta.experimental.guice.beans.state.JCottaState;

import java.util.List;

public interface JCottaGame {
    JCottaGameConfiguration getConfig();

    List<Class> getComponentClasses();

    List<Class> getMetaEntitiesInputComponents();

    List<Class> getServerSystems();

    void initializeServerState(JCottaState state);
}
