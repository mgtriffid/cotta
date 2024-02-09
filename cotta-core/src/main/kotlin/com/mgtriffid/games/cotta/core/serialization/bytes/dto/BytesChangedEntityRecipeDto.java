package com.mgtriffid.games.cotta.core.serialization.bytes.dto;

import com.mgtriffid.games.cotta.core.serialization.dto.EntityIdDto;

import java.util.ArrayList;

public class BytesChangedEntityRecipeDto {
    public EntityIdDto entityId;
    public ArrayList<BytesComponentDeltaRecipeDto> changedComponents;
    public ArrayList<BytesComponentRecipeDto> addedComponents;
    public ArrayList<Short> removedComponents; // TODO array?
}
