package com.mgtriffid.games.cotta.core.simulation.invokers.impl

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.simulation.PlayersSawTicks
import com.mgtriffid.games.cotta.core.simulation.invokers.LagCompensatingEffectBus
import com.mgtriffid.games.cotta.core.simulation.invokers.SawTickHolder
import com.mgtriffid.games.cotta.core.simulation.invokers.SystemInvoker
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import com.mgtriffid.games.cotta.core.systems.LagCompensatedEffectsConsumerSystem
import jakarta.inject.Inject
import jakarta.inject.Named
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

// TODO forbid creation of Entities here or make Entities created in the latest tick
class LagCompensatingEffectsConsumerInvoker @Inject constructor(
    @Named("historical") private val effectBus: LagCompensatingEffectBus,
    private val sawTickHolder: SawTickHolder,
    @Named("lagCompensated") private val context: EffectProcessingContext,
    private val playersSawTicks: PlayersSawTicks
) : SystemInvoker<EffectsConsumerSystem<*>> {
    override fun invoke(system: EffectsConsumerSystem<*>) {
        logger.debug { "Invoked ${system::class.qualifiedName}" }
        if (system::class.simpleName == "MovementEffectConsumerSystem") {
            logger.info { "Invoked MovementEffectConsumerSystem in simulation" }
        }
        effectBus.effects().forEach { process(it, system) }
        if (system::class.simpleName == "MovementEffectConsumerSystem") {
            logger.info { "Done invoking MovementEffectConsumerSystem in simulation" }
        }
    }

    private fun <T: CottaEffect> process(effect: CottaEffect, system: EffectsConsumerSystem<T>) {
        logger.debug { "${system::class.simpleName} processing effect $effect" }
        if (system.effectType.isAssignableFrom(effect::class.java)) {
            val e = system.effectType.cast(effect)
            if (system is LagCompensatedEffectsConsumerSystem) {
                val playerId = system.player(e)
                sawTickHolder.tick = playersSawTicks[playerId]
            } else {
                sawTickHolder.tick = effectBus.getTickForEffect(effect)
            }
            system.handle(e, context)

            sawTickHolder.tick = null
        }
    }
}
