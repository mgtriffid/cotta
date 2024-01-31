package com.mgtriffid.games.cotta.core.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.esotericsoftware.minlog.Log
import org.slf4j.LoggerFactory

fun configureLogging() {
    // KryoNet:
    Log.set(Log.LEVEL_INFO)

    mapOf(
        "org.eclipse" to Level.INFO,
        "com.mgtriffid.games.cotta.server.impl" to Level.INFO,
        "com.mgtriffid.games.cotta.server.impl.invokers" to Level.INFO,
        "com.mgtriffid.games.cotta.client.invokers.impl" to Level.INFO,
        "com.mgtriffid.games.cotta.core.serialization.impl" to Level.INFO,
        "com.mgtriffid.games.cotta.network.kryonet" to Level.INFO,
        "com.mgtriffid.games.cotta.core.simulation.invokers" to Level.INFO,
        "com.mgtriffid.games.panna.shared.game.systems" to Level.INFO,
        "com.mgtriffid.games.cotta.client.impl.CottaClientImpl" to Level.INFO,
        "com.mgtriffid.games.cotta.core.registry.ComponentsRegistry" to Level.INFO,
        "com.mgtriffid.games.cotta.server.impl.ServerToClientDataDispatcherImpl" to Level.INFO,
        "com.mgtriffid.games.cotta.client.invokers.impl.PredictionCreateEntityStrategy" to Level.INFO,
        "com.mgtriffid.games.cotta.client.impl.PredictionSimulationImpl" to Level.INFO,
        "com.mgtriffid.games.cotta.server.impl.PredictedToAuthoritativeIdMappingsImpl" to Level.INFO,
        "com.mgtriffid.games.cotta.client.impl.ClientSimulationInputProviderImpl" to Level.INFO,
        "com.mgtriffid.games.cotta.core.simulation.SimulationInput" to Level.INFO,
        "com.mgtriffid.games.cotta.core.entities.impl.CottaStateImpl" to Level.INFO,
        "com.mgtriffid.games.cotta.core.entities.impl.AtomicLongTickProvider" to Level.INFO,
        "com.mgtriffid.games.cotta.client.impl.IncomingDataBuffer" to Level.INFO,
        "com.mgtriffid.games.panna.screens.menu.GameScreen" to Level.INFO,
        "com.mgtriffid.games.cotta.client.impl.ClientIncomingDataBuffer" to Level.INFO,
        "com.mgtriffid.games.cotta.client.network.impl.NetworkClientImpl" to Level.INFO,
        "com.mgtriffid.games.cotta.client.impl.AuthoritativeToPredictedEntityIdMappingsImpl" to Level.INFO,
        "com.mgtriffid.games.panna.shared.game.systems.ShootEffectConsumerSystem" to Level.INFO,
    ).forEach { (logger, level) ->
        (LoggerFactory.getLogger(logger) as Logger).level = level
    }
}
