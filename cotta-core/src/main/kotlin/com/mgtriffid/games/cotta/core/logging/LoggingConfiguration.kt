package com.mgtriffid.games.cotta.core.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.esotericsoftware.minlog.Log
import org.slf4j.LoggerFactory

fun configureLogging() {
    // KryoNet:
    Log.set(Log.LEVEL_INFO)

    val default = Level.INFO
    mapOf(
        "org.eclipse" to default,
        "com.mgtriffid.games.cotta.server.impl" to default,
        "com.mgtriffid.games.cotta.server.impl.invokers" to default,
        "com.mgtriffid.games.cotta.client.invokers.impl" to default,
        "com.mgtriffid.games.cotta.core.serialization.impl" to default,
        "com.mgtriffid.games.cotta.network.kryonet" to default,
        "com.mgtriffid.games.cotta.core.simulation.invokers" to default,
        "com.mgtriffid.games.panna.shared.game.systems" to default,
        "com.mgtriffid.games.cotta.client.impl.CottaClientImpl" to default,
        "com.mgtriffid.games.cotta.core.registry.ComponentsRegistry" to default,
        "com.mgtriffid.games.cotta.server.impl.ServerToClientDataDispatcherImpl" to default,
        "com.mgtriffid.games.cotta.client.invokers.impl.PredictionCreateEntityStrategy" to default,
        "com.mgtriffid.games.cotta.client.impl.PredictionSimulationImpl" to default,
        "com.mgtriffid.games.cotta.server.impl.PredictedToAuthoritativeIdMappingsImpl" to default,
        "com.mgtriffid.games.cotta.client.impl.ClientSimulationInputProviderImpl" to default,
        "com.mgtriffid.games.cotta.core.simulation.SimulationInput" to default,
        "com.mgtriffid.games.cotta.core.entities.impl.CottaStateImpl" to default,
        "com.mgtriffid.games.cotta.core.entities.impl.AtomicLongTickProvider" to default,
        "com.mgtriffid.games.cotta.client.impl.IncomingDataBuffer" to default,
        "com.mgtriffid.games.cotta.client.impl.AuthoritativeSimulationImpl" to default,
        "com.mgtriffid.games.panna.screens.menu.GameScreen" to default,
        "com.mgtriffid.games.cotta.client.impl.ClientIncomingDataBuffer" to default,
        "com.mgtriffid.games.cotta.client.network.impl.NetworkClientImpl" to default,
        "com.mgtriffid.games.cotta.client.impl.ClientInputsImpl" to default,
        "com.mgtriffid.games.cotta.client.impl.AuthoritativeToPredictedEntityIdMappingsImpl" to default,
        "com.mgtriffid.games.panna.shared.game.systems.ShootEffectConsumerSystem" to default,
        "com.mgtriffid.games.cotta.client.impl.DrawableStateProviderImpl" to default,
        "com.mgtriffid.games.panna.PannaClientGdxInput" to default,
        "com.mgtriffid.games.cotta.client.impl.SimulationsImpl" to default,
        "com.mgtriffid.games.cotta.core.simulation.impl.AuthoritativeSimulationImpl" to default,
        "com.mgtriffid.games.cotta.core.loop.impl.FixedRateLoopBody" to default,
    ).forEach { (logger, level) ->
        (LoggerFactory.getLogger(logger) as Logger).level = level
    }
}
