package com.mgtriffid.games.cotta.network.kryonet

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.serializers.CollectionSerializer
import com.esotericsoftware.kryo.serializers.MapSerializer
import com.mgtriffid.games.cotta.network.CottaClientNetwork
import com.mgtriffid.games.cotta.network.CottaNetwork
import com.mgtriffid.games.cotta.network.CottaServerNetwork
import com.mgtriffid.games.cotta.network.protocol.EnterTheGameDto

class KryonetCottaNetwork : CottaNetwork {
    override fun createServerNetwork(): CottaServerNetwork {
        return KryonetCottaServerNetwork()
    }

    override fun createClientNetwork(): CottaClientNetwork {
        return KryonetCottaClientNetwork()
    }
}

fun Kryo.registerClasses() {
    register(EnterTheGameDto::class.java)
    register(ArrayList::class.java, CollectionSerializer<ArrayList<Any?>>())
    register(HashMap::class.java, MapSerializer<HashMap<String, Any?>>())
    register(LinkedHashMap::class.java, MapSerializer<LinkedHashMap<String, Any?>>())
}
