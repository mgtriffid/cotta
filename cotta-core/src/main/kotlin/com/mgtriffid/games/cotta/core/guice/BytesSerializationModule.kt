package com.mgtriffid.games.cotta.core.guice

import com.esotericsoftware.kryo.Kryo
import com.google.inject.Binder
import com.google.inject.Module
import com.google.inject.Scopes
import com.google.inject.name.Names.named
import com.mgtriffid.games.cotta.core.serialization.bytes.BytesStateSnapper

class BytesSerializationModule : Module {
    override fun configure(binder: Binder) {
        with(binder) {
            val kryo = Kryo()
            bind(Kryo::class.java).annotatedWith(named("snapper")).toInstance(kryo)
            bind(BytesStateSnapper::class.java).`in`(Scopes.SINGLETON)

        }
    }
}
