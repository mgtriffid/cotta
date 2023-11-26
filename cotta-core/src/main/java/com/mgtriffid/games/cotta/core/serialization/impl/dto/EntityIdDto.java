package com.mgtriffid.games.cotta.core.serialization.impl.dto;

import org.jetbrains.annotations.NotNull;

public class EntityIdDto {
    public int id;
    public int playerId;
    public Kind kind;

    public enum Kind {
        AUTHORITATIVE,
        PREDICTED
    }
}
