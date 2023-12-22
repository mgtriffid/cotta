package com.mgtriffid.games.cotta.core.serialization.impl.dto;


public class CottaTraceElementDto {
    public Kind kind;
    public MapEffectRecipeDto data;
    public EntityIdDto entityId;

    public enum Kind {
        INPUT,
        EFFECT,
        ENTITY_PROCESSING
    }
}
