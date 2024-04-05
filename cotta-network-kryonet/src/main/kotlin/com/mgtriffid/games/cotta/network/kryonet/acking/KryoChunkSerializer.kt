package com.mgtriffid.games.cotta.network.kryonet.acking

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output

class KryoChunkSerializer(private val kryo: Kryo) : ChunkSerializer {
    override fun serialize(objs: ArrayList<Any>): ByteArray {
        return Output(1024 * 1024).also { output ->
            kryo.writeClassAndObject(output, objs)
        }.toBytes()
    }

    override fun deserialize(bytes: ByteArray): ArrayList<*> {
        val input = Input(bytes)
        return kryo.readClassAndObject(input) as ArrayList<*>
    }
}
