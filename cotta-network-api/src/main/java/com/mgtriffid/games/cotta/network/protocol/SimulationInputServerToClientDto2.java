package com.mgtriffid.games.cotta.network.protocol;

public class SimulationInputServerToClientDto2 implements ServerToClientDto2 {
    public long tick;
    public byte[] playersSawTicks;
    public byte[] playersInputs;
    public PlayersDiffDto playersDiff;
    public int idSequence;
}
