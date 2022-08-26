package com.mgtriffid.games.panna;

public class PannaServerLauncher {
    public static void main(String[] args) {
        new Thread(() -> {
            System.out.println("Starting server");
            new PannaServer().start();
        }).start();
    }
}
