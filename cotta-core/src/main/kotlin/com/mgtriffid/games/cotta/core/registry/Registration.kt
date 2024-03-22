package com.mgtriffid.games.cotta.core.registry

import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.codegen.Constants.COMPONENTS_CLASS_SUFFIX
import com.mgtriffid.games.cotta.core.codegen.Constants.EFFECTS_CLASS_SUFFIX
import com.mgtriffid.games.cotta.core.codegen.Constants.GET_COMPONENTS_METHOD
import com.mgtriffid.games.cotta.core.codegen.Constants.GET_EFFECTS_METHOD
import com.mgtriffid.games.cotta.core.codegen.Constants.GET_INPUT_COMPONENTS_METHOD
import com.mgtriffid.games.cotta.core.codegen.Constants.IMPL_SUFFIX
import com.mgtriffid.games.cotta.core.codegen.Constants.INPUT_COMPONENTS_CLASS_SUFFIX
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Component
import mu.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.declaredMemberProperties

private val logger = KotlinLogging.logger {}

fun registerComponents(game: CottaGame, componentRegistry: ComponentRegistry) {
    getComponentClasses(game).forEachIndexed { index, kClass ->
        componentRegistry.registerComponent(
            ShortComponentKey(index.toShort()),
            kClass,
            getImplKClass(kClass) as KClass<out Component<*>>,
            kClass.declaredMemberProperties.any { it is KMutableProperty1 }
        )
    }
    getEffectClasses(game).forEachIndexed { index, kClass ->
        componentRegistry.registerEffect(ShortEffectKey(index.toShort()), kClass, getImplKClass(kClass) as KClass<out CottaEffect>)
    }
}

fun getComponentClasses(game: CottaGame): List<KClass<out Component<*>>> {
    val gameClass = game::class
    return getCottaGeneratedClass(gameClass, COMPONENTS_CLASS_SUFFIX).let {
        @Suppress("UNCHECKED_CAST")
        val components = invokeOnNewInstance(it, GET_COMPONENTS_METHOD) as List<KClass<*>>
        components.map { it as KClass<out Component<*>> }
    }
}

fun getEffectClasses(game: CottaGame): List<KClass<out CottaEffect>> {
    val gameClass = game::class
    return getCottaGeneratedClass(gameClass, EFFECTS_CLASS_SUFFIX).let {
        @Suppress("UNCHECKED_CAST")
        val components = invokeOnNewInstance(it, GET_EFFECTS_METHOD) as List<KClass<*>>
        components.map { it as KClass<out CottaEffect> }
    }
}

private fun invokeOnNewInstance(clazz: Class<*>, method: String): Any? =
    clazz.getMethod(method).invoke(clazz.getConstructor().newInstance())

private fun getCottaGeneratedClass(gameClass: KClass<out CottaGame>, suffix: String): Class<*> =
    Class.forName(gameClass.qualifiedName + suffix)

private fun findCottaGeneratedClass(gameClass: KClass<out CottaGame>, suffix: String): Class<*>? =
    try {
        getCottaGeneratedClass(gameClass, suffix)
    } catch (e: ClassNotFoundException) {
        logger.warn {"Could not find generated class for ${gameClass.qualifiedName} with suffix $suffix" }
        null
    }

private fun getImplKClass(kClass: KClass<*>): KClass<*> = (kClass.qualifiedName + IMPL_SUFFIX).let {
    Class.forName(it).kotlin
}
