package com.mgtriffid.games.cotta.experimental.guice.data;

public interface OwnedBy {
    OwnedBy SERVER = new OwnedBy() {};
    class Player {
        public final int id;

        public Player(int id) {
            this.id = id;
        }
    }
}
