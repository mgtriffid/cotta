package com.mgtriffid.games.cotta.core.serialization.bytes.dto;

import com.mgtriffid.games.cotta.core.serialization.dto.EntityIdDto;

import java.util.ArrayList;

public class BytesEntityInputRecipeDto {
    public EntityIdDto entityId;
    public ArrayList<BytesInputComponentRecipeDto> components;
}
