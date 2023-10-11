package com.mgtriffid.games.cotta.experimental.guice.loop.impl;

import com.mgtriffid.games.cotta.experimental.guice.loop.GameLoop;


public class FixedRateGameLoop implements GameLoop {
    private final long tickLengthMs;
    private long nextTickAt;
    public boolean isRunning = true;

    public FixedRateGameLoop(long tickLengthMs) {
        this.tickLengthMs = tickLengthMs;
    }

    @Override
    public void start(Runnable block) {
        nextTickAt = System.currentTimeMillis();
        do {
            System.out.println("In loop");
            nextTickAt = nextTickAt + tickLengthMs;
            sleepIfNeeded();
            block.run();
        } while (isRunning);
    }

    private void sleepIfNeeded() {
        long delay = nextTickAt - System.currentTimeMillis();
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                throw new RuntimeException(e); // TODO handle gracefully
            }
        }
    }
}
