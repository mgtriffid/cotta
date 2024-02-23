package com.mgtriffid.games.cotta.network.kryonet

import com.mgtriffid.games.cotta.network.CottaClientNetworkTransport
import com.mgtriffid.games.cotta.network.CottaServerNetworkTransport
import com.mgtriffid.games.cotta.network.kryonet.client.KryonetCottaClientNetworkTransport
import com.mgtriffid.games.cotta.network.kryonet.client.SimpleSaver
import com.mgtriffid.games.cotta.network.kryonet.client.SimpleSender

class KryonetCottaTransportFactory {
    fun createClient() : CottaClientNetworkTransport {
        return KryonetCottaClientNetworkTransport(SimpleSender(), SimpleSaver())
    }

    fun createServer() : CottaServerNetworkTransport {
        return KryonetCottaServerNetworkTransport()
    }
}
