package com.mgtriffid.games.cotta.experimental.guice.api;

public interface JCottaGameConfiguration {
    void print();
    int getHistoryLength();

    long getTickLengthMs();
}
