package com.mgtriffid.games.cotta.network.kryonet

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.serializers.CollectionSerializer
import com.esotericsoftware.kryo.serializers.MapSerializer
import com.mgtriffid.games.cotta.core.entities.id.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.network.protocol.*

fun Kryo.registerClasses() {
    register(EnterTheGameDto::class.java)
    register(ClientToServerInputDto::class.java)
    register(ClientToServerInputDto2::class.java)
    register(ClientToServerCreatedPredictedEntitiesDto::class.java)
    register(ServerToClientDto::class.java)
    register(ByteArray::class.java)
    register(KindOfData::class.java)
    register(ArrayList::class.java, CollectionSerializer<ArrayList<Any?>>())
    register(HashMap::class.java, MapSerializer<HashMap<String, Any?>>())
    register(LinkedHashMap::class.java, MapSerializer<LinkedHashMap<String, Any?>>())
    register(EntityId::class.java)
    register(AuthoritativeEntityId::class.java)
}
