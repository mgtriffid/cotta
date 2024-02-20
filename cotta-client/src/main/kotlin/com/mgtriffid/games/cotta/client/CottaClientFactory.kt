package com.mgtriffid.games.cotta.client

import com.google.inject.Guice
import com.mgtriffid.games.cotta.client.guice.CottaClientModule
import com.mgtriffid.games.cotta.client.interpolation.Interpolators
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.annotations.Predicted
import com.mgtriffid.games.cotta.core.registry.ComponentRegistry
import com.mgtriffid.games.cotta.core.registry.getComponentClasses
import com.mgtriffid.games.cotta.core.registry.registerComponents
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import kotlin.reflect.KClass
import kotlin.reflect.full.hasAnnotation

class CottaClientFactory {
    fun create(game: CottaGame, input: CottaClientInput) : CottaClient {
        val module = CottaClientModule(game, input)
        val injector = Guice.createInjector(module)
        val client = injector.getInstance(CottaClient::class.java)

        registerComponents(
            game,
            injector.getInstance(Interpolators::class.java),
            injector.getInstance(ComponentRegistry::class.java)
        )

        registerSystems(
            game,
            injector.getInstance(ClientSimulation::class.java),
            injector.getInstance(PredictionSimulation::class.java)
        )

        client.initialize()
        return client
    }

    private fun registerComponents(game: CottaGame, interpolators: Interpolators, componentRegistry: ComponentRegistry) {
        getComponentClasses(game).forEach{ kClass ->
            interpolators.register(kClass)
        }
        registerComponents(game, componentRegistry)
    }

    private fun registerSystems(game: CottaGame, clientSimulation: ClientSimulation, predictionSimulation: PredictionSimulation) {
        game.serverSystems.forEach { system ->
            clientSimulation.registerSystem(system as KClass<CottaSystem>)
            if (isPredicted(system)) {
                predictionSimulation.registerSystem(system)
            }
        }
    }

    private fun isPredicted(system: KClass<CottaSystem>): Boolean {
        return system.hasAnnotation<Predicted>()
    }
}
