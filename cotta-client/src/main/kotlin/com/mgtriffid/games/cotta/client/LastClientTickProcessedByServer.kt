package com.mgtriffid.games.cotta.client

// TODO suspicious, maybe need to reconsider usage or the whole approach
interface LastClientTickProcessedByServer {
    var tick: Long
}

class LastClientTickProcessedByServerImpl(
    override var tick: Long
) : LastClientTickProcessedByServer
