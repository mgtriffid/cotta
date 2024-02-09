package com.mgtriffid.games.cotta.core.serialization.bytes.dto;

import com.mgtriffid.games.cotta.core.serialization.dto.EntityIdDto;

import java.util.ArrayList;

public class BytesDeltaRecipeDto {
    public ArrayList<BytesEntityRecipeDto> addedEntities;
    public ArrayList<BytesChangedEntityRecipeDto> changedEntities;
    public ArrayList<EntityIdDto> removedEntitiesIds;
}
