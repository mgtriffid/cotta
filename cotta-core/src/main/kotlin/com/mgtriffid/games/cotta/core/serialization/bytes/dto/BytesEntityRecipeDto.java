package com.mgtriffid.games.cotta.core.serialization.bytes.dto;

import com.mgtriffid.games.cotta.core.serialization.dto.EntityIdDto;
import com.mgtriffid.games.cotta.core.serialization.dto.EntityOwnedByDto;

import java.util.ArrayList;

public class BytesEntityRecipeDto {
    public EntityIdDto entityId;
    public EntityOwnedByDto ownedBy;
    public ArrayList<BytesComponentRecipeDto> components;
}
