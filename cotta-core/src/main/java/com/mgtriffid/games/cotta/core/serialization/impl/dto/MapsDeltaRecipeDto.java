package com.mgtriffid.games.cotta.core.serialization.impl.dto;


import com.mgtriffid.games.cotta.core.entities.EntityId;

import java.util.ArrayList;

public class MapsDeltaRecipeDto {
    public ArrayList<MapsEntityRecipeDto> addedEntities;
    public ArrayList<MapsChangedEntityRecipeDto> changedEntities;
    public ArrayList<EntityId> removedEntitiesIds;
}
