package com.mgtriffid.games.cotta.experimental.guice.serialization.recipe;

import java.util.List;

public interface EntityInputRecipe {
    int getEntityId();

    List<InputComponentRecipe> getInputComponents();
}
