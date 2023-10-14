package com.mgtriffid.games.cotta.experimental.guice.serialization.recipe;

import com.mgtriffid.games.cotta.experimental.guice.data.ComponentKey;
import com.mgtriffid.games.cotta.experimental.guice.data.OwnedBy;

import java.util.List;

public interface EntityRecipe {
    int getEntityId();

    OwnedBy getOwnedBy();

    List<ComponentRecipe> getComponents();

    List<ComponentKey> getInputComponents();
}
