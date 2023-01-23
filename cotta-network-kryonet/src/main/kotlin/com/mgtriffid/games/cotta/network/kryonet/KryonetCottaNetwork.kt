package com.mgtriffid.games.cotta.network.kryonet

import com.mgtriffid.games.cotta.network.CottaClientNetwork
import com.mgtriffid.games.cotta.network.CottaNetwork
import com.mgtriffid.games.cotta.network.CottaServerNetwork

class KryonetCottaNetwork : CottaNetwork {
    override fun createServerNetwork(): CottaServerNetwork {
        return KryonetCottaServerNetwork()
    }

    override fun createClientNetwork(): CottaClientNetwork {
        return KryonetCottaClientNetwork()
    }
}
