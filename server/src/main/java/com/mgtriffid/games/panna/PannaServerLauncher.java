package com.mgtriffid.games.panna;

import com.mgtriffid.games.cotta.CottaServer;

public class PannaServerLauncher {
    public static void main(String[] args) {
        new Thread(() -> {
            System.out.println("Starting server");
            new CottaServer().start();
        }).start();
    }
}
