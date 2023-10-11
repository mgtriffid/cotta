package com.mgtriffid.games.cotta.experimental.guice.beans.impl;

import com.mgtriffid.games.cotta.experimental.guice.beans.ClientGhosts;
import com.mgtriffid.games.cotta.experimental.guice.data.ClientGhost;

import java.util.HashMap;

public class ClientGhostsImpl implements ClientGhosts {
    final HashMap<Integer, Integer> playerByConnection = new HashMap<>();
    final HashMap<Integer, ClientGhost> data = new HashMap<>();
    @Override
    public void addGhost(int playerId, int connectionId) {
        data.put(playerId, new ClientGhost(connectionId));
        playerByConnection.put(connectionId, playerId);
    }
}
