package com.mgtriffid.games.cotta.experimental.guice.serialization.recipe;

import com.mgtriffid.games.cotta.experimental.guice.data.ComponentKey;

import java.util.List;

public interface ChangedEntityRecipe {
    int getEntityId();

    List<ComponentDeltaRecipe> getChangedComponents();

    List<ComponentRecipe> getAddedComponents();

    List<ComponentKey> getRemovedComponents();
}
