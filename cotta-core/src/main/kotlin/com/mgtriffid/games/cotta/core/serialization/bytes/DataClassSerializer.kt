package com.mgtriffid.games.cotta.core.serialization.bytes

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties

class DataClassSerializer<C: Any>(
    kClass: KClass<C>
) : Serializer<C>() {
    private val fields = kClass.declaredMemberProperties.toTypedArray()
    private val constructor: KCallable<C> = kClass.constructors.first()

    override fun write(kryo: Kryo, output: Output?, `object`: C) {
        fields.forEach { field ->
            // TODO if field is final then writeObjectOrNull or even writeObject
            kryo.writeClassAndObject(output, field.getter.call(`object`))
        }
    }

    override fun read(kryo: Kryo, input: Input?, type: Class<out C>?): C {
        return constructor.call(*Array(fields.size) {
            kryo.readClassAndObject(input)
        })
    }
}
