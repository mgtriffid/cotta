package com.mgtriffid.games.cotta.core.serialization.maps.dto;


import java.util.ArrayList;

public class MapsDeltaRecipeDto {
    public ArrayList<MapsEntityRecipeDto> addedEntities;
    public ArrayList<MapsChangedEntityRecipeDto> changedEntities;
    public ArrayList<EntityIdDto> removedEntitiesIds;
}
