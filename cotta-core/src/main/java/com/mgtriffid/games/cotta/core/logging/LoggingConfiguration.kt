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
    (LoggerFactory.getLogger("com.mgtriffid.games.cotta.server.impl.ServerToClientDataDispatcherImpl") as Logger).setLevel(Level.DEBUG)

}