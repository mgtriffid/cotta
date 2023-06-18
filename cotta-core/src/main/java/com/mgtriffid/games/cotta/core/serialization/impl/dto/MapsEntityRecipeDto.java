package com.mgtriffid.games.cotta.core.serialization.impl.dto;

import com.mgtriffid.games.cotta.core.entities.EntityId;

import java.util.ArrayList;

public class MapsEntityRecipeDto {
    public EntityIdDto entityId;
    public EntityOwnedByDto ownedBy;
    public ArrayList<MapComponentRecipeDto> components;
}
