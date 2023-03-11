package com.mgtriffid.games.cotta.core.serialization.impl.dto;

import java.util.ArrayList;

public class MapsChangedEntityRecipeDto {
    public int entityId;
    public ArrayList<MapComponentDeltaRecipeDto> changedComponents;
    public ArrayList<MapComponentRecipeDto> addedComponents;
    public ArrayList<String> removedComponents;
}