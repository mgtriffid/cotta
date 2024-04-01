package com.mgtriffid.games.cotta.network.protocol;

import java.util.List;

public class StateServerToClientDto implements ServerToClientDto {
    public long tick;
    public FullStateDto fullState;
    public List<DeltaDto> deltas;
    public int playerId;
    public int[] playerIds;
}
