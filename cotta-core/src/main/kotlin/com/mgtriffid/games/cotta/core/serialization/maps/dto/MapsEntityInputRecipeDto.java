package com.mgtriffid.games.cotta.core.serialization.maps.dto;

import com.mgtriffid.games.cotta.core.serialization.dto.EntityIdDto;

import java.util.ArrayList;

public class MapsEntityInputRecipeDto {
    public EntityIdDto entityId;
    public ArrayList<MapsInputComponentRecipeDto> components;
}
