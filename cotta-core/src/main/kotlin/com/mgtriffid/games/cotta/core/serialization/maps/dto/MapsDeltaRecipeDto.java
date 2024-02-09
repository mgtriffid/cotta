package com.mgtriffid.games.cotta.core.serialization.maps.dto;


import com.mgtriffid.games.cotta.core.serialization.dto.EntityIdDto;

import java.util.ArrayList;

public class MapsDeltaRecipeDto {
    public ArrayList<MapsEntityRecipeDto> addedEntities;
    public ArrayList<MapsChangedEntityRecipeDto> changedEntities;
    public ArrayList<EntityIdDto> removedEntitiesIds;
}
