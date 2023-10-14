package com.mgtriffid.games.cotta.experimental.guice.serialization.maps.recipe;

import com.mgtriffid.games.cotta.experimental.guice.data.ComponentKey;
import com.mgtriffid.games.cotta.experimental.guice.serialization.recipe.ComponentRecipe;

import java.util.Map;

public class MapComponentRecipe implements ComponentRecipe {
    public final ComponentKey componentKey;
    public final Map<String, Object> data;

    public MapComponentRecipe(ComponentKey componentKey, Map<String, Object> data) {
        this.componentKey = componentKey;
        this.data = data;
    }
}
