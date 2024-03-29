package com.mgtriffid.games.cotta.client

interface LastClientTickProcessedByServer {
    var tick: Long
}

class LastClientTickProcessedByServerImpl(
    override var tick: Long
) : LastClientTickProcessedByServer
