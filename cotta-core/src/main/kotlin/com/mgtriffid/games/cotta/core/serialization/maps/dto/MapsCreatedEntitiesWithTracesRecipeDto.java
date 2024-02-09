package com.mgtriffid.games.cotta.core.serialization.maps.dto;


import com.mgtriffid.games.cotta.core.serialization.dto.EntityIdDto;

import java.util.ArrayList;
import java.util.HashMap;

public class MapsCreatedEntitiesWithTracesRecipeDto {
    public ArrayList<MapsCreateEntityTraceDto> traces;
    public HashMap<EntityIdDto, EntityIdDto> predictedEntitiesIds;
}
