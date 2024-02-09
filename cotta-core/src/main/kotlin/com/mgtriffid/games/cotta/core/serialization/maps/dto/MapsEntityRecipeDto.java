package com.mgtriffid.games.cotta.core.serialization.maps.dto;

import com.mgtriffid.games.cotta.core.serialization.dto.EntityIdDto;
import com.mgtriffid.games.cotta.core.serialization.dto.EntityOwnedByDto;

import java.util.ArrayList;

public class MapsEntityRecipeDto {
    public EntityIdDto entityId;
    public EntityOwnedByDto ownedBy;
    public ArrayList<MapsComponentRecipeDto> components;
    public ArrayList<String> inputComponents;
}
