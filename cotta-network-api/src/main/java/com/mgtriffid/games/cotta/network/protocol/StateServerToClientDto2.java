package com.mgtriffid.games.cotta.network.protocol;

import java.util.List;

public class StateServerToClientDto2 implements ServerToClientDto2 {
    public long tick;
    public FullStateDto fullState;
    public List<DeltaDto> deltas;
    public int playerId;
}
