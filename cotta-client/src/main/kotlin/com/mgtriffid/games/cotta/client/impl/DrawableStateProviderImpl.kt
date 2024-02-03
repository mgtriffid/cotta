package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.AuthoritativeToPredictedEntityIdMappings
import com.mgtriffid.games.cotta.client.DrawableEffect
import com.mgtriffid.games.cotta.client.DrawableEffects
import com.mgtriffid.games.cotta.client.DrawableState
import com.mgtriffid.games.cotta.client.DrawableStateProvider
import com.mgtriffid.games.cotta.client.PredictionSimulation
import com.mgtriffid.games.cotta.client.interpolation.Interpolators
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.entities.id.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.id.PredictedEntityId
import com.mgtriffid.games.cotta.core.entities.impl.EntityImpl
import jakarta.inject.Inject
import jakarta.inject.Named
import kotlin.reflect.KClass

private val logger = mu.KotlinLogging.logger {}

class DrawableStateProviderImpl @Inject constructor(
    private val tickProvider: TickProvider,
    private val interpolators: Interpolators,
    @Named("simulation") private val state: CottaState,
    private val predictionSimulation: PredictionSimulation,
    private val authoritativeToPredictedEntityIdMappings: AuthoritativeToPredictedEntityIdMappings,
    private val effectBus: EffectBus
) : DrawableStateProvider {
    private var lastTickEffectsWereReturned: Long = -1

    override fun get(alpha: Float, components: Array<out KClass<out Component<*>>>): DrawableState {
        if (tickProvider.tick == 0L) return DrawableState.EMPTY
        val onlyNeeded: Collection<Entity>.() -> Collection<Entity> = {
            filter { entity ->
                components.all { entity.hasComponent(it) }
            }
        }
        val predictedCurrent = this.predictionSimulation.getLocalPredictedEntities().onlyNeeded()
        val predictedPrevious = this.predictionSimulation.getPreviousLocalPredictedEntities().onlyNeeded()
        val authoritativeCurrent = this.state.entities(this.tickProvider.tick).all().onlyNeeded()
        val authoritativePrevious = this.state.entities(this.tickProvider.tick - 1).all().onlyNeeded()
        val predicted = interpolate(predictedPrevious, predictedCurrent, alpha, components.toList())
        val authoritative = interpolate(authoritativePrevious, authoritativeCurrent, alpha, components.toList())
        val entities =
            (predicted + authoritative.filter { authoritativeToPredictedEntityIdMappings[it.id] == null }).also {
                logger.debug { "Entities found: ${it.map { it.id }}" }
            }

        // TODO consider possible edge cases when the number of ticks we're ahead of server changes due to changing
        //  network conditions.
        val effects = if (lastTickEffectsWereReturned < tickProvider.tick) {
            lastTickEffectsWereReturned = tickProvider.tick
            object : DrawableEffects {
                override val real: Collection<DrawableEffect> = effectBus.effects().map(::DrawableEffect)
                override val predicted: Collection<DrawableEffect> =
                    predictionSimulation.effectBus.effects().map(::DrawableEffect)
                override val mispredicted: Collection<DrawableEffect> = emptyList()
            }
        } else {
            DrawableEffects.EMPTY
        }

        return object : DrawableState {
            override val entities: List<Entity> = entities
            override val authoritativeToPredictedEntityIds: Map<AuthoritativeEntityId, PredictedEntityId> =
                authoritativeToPredictedEntityIdMappings.all()
            override val effects: DrawableEffects = effects
        }
    }

    private fun interpolate(
        previous: Collection<Entity>,
        current: Collection<Entity>,
        alpha: Float,
        components: List<KClass<out Component<*>>>
    ): List<Entity> {
        return current.mapNotNull { curr ->
            val prev = previous.find { it.id == curr.id }
            if (prev == null) null else {
                val interpolated = (curr as EntityImpl).deepCopy()
                components.forEach {
                    interpolate(prev, curr, interpolated, it, alpha)
                }
                interpolated
            }
        }
    }

    private fun interpolate(
        prev: Entity,
        curr: Entity,
        interpolated: Entity,
        component: KClass<out Component<*>>,
        alpha: Float
    ) {
        val prevComponent = prev.getComponent(component)
        val currComponent = curr.getComponent(component)
        val interpolatedComponent = interpolated.getComponent(component)
        interpolateComponent(prevComponent, currComponent, interpolatedComponent, alpha)
    }

    private fun <C : Component<C>> interpolateComponent(
        prev: Any,
        curr: Any,
        interpolated: Any,
        alpha: Float
    ) {
        interpolators.interpolate(prev as C, curr as C, interpolated as C, alpha)
    }
}
