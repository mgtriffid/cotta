package com.mgtriffid.games.cotta.core.serialization.maps.dto;

import com.mgtriffid.games.cotta.core.serialization.dto.EntityIdDto;
import com.mgtriffid.games.cotta.core.serialization.dto.TraceElementDtoKind;

public class MapsCottaTraceElementDto {
    public TraceElementDtoKind kind;
    public MapsEffectRecipeDto data;
    public EntityIdDto entityId;
}
