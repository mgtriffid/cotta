package com.mgtriffid.games.cotta.experimental.guice.serialization.maps;

import com.esotericsoftware.kryo.Kryo;
import com.mgtriffid.games.cotta.experimental.guice.serialization.StateSerialization;
import com.mgtriffid.games.cotta.experimental.guice.serialization.recipe.DeltaRecipe;
import com.mgtriffid.games.cotta.experimental.guice.serialization.recipe.StateRecipe;

public class MapsStateSerialization implements StateSerialization {
    private final Kryo kryo = new Kryo();

    public MapsStateSerialization() {
        // kryo.register
    }

    @Override
    public byte[] serializeDeltaRecipe(DeltaRecipe recipe) {
        return new byte[0];
    }

    @Override
    public DeltaRecipe deserializeDeltaRecipe(byte[] bytes) {
        return null;
    }

    @Override
    public byte[] serializeStateRecipe(StateRecipe recipe) {
        return new byte[0];
    }

    @Override
    public StateRecipe deserializeStateRecipe(byte[] bytes) {
        return null;
    }
}
