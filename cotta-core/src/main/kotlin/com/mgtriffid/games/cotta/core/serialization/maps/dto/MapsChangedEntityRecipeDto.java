package com.mgtriffid.games.cotta.core.serialization.maps.dto;

import com.mgtriffid.games.cotta.core.serialization.dto.EntityIdDto;

import java.util.ArrayList;

public class MapsChangedEntityRecipeDto {
    public EntityIdDto entityId;
    public ArrayList<MapsComponentDeltaRecipeDto> changedComponents;
    public ArrayList<MapsComponentRecipeDto> addedComponents;
    public ArrayList<String> removedComponents;
}
