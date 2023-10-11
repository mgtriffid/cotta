package com.mgtriffid.games.cotta.experimental.guice.beans.state.impl;

import com.mgtriffid.games.cotta.experimental.guice.beans.state.JEntities;

public class JEntitiesImpl implements JEntities {
    @Override
    public JEntities deepCopy() {
        return this;
    }
}
