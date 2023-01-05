package com.mgtriffid.games.cotta.server.impl.invokers

import com.mgtriffid.games.cotta.core.annotations.LagCompensated
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.effects.EffectsConsumer
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import com.mgtriffid.games.cotta.core.systems.EntityProcessingSystem
import com.mgtriffid.games.cotta.core.systems.InputProcessingSystem
import com.mgtriffid.games.cotta.server.impl.invokers.LagCompensatingInputProcessingSystemInvoker.EntityOwnerSawTickProvider
import kotlin.reflect.KClass
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor

class InvokersFactoryImpl(
    private val effectBus: EffectBus,
    private val state: CottaState,
    private val entityOwners: HashMap<Int, Int>,
    private val playersSawTicks: HashMap<Int, Long>
) : InvokersFactory {
    private val sawTickHolder = LagCompensatingInputProcessingSystemInvoker.SawTickHolder(null)
    private val lagCompensatingEffectBus = LagCompensatingEffectBus(effectBus, sawTickHolder)

    override fun <T : CottaSystem> createInvoker(systemClass: KClass<T>): SystemInvoker {
        val shouldPropagateLagCompensationContext = systemClass.isSubclassOf(InputProcessingSystem::class)
        if (shouldPropagateLagCompensationContext) {
            return buildLagCompensatingInputProcessingSystemInvoker(systemClass)
        }
        val ctor = getConstructor(systemClass)
        val parameters = ctor.parameters
        val parameterValues = parameters.map { param ->
            (param.type.classifier as? KClass<*>)?.let {
                when (it) {
                    EffectBus::class -> effectBus
                    Entities::class -> object : Entities {
                        override fun createEntity(): Entity {
                            return state.entities().createEntity()
                        }

                        override fun get(id: Int): Entity {
                            return state.entities().get(id)
                        }

                        override fun all(): Collection<Entity> {
                            return state.entities().all()
                        }
                    }

                    else -> null
                }
            }
        }
        if (systemClass.isSubclassOf(EntityProcessingSystem::class)) {
            return EntityProcessingSystemInvoker(
                state = state,
                system = ctor.call(*parameterValues.toTypedArray()) as EntityProcessingSystem
            )
        }

        val shouldUseContextWhileConsumingEffects = parameters.any {
            it.type.classifier as? KClass<*> == Entities::class &&
                    it.hasAnnotation<LagCompensated>()
        }
        if (shouldUseContextWhileConsumingEffects) {
            return buildLagCompensatingEffectsConsumerInvoker(systemClass)
        }
        return SimpleEffectsConsumerSystemInvoker(ctor.call(*parameterValues.toTypedArray()) as CottaSystem, effectBus)
    }

    private fun <T : CottaSystem> getConstructor(systemClass: KClass<T>) =
        systemClass.primaryConstructor ?: throw IllegalArgumentException(
            "Class ${systemClass.qualifiedName} must have a primary constructor"
        )

    private fun <T : CottaSystem> buildLagCompensatingInputProcessingSystemInvoker(
        systemClass: KClass<T>
    ): SystemInvoker {
        val ctor = getConstructor(systemClass)
        val parameters = ctor.parameters
        val parameterValues = parameters.map { param ->
            (param.type.classifier as? KClass<*>)?.let {
                when (it) {
                    EffectBus::class -> lagCompensatingEffectBus
                    Entities::class -> object : Entities {
                        override fun createEntity(): Entity {
                            return state.entities().createEntity()
                        }

                        override fun get(id: Int): Entity {
                            return state.entities().get(id)
                        }

                        override fun all(): Collection<Entity> {
                            return state.entities().all()
                        }
                    }

                    else -> null
                }
            }
        }
        return LagCompensatingInputProcessingSystemInvoker(
            state = state,
            system = ctor.call(*parameterValues.toTypedArray()) as InputProcessingSystem,
            entityOwnerSawTickProvider = object : EntityOwnerSawTickProvider {
                override fun getSawTickByEntityId(entityId: Int): Long? {
                    return entityOwners[entityId]?.let { playersSawTicks[it] }
                }
            },
            sawTickHolder = sawTickHolder
        )
    }

    private fun <T : CottaSystem> buildLagCompensatingEffectsConsumerInvoker(
        systemClass: KClass<T>
    ): SystemInvoker {
        val ctor = getConstructor(systemClass)
        val parameters = ctor.parameters

        val parameterValues = parameters.map { param ->
            (param.type.classifier as? KClass<*>)?.let {
                when (it) {
                    EffectBus::class -> effectBus
                    Entities::class -> object : Entities {
                        override fun createEntity(): Entity {
                            return state.entities().createEntity()
                        }

                        override fun get(id: Int): Entity {
                            return state.entities().get(id)
                        }

                        override fun all(): Collection<Entity> {
                            return state.entities(atTick = sawTickHolder.tick ?: state.currentTick()).all()
                        }
                    }

                    else -> null
                }
            }
        }
        return LagCompensatingEffectsConsumerInvoker(
            lagCompensatingEffectBus,
            ctor.call(*parameterValues.toTypedArray()) as EffectsConsumer,
            sawTickHolder
        )
    }
}
