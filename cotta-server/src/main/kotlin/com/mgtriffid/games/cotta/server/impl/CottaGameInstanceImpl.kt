package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.loop.impl.FixedRateLoopBody
import com.mgtriffid.games.cotta.core.registry.ComponentRegistry2
import com.mgtriffid.games.cotta.core.registry.ComponentsRegistry
import com.mgtriffid.games.cotta.core.registry.ShortComponentKey
import com.mgtriffid.games.cotta.core.registry.ShortEffectKey
import com.mgtriffid.games.cotta.core.serialization.InputRecipe
import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import com.mgtriffid.games.cotta.network.ConnectionId
import com.mgtriffid.games.cotta.network.CottaServerNetworkTransport
import com.mgtriffid.games.cotta.network.purgatory.EnterGameIntent
import com.mgtriffid.games.cotta.server.*
import jakarta.inject.Inject
import jakarta.inject.Named
import mu.KotlinLogging
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

class CottaGameInstanceImpl<IR: InputRecipe> @Inject constructor(
    private val game: CottaGame,
    private val componentsRegistry: ComponentsRegistry,
    private val componentRegistry2: ComponentRegistry2,
    private val network: CottaServerNetworkTransport,
    private val clientsGhosts: ClientsGhosts<IR>,
    private val tickProvider: TickProvider,
    @Named("simulation") private val state: CottaState,
    private val serverToClientDataDispatcher: ServerToClientDataDispatcher,
    private val entitiesCreatedOnClientsRegistry: EntitiesCreatedOnClientsRegistry,
    private val serverSimulation: ServerSimulation,
    private val serverSimulationInputProvider: ServerSimulationInputProvider
): CottaGameInstance {

    @Volatile
    var running = true

    override fun run() {
        registerComponents()
        registerComponents2()
        initializeState()
        registerSystems()
        logger.debug { "Tick length is ${game.config.tickLength}" }
        val loop = FixedRateLoopBody(
            tickLengthMs = game.config.tickLength,
            startsAt = System.currentTimeMillis()
        ) {
            tick()
        }
        loop.start()
    }

    // TODO probably this is wrong place
    private fun registerComponents() {
        getComponentClasses().forEach {
            componentsRegistry.registerComponentClass(it)
        }
        game.inputComponentClasses.forEach {
            componentsRegistry.registerInputComponentClass(it)
        }
        game.effectClasses.forEach {
            componentsRegistry.registerEffectClass(it)
        }
        serverSimulation.setMetaEntitiesInputComponents(game.metaEntitiesInputComponents)
    }

    private fun registerComponents2() {
        getComponentClasses2().forEachIndexed { index, kClass ->
            componentRegistry2.registerComponent(ShortComponentKey(index.toShort()), kClass, (kClass.qualifiedName + "Impl").let {
                Class.forName(it).kotlin as KClass<out Component<*>>
            })
        }
        getInputComponentClasses2().forEachIndexed { index, kClass ->
            componentRegistry2.registerInputComponent(ShortComponentKey(index.toShort()), kClass, (kClass.qualifiedName + "Impl").let {
                Class.forName(it).kotlin as KClass<out InputComponent<*>>
            })
        }
        getEffectClasses2().forEachIndexed { index, kClass ->
            componentRegistry2.registerEffect(ShortEffectKey(index.toShort()), kClass, (kClass.qualifiedName + "Impl").let {
                Class.forName(it).kotlin as KClass<out CottaEffect>
            })
        }
        serverSimulation.setMetaEntitiesInputComponents(game.metaEntitiesInputComponents)
    }

    private fun getComponentClasses2(): List<KClass<out Component<*>>> {
        val gameClass = game::class
        return Class.forName(gameClass.qualifiedName + "Components").let {
            val method = it.getMethod("getComponents")
            @Suppress("UNCHECKED_CAST")
            val components = method.invoke(it.getConstructor().newInstance()) as List<KClass<*>>
            components.map { it as KClass<out Component<*>> }
        }
    }

    private fun getInputComponentClasses2(): List<KClass<out InputComponent<*>>> {
        val gameClass = game::class
        return Class.forName(gameClass.qualifiedName + "InputComponents").let {
            val method = it.getMethod("getComponents")
            @Suppress("UNCHECKED_CAST")
            val components = method.invoke(it.getConstructor().newInstance()) as List<KClass<*>>
            components.map { it as KClass<out InputComponent<*>> }
        }
    }

    private fun getEffectClasses2(): List<KClass<out CottaEffect>> {
        val gameClass = game::class
        return Class.forName(gameClass.qualifiedName + "Effects").let {
            val method = it.getMethod("getEffects")
            @Suppress("UNCHECKED_CAST")
            val components = method.invoke(it.getConstructor().newInstance()) as List<KClass<*>>
            components.map { it as KClass<out CottaEffect> }
        }
    }

    // TODO dry
    private fun getComponentClasses(): Set<KClass<out Component<*>>> {
        val gameClass = game::class
        return Class.forName(gameClass.qualifiedName + "Components").let {
            val method = it.getMethod("getComponents")
            @Suppress("UNCHECKED_CAST")
            val components = method.invoke(it.getConstructor().newInstance()) as List<KClass<*>>
            components.map { it as KClass<out Component<*>> }.toSet()
        }
    }

    private fun initializeState() {
        game.initializeStaticState(state.entities(tickProvider.tick))
        state.setBlank(state.entities(tickProvider.tick))
        game.initializeServerState(state.entities(tickProvider.tick))
    }

    private fun registerSystems() {
        game.serverSystems.forEach { serverSimulation.registerSystem(it as KClass<CottaSystem>) }
    }

    private fun tick() {
        serverSimulation.tick(fetchInput())
        dispatchDataToClients()
    }

    private fun fetchInput(): SimulationInput {
        serverSimulationInputProvider.fetch()
        val delta = serverSimulationInputProvider.getDelta()
        entitiesCreatedOnClientsRegistry.populate(delta.createdEntities)
        val intents = network.drainEnterGameIntents() // TODO move into delta, for it's used to perform simulation
        intents.forEach { (connectionId, intent) ->
            registerPlayer(connectionId, intent)
        }
        return delta.input
    }

    private fun registerPlayer(connectionId: ConnectionId, intent: EnterGameIntent) {
        logger.debug { "Received an intent to enter the game from connection '${connectionId.id}'" }
        val playerId = serverSimulation.enterGame(intent)
        clientsGhosts.addGhost(playerId, connectionId)
    }

    private fun dispatchDataToClients() {
        logger.debug { "Preparing data to send to clients" }
        serverToClientDataDispatcher.dispatch()
    }
}
