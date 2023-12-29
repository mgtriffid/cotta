package com.mgtriffid.games.cotta.client.interpolation

import com.mgtriffid.games.cotta.client.annotation.Interpolated
import com.mgtriffid.games.cotta.core.entities.Component
import kotlin.math.roundToInt
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf

class Interpolators {
    private val interpolators = HashMap<KClass<out Component<*>>, Interpolator<*>>()

    fun <C: Component<C>> register(klass: KClass<out Component<C>>) {
        val interpolatedFields = klass.declaredMemberProperties.filter { it.hasAnnotation<Interpolated>() }
        if (interpolatedFields.isEmpty()) {
            return
        }
        if (interpolatedFields.any { it !is KMutableProperty1<*, *> }) {
            throw IllegalArgumentException("Only mutable properties can be interpolated")
        }
        interpolators[klass] = createInterpolator(interpolatedFields as List<KMutableProperty1<C, *>>)
    }

    private fun <C: Component<C>> createInterpolator(fields: List<KMutableProperty1<C, *>>): Interpolator<*> {
        val fieldInterpolators: Map<KMutableProperty1<C, *>, FieldInterpolator<*>> = fields.associateWith { createFieldInterpolator(it) }
        return Interpolator(fieldInterpolators)
    }

    private fun <C: Component<C>> createFieldInterpolator(property: KMutableProperty1<C, *>): FieldInterpolator<*> {
        val type = property.returnType
        return when (type.classifier) {
            Float::class -> FloatInterpolator()
            Double::class -> DoubleInterpolator()
            Int::class -> IntInterpolator()
            Long::class -> LongInterpolator()
            Short::class -> ShortInterpolator()
            else -> throw IllegalArgumentException("Cannot interpolate type ${type.classifier}")
        }
    }

    // TODO somehow refactor and make this return interpolated component; refactor in CottaClientImpl accordingly
    fun <C: Component<C>> interpolate(prev: C, curr: C, interpolated: C, alpha: Float) {
        interpolators.keys.find { prev::class.isSubclassOf(it) }?.let {
            (interpolators[it] as? Interpolator<C>)?.interpolate(prev, curr, interpolated, alpha)
        }
    }

    private class Interpolator<T : Component<T>>(
        private val fieldInterpolators: Map<KMutableProperty1<T, *>, FieldInterpolator<*>>
    ) {
        fun interpolate(from: T, to: T, target: T, alpha: Float) {
            fieldInterpolators.forEach { p: KMutableProperty1<T, *>, fi: FieldInterpolator<*> ->
                p as KMutableProperty1<T, Any>
                @Suppress("UNCHECKED_CAST")
                val fromValue = p.get(from) as Any
                @Suppress("UNCHECKED_CAST")
                val toValue = p.get(to) as Any
                fi as FieldInterpolator<Any>
                val interpolatedValue = fi.interpolate(fromValue, toValue, alpha)
                p.set(target, interpolatedValue)
            }
        }
    }

    interface FieldInterpolator<C> {
        fun interpolate(from: C, to: C, alpha: Float): C
    }

    class FloatInterpolator: FieldInterpolator<Float> {
        override fun interpolate(from: Float, to: Float, alpha: Float): Float {
            return from + (to - from) * alpha
        }
    }

    class DoubleInterpolator: FieldInterpolator<Double> {
        override fun interpolate(from: Double, to: Double, alpha: Float): Double {
            return from + (to - from) * alpha
        }
    }

    class IntInterpolator: FieldInterpolator<Int> {
        override fun interpolate(from: Int, to: Int, alpha: Float): Int {
            return from + ((to - from) * alpha).roundToInt()
        }
    }

    class LongInterpolator: FieldInterpolator<Long> {
        override fun interpolate(from: Long, to: Long, alpha: Float): Long {
            return from + ((to - from) * alpha).roundToInt()
        }
    }

    class ShortInterpolator: FieldInterpolator<Short> {
        override fun interpolate(from: Short, to: Short, alpha: Float): Short {
            return (from + ((to - from) * alpha).roundToInt()).toShort()
        }
    }
}
