package com.mgtriffid.games.panna;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.mgtriffid.games.panna.lobby.PannaLobby;
import org.slf4j.LoggerFactory;

public class PannaServerLauncher {
    public static void main(String[] args) {
        configureLogging();
        new PannaLobby().start();
/*
        new Thread(() -> {
            System.out.println("Starting server");
            new PannaServer().start();
        }).start();
*/
    }

    private static void configureLogging() {
        ((Logger) LoggerFactory.getLogger("org.eclipse")).setLevel(Level.INFO);
    }
}
