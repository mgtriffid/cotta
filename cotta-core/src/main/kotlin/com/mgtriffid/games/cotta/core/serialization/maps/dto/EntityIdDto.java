package com.mgtriffid.games.cotta.core.serialization.maps.dto;

public class EntityIdDto {
    public int id;
    public int playerId;
    public Kind kind;

    public enum Kind {
        AUTHORITATIVE,
        PREDICTED,
        STATIC
    }
}
