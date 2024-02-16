package com.mgtriffid.games.cotta.core.serialization.bytes

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output

class ObjectSerializer<C: Any>(private val obj: C) : Serializer<C>() {
    override fun write(kryo: Kryo, output: Output, `object`: C) {

    }

    override fun read(kryo: Kryo, input: Input, type: Class<out C>): C {
        return obj
    }
}
