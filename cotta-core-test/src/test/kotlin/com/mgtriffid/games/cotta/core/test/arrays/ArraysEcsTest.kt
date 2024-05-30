package com.mgtriffid.games.cotta.core.test.arrays

import com.esotericsoftware.kryo.Kryo
import com.mgtriffid.games.cotta.core.entities.arrays.ArraysBasedState
import com.mgtriffid.games.cotta.core.registry.impl.ComponentRegistryImpl
import com.mgtriffid.games.cotta.core.registry.registerComponents
import com.mgtriffid.games.cotta.core.test.workload.GameStub
import org.junit.jupiter.api.BeforeEach

open class ArraysEcsTest {
    protected lateinit var state: ArraysBasedState

    @BeforeEach
    fun setUp() {
        val registry = ComponentRegistryImpl(Kryo())
        registerComponents(GameStub, registry)
        state = ArraysBasedState(registry)
        registry.getAllComponents().forEach { (key, kClass) ->
            state.componentsStorage.register(key, kClass)
        }
    }
}
