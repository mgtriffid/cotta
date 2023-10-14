package com.mgtriffid.games.cotta.experimental.guice.serialization.recipe;

import java.util.List;

public interface DeltaRecipe {
    List<EntityRecipe> getAddedEntities();
    List<ChangedEntityRecipe> getChangedEntities();
    List<Integer> getRemovedEntitiesIds();
}
