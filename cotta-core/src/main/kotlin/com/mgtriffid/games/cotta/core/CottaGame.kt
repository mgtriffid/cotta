package com.mgtriffid.games.cotta.core

import com.mgtriffid.games.cotta.core.config.CottaConfig
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.input.InputProcessing
import com.mgtriffid.games.cotta.core.input.NoOpNonPlayerInputProvider
import com.mgtriffid.games.cotta.core.input.NonPlayerInputProvider
import com.mgtriffid.games.cotta.core.input.PlayerInput
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import kotlin.reflect.KClass

/**
 * The entry point for building a game. Implement this and mark with
 * [com.mgtriffid.games.cotta.core.annotations.Game].
 */
interface CottaGame {

    /**
     * Implement this to return the list of systems that will be invoked during
     * simulation. Order matters: systems will be invoked in the order they are
     * returned by this method. This method will be invoked once per Client and
     * one time during Server instance startup.
     *
     * @see CottaSystem
     */
    val systems: List<CottaSystem>

    /**
     * Implement this to allow the game to provide input for server-controlled
     * entities. For example, this can be used to provide input for NPCs. The
     * default implementation returns [NoOpNonPlayerInputProvider], which means
     * no server-side input exists, all inputs come from players.
     *
     * @see NonPlayerInputProvider
     */
    val nonPlayerInputProvider: NonPlayerInputProvider
        get() = NoOpNonPlayerInputProvider

    /**
     * Implement this to initialize the state of the server. All entities that
     * are created inside will be marked as "static" entities, and they must
     * never be modified, neither their components nor their existence. For
     * example, if you have a map, you can create all the tiles here, provided
     * they are indestructible and never change.
     *
     * This method is called on both Client and Server during startup. Client
     * and Server never exchange information about static Entities, it is
     * assumed static parts of the state on Client and Server are equal.
     */
    fun initializeStaticState(entities: Entities) {}

    /**
     * Implement this to create some initial state for the game simulation. This
     * can be a good place to add things like power-ups, enemies, NPCs, etc.
     */
    fun initializeServerState(entities: Entities) {}

    /**
     * Implement this to configure technical aspects of the game.
     */
    val config: CottaConfig

    /**
     * Implement this to return the class of the PlayerInput implementation that
     * the game uses.
     *
     * IMPORTANT: Make sure it's a Kotlin data class without logic. Just data.
     * Client and Server will serialize inputs and send them over the wire..
     */
    val playerInputKClass: KClass<out PlayerInput>

    /**
     * Specify the input processing logic for the game. Every simulation tick
     * Cotta will invoke this object to process the input from players and put
     * it into some components, as you configure.
     */
    val inputProcessing: InputProcessing

    /**
     * Specify how the game should react on players joining or leaving.
     */
    val playersHandler: PlayersHandler
}
