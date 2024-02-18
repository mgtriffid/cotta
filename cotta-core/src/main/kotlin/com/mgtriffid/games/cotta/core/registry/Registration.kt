package com.mgtriffid.games.cotta.core.registry

import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.InputComponent
import kotlin.reflect.KClass

fun registerComponents(game: CottaGame, componentRegistry: ComponentRegistry) {
    getComponentClasses(game).forEachIndexed { index, kClass ->
        componentRegistry.registerComponent(ShortComponentKey(index.toShort()), kClass, getImplKClass(kClass) as KClass<out Component<*>>)
    }
    getInputComponentClasses(game).forEachIndexed { index, kClass ->
        componentRegistry.registerInputComponent(ShortComponentKey(index.toShort()), kClass, getImplKClass(kClass) as KClass<out InputComponent<*>>)
    }
    getEffectClasses(game).forEachIndexed { index, kClass ->
        componentRegistry.registerEffect(ShortEffectKey(index.toShort()), kClass, getImplKClass(kClass) as KClass<out CottaEffect>)
    }
}

fun getComponentClasses(game: CottaGame): List<KClass<out Component<*>>> {
    val gameClass = game::class
    return Class.forName(gameClass.qualifiedName + "Components").let {
        val method = it.getMethod("getComponents")
        @Suppress("UNCHECKED_CAST")
        val components = method.invoke(it.getConstructor().newInstance()) as List<KClass<*>>
        components.map { it as KClass<out Component<*>> }
    }
}

fun getInputComponentClasses(game: CottaGame): List<KClass<out InputComponent<*>>> {
    val gameClass = game::class
    return Class.forName(gameClass.qualifiedName + "InputComponents").let {
        val method = it.getMethod("getComponents")
        @Suppress("UNCHECKED_CAST")
        val components = method.invoke(it.getConstructor().newInstance()) as List<KClass<*>>
        components.map { it as KClass<out InputComponent<*>> }
    }
}

fun getEffectClasses(game: CottaGame): List<KClass<out CottaEffect>> {
    val gameClass = game::class
    return Class.forName(gameClass.qualifiedName + "Effects").let {
        val method = it.getMethod("getEffects")
        @Suppress("UNCHECKED_CAST")
        val components = method.invoke(it.getConstructor().newInstance()) as List<KClass<*>>
        components.map { it as KClass<out CottaEffect> }
    }
}

private fun getImplKClass(kClass: KClass<*>): KClass<*> = (kClass.qualifiedName + "Impl").let {
    Class.forName(it).kotlin
}
