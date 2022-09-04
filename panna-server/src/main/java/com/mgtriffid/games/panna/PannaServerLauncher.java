package com.mgtriffid.games.panna;

import com.mgtriffid.games.panna.lobby.PannaLobby;

public class PannaServerLauncher {
    public static void main(String[] args) {
        new PannaLobby().start();
/*
        new Thread(() -> {
            System.out.println("Starting server");
            new PannaServer().start();
        }).start();
*/
    }
}
