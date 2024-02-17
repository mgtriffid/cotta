package com.mgtriffid.games.cotta.core.serialization.bytes

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.id.PredictedEntityId
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

class DataClassSerializer<C: Any>(
    kClass: KClass<C>
) : Serializer<C>() {
    private val parameters = kClass.primaryConstructor!!.parameters
    private val fields = kClass.run {
        val properties = declaredMemberProperties.toTypedArray()
        parameters.map { parameter ->
            properties.find { it.name == parameter.name }!!
        }.toTypedArray()
    }
    private val constructor: KCallable<C> = kClass.constructors.first()

    override fun write(kryo: Kryo, output: Output?, `object`: C) {
        fields.forEach { field ->
            kryo.writeClassAndObject(output, field.getter.call(`object`))
        }
    }

    override fun read(kryo: Kryo, input: Input?, type: Class<out C>?): C {
        return constructor.call(*Array(fields.size) {
            kryo.readClassAndObject(input)
        })
    }
}

fun main() {
    val kryo = Kryo()
    kryo.register(PlayerId::class.java, DataClassSerializer(PlayerId::class))
    kryo.register(PredictedEntityId::class.java, DataClassSerializer(PredictedEntityId::class))
    val playerId = PlayerId(1)
    val output = Output(1024)
    kryo.writeClassAndObject(output, PredictedEntityId(playerId, 4))
    val input = Input(output.toBytes())
    val deserializedId = kryo.readClassAndObject(input)
    println(deserializedId)

}
