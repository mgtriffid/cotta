package com.mgtriffid.games.cotta.network.protocol;

public class ServerToClientDto {
    public long tick;
    public KindOfData kindOfData;
    public byte[] payload;
}
