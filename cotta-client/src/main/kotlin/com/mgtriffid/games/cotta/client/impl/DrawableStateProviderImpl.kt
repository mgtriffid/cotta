package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.DrawableEffect
import com.mgtriffid.games.cotta.client.DrawableEffects
import com.mgtriffid.games.cotta.client.DrawableState
import com.mgtriffid.games.cotta.client.DrawableStateProvider
import com.mgtriffid.games.cotta.client.InterpolationAlphas
import com.mgtriffid.games.cotta.client.PredictionSimulation
import com.mgtriffid.games.cotta.client.interpolation.Interpolators
import com.mgtriffid.games.cotta.core.GLOBAL
import com.mgtriffid.games.cotta.core.SIMULATION
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.entities.impl.EntityImpl
import jakarta.inject.Inject
import jakarta.inject.Named
import java.util.*
import kotlin.reflect.KClass

private val logger = mu.KotlinLogging.logger {}

class DrawableStateProviderImpl @Inject constructor(
    @Named(SIMULATION) private val simulationTickProvider: TickProvider,
    @Named(GLOBAL) private val globalTickProvider: TickProvider,
    @Named("prediction") private val predictionTickProvider: TickProvider,
    private val interpolators: Interpolators,
    @Named("simulation") private val state: CottaState,
    private val predictionSimulation: PredictionSimulation,
    private val effectBus: EffectBus,
    private val localPlayer: LocalPlayer,
) : DrawableStateProvider {
    private var lastTickEffectsWereReturned: Long = -1
    private val previouslyPredicted = TreeMap<Long, Collection<DrawableEffect>>()

    // GROOM what the actual fuck
    override fun get(alphas: InterpolationAlphas, components: Array<out KClass<out Component<*>>>): DrawableState {
        if (globalTickProvider.tick == 0L) return DrawableState.NotReady
        val onlyNeeded: Collection<Entity>.() -> Collection<Entity> = {
            filter { entity ->
                components.all { entity.hasComponent(it) }
            }
        }
        val predictedCurrent = this.predictionSimulation.getLocalPredictedEntities().onlyNeeded()
        val predictedPrevious = this.predictionSimulation.getPreviousLocalPredictedEntities().onlyNeeded()
        // Here guessed vs authoritative becomes interesting. CottaStateView?
        val authoritativeCurrent = this.state.entities(this.simulationTickProvider.tick).all().onlyNeeded()
        val authoritativePrevious = this.state.entities(this.simulationTickProvider.tick - 1).all().onlyNeeded()
        val predicted = interpolate(predictedPrevious, predictedCurrent, alphas.prediction, components.toList())
        val authoritative = interpolate(authoritativePrevious, authoritativeCurrent, alphas.simulation, components.toList())
        val predictedIds = predicted.map { it.id }
        val entities =
            (predicted + authoritative.filter {
                it.id !in predictedIds
            }).also {
                logger.trace { "Entities found: ${it.map { it.id }}" }
            }

        // TODO consider possible edge cases when the number of ticks we're ahead of server changes due to changing
        //  network conditions.
        val effects = if (lastTickEffectsWereReturned < globalTickProvider.tick) {
            logger.debug { "Simulation tick: ${globalTickProvider.tick}" }
            logger.debug { "Prediction tick: ${predictionTickProvider.tick}" }
            lastTickEffectsWereReturned = globalTickProvider.tick
            val predictedEffects = predictionSimulation.effectBus.effects().map(::DrawableEffect)
            val previouslyPredictedEffects = previouslyPredicted[predictionTickProvider.tick + 1] // TODO why +1?
            logger.debug { "previouslyPredictedEffects: $previouslyPredictedEffects" }
            val real = effectBus.effects().map(::DrawableEffect)
            logger.debug { "Real effects: $real" }
            val effects = object : DrawableEffects {
                override val real: Collection<DrawableEffect> = real - (previouslyPredictedEffects ?: emptyList())
                override val predicted: Collection<DrawableEffect> = predictedEffects
                override val mispredicted: Collection<DrawableEffect> = emptyList()
            }
            previouslyPredicted[globalTickProvider.tick] = predictedEffects
            cleanUpOldPredictedEffects()
            effects
        } else {
            DrawableEffects.EMPTY
        }

        return object : DrawableState.Ready {
            override val entities: List<Entity> = entities
            override val effects: DrawableEffects = effects
            override val playerId: PlayerId = localPlayer.playerId
        }
    }

    private fun cleanUpOldPredictedEffects() {
        val old = previouslyPredicted.keys.filter { it < globalTickProvider.tick - 128 }
        old.forEach { previouslyPredicted.remove(it) }
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
