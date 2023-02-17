package com.mgtriffid.games.cotta.network.idiotic;

import java.util.List;

public class ServerToClientDeltaDto implements ServerToClientDto {
    public List<Integer> removedEntityIds;
    public List<EntityDto> addedEntities;
    public List<EntityDeltaDto> modifiedEntities;
}
