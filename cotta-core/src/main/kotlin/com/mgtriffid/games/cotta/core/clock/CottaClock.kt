package com.mgtriffid.games.cotta.core.clock

/**
 * Allows `CottaSystem`s to access current logical time reliably.
 *
 * Simulation has to be deterministic, and client has not one but two
 * simulations running, so we cannot just use `System.currentTimeMillis()` to
 * obtain time required for game logic. Instead, systems should use this class
 * in order to not have desyncs.
 */
interface CottaClock {

    /**
     * Returns current logical time in milliseconds since the game started on
     * Server. Every logical tick the value returned by this method increments
     * by [com.mgtriffid.games.cotta.core.config.CottaConfig.tickLength].
     */
    fun time(): Long

    /**
     * Same as [deltaMs], but returns delta in seconds.
     */
    fun delta(): Float = deltaMs() / 1000f

    /**
     * Returns tick length in milliseconds. This may be useful for systems like
     * those controlling moving objects that have velocity:
     * ```kotlin
     * position.x += velocity.x * clock.delta()
     * ```
     *
     * Ideally, simulation should not depend on the tick length, so this method
     * helps to change the tick length without changing the simulation logic.
     */
    fun deltaMs(): Long
}
