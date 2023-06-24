package com.mgtriffid.games.cotta.server.impl.invokers

import com.mgtriffid.games.cotta.core.annotations.LagCompensated
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.effects.EffectsConsumer
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import com.mgtriffid.games.cotta.core.systems.EntityProcessingSystem
import com.mgtriffid.games.cotta.core.systems.InputProcessingSystem
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.server.impl.invokers.LagCompensatingInputProcessingSystemInvoker.EntityOwnerSawTickProvider
import kotlin.reflect.KClass
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor

class InvokersFactoryImpl(
    private val lagCompensatingEffectBus: LagCompensatingEffectBus,
    private val state: CottaState,
    private val entityOwners: HashMap<EntityId, PlayerId>,
    private val playersSawTicks: HashMap<PlayerId, Long>,
    private val tickProvider: TickProvider,
    private val sawTickHolder: SawTickHolder
) : InvokersFactory {
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
                    EffectBus::class -> lagCompensatingEffectBus
                    Entities::class -> LatestEntities(state)
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
        return SimpleEffectsConsumerSystemInvoker(ctor.call(*parameterValues.toTypedArray()) as EffectsConsumer, lagCompensatingEffectBus)
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
                    Entities::class -> LatestEntities(state)
                    else -> null
                }
            }
        }
        return LagCompensatingInputProcessingSystemInvoker(
            state = state,
            system = ctor.call(*parameterValues.toTypedArray()) as InputProcessingSystem,
            entityOwnerSawTickProvider = object : EntityOwnerSawTickProvider {
                override fun getSawTickByEntityId(entityId: EntityId): Long? {
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
                    EffectBus::class -> lagCompensatingEffectBus
                    Entities::class -> ReadingFromPreviousTickEntities(sawTickHolder, state, tickProvider)

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

    private class LatestEntities(private val state: CottaState) : Entities {
        override fun createEntity(ownedBy: Entity.OwnedBy): Entity {
            return state.entities().createEntity(ownedBy)
        }

        override fun get(id: EntityId): Entity {
            return state.entities().get(id)
        }

        override fun all(): Collection<Entity> {
            return state.entities().all()
        }

        override fun remove(id: EntityId) {
            throw NotImplementedError("Is not supposed to be called on Server")
        }

        override fun createEntity(id: EntityId, ownedBy: Entity.OwnedBy): Entity {
            throw NotImplementedError("Is not supposed to be called on Server")
        }
    }

    private class ReadingFromPreviousTickEntities(
        private val sawTickHolder: SawTickHolder,
        private val state: CottaState,
        private val tickProvider: TickProvider
    ): Entities {
        override fun createEntity(ownedBy: Entity.OwnedBy): Entity {
            return state.entities().createEntity(ownedBy)
        }

        override fun get(id: EntityId): Entity {
            return state.entities(atTick = sawTickHolder.tick ?: tickProvider.tick).get(id)
        }

        override fun all(): Collection<Entity> {
            return state.entities(atTick = sawTickHolder.tick ?: tickProvider.tick).all()
        }

        override fun createEntity(id: EntityId, ownedBy: Entity.OwnedBy): Entity {
            throw NotImplementedError("Is not supposed to be called on Server")
        }

        override fun remove(id: EntityId) {
            // TODO should be actually handled by a different subclass
            throw NotImplementedError("Is not supposed to be called on Server")
        }
    }

    data class SawTickHolder(var tick: Long?)
}
