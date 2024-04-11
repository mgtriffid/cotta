package com.mgtriffid.games.cotta.network.protocol;

public class SimulationInputServerToClientDto implements ServerToClientDto {
    public long tick;
    public byte[] playersSawTicks;
    public byte[] playersInputs;
    public PlayersDiffDto playersDiff;
    public int idSequence;
    public int confirmedClientInput;
}
