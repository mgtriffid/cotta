package com.mgtriffid.games.cotta.experimental.guice.beans;

import com.mgtriffid.games.cotta.experimental.guice.data.EnterGameIntent;

import java.io.IOException;
import java.util.List;

public interface ServerNetwork {
    void initialize() throws IOException;

    List<EnterGameIntent> drainEnterGameIntents();
}
