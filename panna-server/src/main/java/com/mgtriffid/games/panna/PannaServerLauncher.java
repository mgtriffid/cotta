package com.mgtriffid.games.panna;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.mgtriffid.games.panna.lobby.PannaLobby;
import org.slf4j.LoggerFactory;

public class PannaServerLauncher {
    public static void main(String[] args) {
        configureLogging();
        new PannaLobby().start();
        // lobby can receive requests to join a room or start a room, so perhaps
        // we first instantiate a Server that runs somehow, then we make it possible
        // to call some methods of Server from Lobby. Like join, stop, create new, etc.

        new Thread(() -> {
            System.out.println("Starting server");
            new PannaServer().start();
        }).start();
    }

    private static void configureLogging() {
        ((Logger) LoggerFactory.getLogger("org.eclipse")).setLevel(Level.INFO);
        ((Logger) LoggerFactory.getLogger("com.mgtriffid.games.cotta.server.impl.invokers")).setLevel(Level.INFO);
        ((Logger) LoggerFactory.getLogger("com.mgtriffid.games.cotta.core.serialization.impl")).setLevel(Level.INFO);
    }
}
