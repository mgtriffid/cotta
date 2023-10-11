package com.mgtriffid.games.cotta.experimental.guice.data;

import java.util.Map;

public class EnterGameIntent {

    public final int connectionId;
    public final Map<String, String> params;

    public EnterGameIntent(int connectionId, Map<String, String> params) {
        this.connectionId = connectionId;
        this.params = params;
    }
}
