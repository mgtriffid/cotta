package com.mgtriffid.games.cotta.core.serialization.impl.dto;

import java.util.ArrayList;

public class MapsEntityRecipeDto {
    public EntityIdDto entityId;
    public EntityOwnedByDto ownedBy;
    public ArrayList<MapComponentRecipeDto> components;
    public ArrayList<String> inputComponents;
}
