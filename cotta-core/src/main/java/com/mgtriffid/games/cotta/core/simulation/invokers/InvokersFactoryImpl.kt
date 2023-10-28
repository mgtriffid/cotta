package com.mgtriffid.games.cotta.core.simulation.invokers

import com.mgtriffid.games.cotta.core.annotations.LagCompensated
import com.mgtriffid.games.cotta.core.effects.EffectPublisher
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import com.mgtriffid.games.cotta.core.systems.EntityProcessingSystem
import com.mgtriffid.games.cotta.core.systems.InputProcessingSystem
import com.mgtriffid.games.cotta.core.simulation.PlayersSawTicks
import com.mgtriffid.games.cotta.core.simulation.invokers.LagCompensatingInputProcessingSystemInvoker.EntityOwnerSawTickProvider
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.hasAnnotation

class InvokersFactoryImpl(
    private val lagCompensatingEffectBus: LagCompensatingEffectBus,
    private val state: CottaState,
    private val playersSawTicks: PlayersSawTicks,
    private val sawTickHolder: SawTickHolder
) : InvokersFactory {
    // simulation invoker! very specific thing
    override fun <T : CottaSystem> createInvoker(systemClass: KClass<T>): Pair<SystemInvoker<*>, T> {
        val ctor = systemClass.getConstructor()
        val parameters = ctor.parameters
        val publisher = lagCompensatingEffectBus.publisher()
        val parameterValues = parameters.map { param: KParameter ->
            (param.type.classifier as? KClass<*>)?.let {
                when (it) {
                    EffectPublisher::class -> {
                        publisher
                    }
                    Entities::class -> {
                        if (param.hasAnnotation<LagCompensated>()) {
                            ReadingFromPreviousTickEntities(sawTickHolder, state)
                        } else {
                            LatestEntities(state)
                        }
                    }
                    else -> null
                }
            }
        }.toTypedArray()
        val system = ctor.call(*parameterValues)
        return when (system) {
            is InputProcessingSystem -> {
                // propagates sawTick to lagCompensatingEffectBus so that effect would know what was seen by the player
                Pair(LagCompensatingInputProcessingSystemInvoker(
                    entities = LatestEntities(state),
                    entityOwnerSawTickProvider = object : EntityOwnerSawTickProvider {
                        override fun getSawTickByEntity(entity: Entity): Long? {
                            return (entity.ownedBy as? Entity.OwnedBy.Player)?.let { playersSawTicks[it.playerId] }
                        }
                    },
                    sawTickHolder = sawTickHolder
                ), system)
            }

            is EntityProcessingSystem -> {
                // normal stuff, uses LatestEntities and lagCompensatingEffectBus (why not normal tho)
                Pair(EntityProcessingSystemInvoker(
                    state = state
                ), system)
            }

            is EffectsConsumerSystem -> if (
                parameters.any {
                    it.type.classifier as? KClass<*> == Entities::class &&
                            it.hasAnnotation<LagCompensated>()
                }
            ) {
                Pair(LagCompensatingEffectsConsumerInvoker(
                    lagCompensatingEffectBus,
                    sawTickHolder
                ), system)
            } else {
                Pair(SimpleEffectsConsumerSystemInvoker(
                    lagCompensatingEffectBus
                ), system)
            }

            else -> { throw IllegalStateException("Unexpected implementation of CottaSystem") }
        }
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
        private val state: CottaState
    ) : Entities {
        override fun createEntity(ownedBy: Entity.OwnedBy): Entity {
            return state.entities().createEntity(ownedBy)
        }

        override fun get(id: EntityId): Entity {
            val entities = sawTickHolder.tick?.let { state.entities(atTick = it) } ?: state.entities()
            return entities.get(id)
        }

        override fun all(): Collection<Entity> {
            val entities = sawTickHolder.tick?.let { state.entities(atTick = it) } ?: state.entities()
            return entities.all()
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
