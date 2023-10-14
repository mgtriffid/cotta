package com.mgtriffid.games.cotta.experimental.guice.serialization;

import com.mgtriffid.games.cotta.experimental.guice.serialization.recipe.InputRecipe;

public interface InputSerialization {
    byte[] serializeInputRecipe(InputRecipe recipe);
    InputRecipe deserializeInputRecipe(byte[] bytes);
}
