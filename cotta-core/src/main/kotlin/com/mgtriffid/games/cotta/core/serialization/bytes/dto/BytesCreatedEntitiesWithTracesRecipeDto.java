package com.mgtriffid.games.cotta.core.serialization.bytes.dto;

import com.mgtriffid.games.cotta.core.serialization.dto.EntityIdDto;

import java.util.ArrayList;
import java.util.HashMap;

public class BytesCreatedEntitiesWithTracesRecipeDto {
    public ArrayList<BytesCreateEntityTraceDto> traces;
    public HashMap<EntityIdDto, EntityIdDto> predictedEntitiesIds;
}
