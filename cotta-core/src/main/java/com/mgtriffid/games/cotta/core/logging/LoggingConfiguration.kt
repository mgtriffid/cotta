package com.mgtriffid.games.cotta.core.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.esotericsoftware.minlog.Log
import org.slf4j.LoggerFactory

fun configureLogging() {
    // KryoNet:
    Log.set(Log.LEVEL_INFO)

    (LoggerFactory.getLogger("org.eclipse") as Logger).setLevel(Level.INFO)
    (LoggerFactory.getLogger("com.mgtriffid.games.cotta.server.impl") as Logger).setLevel(Level.INFO)
    (LoggerFactory.getLogger("com.mgtriffid.games.cotta.server.impl.invokers") as Logger).setLevel(Level.INFO)
    (LoggerFactory.getLogger("com.mgtriffid.games.cotta.client.invokers.impl") as Logger).setLevel(Level.INFO)
    (LoggerFactory.getLogger("com.mgtriffid.games.cotta.core.serialization.impl") as Logger).setLevel(Level.INFO)
    (LoggerFactory.getLogger("com.mgtriffid.games.cotta.network.kryonet") as Logger).setLevel(Level.INFO)
    (LoggerFactory.getLogger("com.mgtriffid.games.cotta.core.simulation.invokers") as Logger).setLevel(Level.INFO)
    (LoggerFactory.getLogger("com.mgtriffid.games.panna.shared.game.systems") as Logger).setLevel(Level.INFO)
    (LoggerFactory.getLogger("com.mgtriffid.games.cotta.client.impl.CottaClientImpl") as Logger).setLevel(Level.INFO)
    (LoggerFactory.getLogger("com.mgtriffid.games.cotta.core.registry.ComponentsRegistry") as Logger).setLevel(Level.INFO)
    (LoggerFactory.getLogger("com.mgtriffid.games.cotta.server.impl.ServerToClientDataDispatcherImpl") as Logger).setLevel(Level.INFO)
    (LoggerFactory.getLogger("com.mgtriffid.games.cotta.client.invokers.impl.PredictionCreateEntityStrategy") as Logger).setLevel(Level.INFO)
    (LoggerFactory.getLogger("com.mgtriffid.games.cotta.client.impl.PredictionSimulationImpl") as Logger).setLevel(Level.INFO)
    (LoggerFactory.getLogger("com.mgtriffid.games.cotta.server.impl.PredictedToAuthoritativeIdMappingsImpl") as Logger).setLevel(Level.INFO)
    (LoggerFactory.getLogger("com.mgtriffid.games.cotta.client.impl.ClientSimulationInputProviderImpl") as Logger).setLevel(Level.INFO)
    (LoggerFactory.getLogger("com.mgtriffid.games.cotta.core.simulation.SimulationInput") as Logger).setLevel(Level.DEBUG)
    (LoggerFactory.getLogger("com.mgtriffid.games.cotta.core.entities.impl.CottaStateImpl") as Logger).setLevel(Level.INFO)
    (LoggerFactory.getLogger("com.mgtriffid.games.cotta.core.entities.impl.AtomicLongTickProvider") as Logger).setLevel(Level.INFO)
    (LoggerFactory.getLogger("com.mgtriffid.games.cotta.client.impl.IncomingDataBuffer") as Logger).setLevel(Level.INFO)

    (LoggerFactory.getLogger("com.mgtriffid.games.panna.screens.menu.GameScreen") as Logger).setLevel(Level.INFO)
}