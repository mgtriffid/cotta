package com.mgtriffid.games.cotta.network.kryonet.acking

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output

class KryoChunkSerializer : ChunkSerializer {
    val kryoSer = Kryo()
    val kryoDeser = Kryo()

    override fun serialize(objs: ArrayList<Any>): ByteArray {
        return Output(1024 * 1024).also { output ->
            kryoSer.writeClassAndObject(output, objs)
        }.toBytes()
    }

    override fun deserialize(bytes: ByteArray): ArrayList<*> {
        val input = Input(bytes)
        return kryoDeser.readClassAndObject(input) as ArrayList<*>
    }
}
