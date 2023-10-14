package com.mgtriffid.games.cotta.experimental.guice.serialization;

import com.mgtriffid.games.cotta.experimental.guice.serialization.recipe.DeltaRecipe;
import com.mgtriffid.games.cotta.experimental.guice.serialization.recipe.StateRecipe;

public interface StateSerialization {
    byte[] serializeDeltaRecipe(DeltaRecipe recipe);

    DeltaRecipe deserializeDeltaRecipe(byte[] bytes);

    byte[] serializeStateRecipe(StateRecipe recipe);

    StateRecipe deserializeStateRecipe(byte[] bytes);
}
