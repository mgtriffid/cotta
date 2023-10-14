package com.mgtriffid.games.cotta.experimental.guice.serialization.maps.recipe;

import com.mgtriffid.games.cotta.experimental.guice.data.ComponentKey;
import com.mgtriffid.games.cotta.experimental.guice.serialization.recipe.ChangedEntityRecipe;
import com.mgtriffid.games.cotta.experimental.guice.serialization.recipe.ComponentDeltaRecipe;
import com.mgtriffid.games.cotta.experimental.guice.serialization.recipe.ComponentRecipe;

import java.util.List;

public class MapsChangedEntityRecipe implements ChangedEntityRecipe {
    private final int entityId;
    private final List<MapComponentDeltaRecipe> changedComponents;

    public MapsChangedEntityRecipe(int entityId, List<MapComponentDeltaRecipe> changedComponents) {
        this.entityId = entityId;
        this.changedComponents = changedComponents;
    }

    @Override
    public int getEntityId() {
        return entityId;
    }

    @Override
    public List<ComponentDeltaRecipe> getChangedComponents() {
        throw new RuntimeException("TODO");
    }

    @Override
    public List<ComponentRecipe> getAddedComponents() {
        return null;
    }

    @Override
    public List<ComponentKey> getRemovedComponents() {
        return null;
    }
}
