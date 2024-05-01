package com.mgtriffid.games.cotta.client

import com.google.inject.Guice
import com.google.inject.Key
import com.google.inject.name.Names
import com.mgtriffid.games.cotta.client.guice.CottaClientModule
import com.mgtriffid.games.cotta.client.interpolation.Interpolators
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.annotations.Predicted
import com.mgtriffid.games.cotta.core.registry.ComponentRegistry
import com.mgtriffid.games.cotta.core.registry.getComponentClasses
import com.mgtriffid.games.cotta.core.registry.registerComponents
import com.mgtriffid.games.cotta.core.simulation.Simulation
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import mu.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.full.hasAnnotation

private val logger = KotlinLogging.logger {}

class CottaClientFactory {
    fun create(game: CottaGame, input: CottaClientInput) : CottaClient {
        logger.info { "Creating Client" }
        val module = CottaClientModule(game, input)
        val injector = Guice.createInjector(module)
        val client = injector.getInstance(CottaClient::class.java)
        logger.info { "Client created" }

        logger.info { "Registering components" }
        registerComponents(
            game,
            injector.getInstance(Interpolators::class.java),
            injector.getInstance(ComponentRegistry::class.java)
        )

        logger.info { "Registering systems" }
        registerSystems(
            game,
            injector.getInstance(Key.get(Simulation::class.java, Names.named("simulation"))),
            injector.getInstance(PredictionSimulation::class.java),
            injector.getInstance(Key.get(Simulation::class.java, Names.named("guessed")))
        )
        logger.info { "Systems registered" }

        logger.info { "Initializing client" }
        client.initialize()
        logger.info { "Client initialized" }
        return client
    }

    private fun registerComponents(game: CottaGame, interpolators: Interpolators, componentRegistry: ComponentRegistry) {
        getComponentClasses(game).forEach{ kClass ->
            interpolators.register(kClass)
        }
        registerComponents(game, componentRegistry)
    }

    private fun registerSystems(
        game: CottaGame,
        clientSimulation: Simulation,
        predictionSimulation: PredictionSimulation,
        guessedSimulation: Simulation
    ) {

        game.systems.forEach { system ->
            clientSimulation.registerSystem(system)
            guessedSimulation.registerSystem(system)
            if (isPredicted(system)) {
                predictionSimulation.registerSystem(system)
            }
        }
    }

    private fun isPredicted(system: CottaSystem): Boolean {
        return system::class.hasAnnotation<Predicted>()
    }
}
