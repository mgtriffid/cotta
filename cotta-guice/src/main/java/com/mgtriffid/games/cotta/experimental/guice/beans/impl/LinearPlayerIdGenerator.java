package com.mgtriffid.games.cotta.experimental.guice.beans.impl;

import com.mgtriffid.games.cotta.experimental.guice.beans.PlayerIdGenerator;

import java.util.concurrent.atomic.AtomicInteger;

public class LinearPlayerIdGenerator implements PlayerIdGenerator {

    private final AtomicInteger id = new AtomicInteger(0);

    @Override
    public int id() {
        return id.incrementAndGet();
    }
}
