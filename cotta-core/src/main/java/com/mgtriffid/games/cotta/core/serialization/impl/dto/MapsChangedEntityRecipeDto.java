package com.mgtriffid.games.cotta.core.serialization.impl.dto;

import com.mgtriffid.games.cotta.core.entities.EntityId;

import java.util.ArrayList;

public class MapsChangedEntityRecipeDto {
    public EntityId entityId;
    public ArrayList<MapComponentDeltaRecipeDto> changedComponents;
    public ArrayList<MapComponentRecipeDto> addedComponents;
    public ArrayList<String> removedComponents;
}
