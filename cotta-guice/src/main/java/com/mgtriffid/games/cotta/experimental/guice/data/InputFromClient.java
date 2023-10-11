package com.mgtriffid.games.cotta.experimental.guice.data;


public class InputFromClient {
    private final int connectionId;
    private final ClientInputDto clientInputDto;

    public InputFromClient(
            int connectionId,
            ClientInputDto clientInputDto
    ) {
        this.connectionId = connectionId;
        this.clientInputDto = clientInputDto;
    }
}
