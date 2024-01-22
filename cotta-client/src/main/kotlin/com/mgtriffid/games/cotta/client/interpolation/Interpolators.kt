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
    private val interpolators = HashMap<KClass<out Component<*>>, ComponentInterpolator<*>>()

    fun <C: Component<C>> register(klass: KClass<out Component<C>>) {
        var interpolatedFields = klass.declaredMemberProperties.filter { it.hasAnnotation<Interpolated>() }
        if (interpolatedFields.isEmpty()) {
            return
        }
        interpolatedFields = interpolatedFields.filterIsInstance<KMutableProperty1<C, *>>()

        interpolators[klass] = createInterpolator(interpolatedFields)
    }

    private fun <C: Component<C>> createInterpolator(fields: List<KMutableProperty1<C, *>>): ComponentInterpolator<*> {
        val fieldInterpolators: List<FieldInterpolator<C, *>> = fields.map { createFieldInterpolator(it) }
        return ComponentInterpolator(fieldInterpolators)
    }

    private fun <C: Component<C>, V> createFieldInterpolator(property: KMutableProperty1<C, V>): FieldInterpolator<C, V> {
        val type = property.returnType
        val interpolator = when (type.classifier) {
            Float::class -> FloatInterpolator()
            Double::class -> DoubleInterpolator()
            Int::class -> IntInterpolator()
            Long::class -> LongInterpolator()
            Short::class -> ShortInterpolator()
            else -> throw IllegalArgumentException("Cannot interpolate type ${type.classifier}")
        }
        @Suppress("UNCHECKED_CAST")
        return FieldInterpolator(property, interpolator as Interpolator<V>)
    }

    // TODO somehow refactor and make this return interpolated component; refactor in CottaClientImpl accordingly
    fun <C: Component<C>> interpolate(prev: C, curr: C, interpolated: C, alpha: Float) {
        interpolators.keys.find { prev::class.isSubclassOf(it) }?.let {
            @Suppress("UNCHECKED_CAST")
            (interpolators[it] as? ComponentInterpolator<C>)?.interpolate(prev, curr, interpolated, alpha)
        }
    }

    private class FieldInterpolator<T: Component<T>, V>(
        val property: KMutableProperty1<T, V>,
        val interpolator: Interpolator<V>)
    {
        fun interpolate(from: T, to: T, target: T, alpha: Float) {
            val fromValue = property.get(from)
            val toValue = property.get(to)
            val interpolatedValue = interpolator.interpolate(fromValue, toValue, alpha)
            property.set(target, interpolatedValue)
        }
    }

    private class ComponentInterpolator<T : Component<T>>(
        private val fieldInterpolators: List<FieldInterpolator<T, *>>
    ) {
        fun interpolate(from: T, to: T, target: T, alpha: Float) {
            fieldInterpolators.forEach { i -> i.interpolate(from, to, target, alpha) }
        }
    }

    interface Interpolator<C> {
        fun interpolate(from: C, to: C, alpha: Float): C
    }

    class FloatInterpolator: Interpolator<Float> {
        override fun interpolate(from: Float, to: Float, alpha: Float): Float {
            return from + (to - from) * alpha
        }
    }

    class DoubleInterpolator: Interpolator<Double> {
        override fun interpolate(from: Double, to: Double, alpha: Float): Double {
            return from + (to - from) * alpha
        }
    }

    class IntInterpolator: Interpolator<Int> {
        override fun interpolate(from: Int, to: Int, alpha: Float): Int {
            return from + ((to - from) * alpha).roundToInt()
        }
    }

    class LongInterpolator:
        Interpolator<Long> {
        override fun interpolate(from: Long, to: Long, alpha: Float): Long {
            return from + ((to - from) * alpha).roundToInt()
        }
    }

    class ShortInterpolator: Interpolator<Short> {
        override fun interpolate(from: Short, to: Short, alpha: Float): Short {
            return (from + ((to - from) * alpha).roundToInt()).toShort()
        }
    }
}
