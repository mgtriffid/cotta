package com.mgtriffid.games.cotta.core.serialization.bytes.dto;

import com.mgtriffid.games.cotta.core.serialization.dto.EntityIdDto;
import com.mgtriffid.games.cotta.core.serialization.dto.TraceElementDtoKind;

public class BytesCottaTraceElementDto {
    public TraceElementDtoKind kind;
    public BytesEffectRecipeDto data;
    public EntityIdDto entityId;
}
