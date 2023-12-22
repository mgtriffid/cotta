package com.mgtriffid.games.cotta.core.serialization.impl.dto;


import java.util.ArrayList;
import java.util.HashMap;

public class CreatedEntitiesWithTracesRecipeDto {
    public ArrayList<CreateEntityTraceDto> traces;
    public HashMap<EntityIdDto, EntityIdDto> predictedEntitiesIds;
}
