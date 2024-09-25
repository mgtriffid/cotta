package com.mgtriffid.games.cotta.client

import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.input.ClientInputId

/**
 * Responsible for simulations that we deem real. Not necessarily authoritative
 * though: it may very well be guessed.
 * This type DOES care about authoritative and guessed simulation;
 * It DOES NOT do anything related to prediction, that's handled in CottaClient.
 */
interface Simulations {
    fun simulate()
    fun getLastConfirmedInput(): ClientInputId
    fun getLastSimulationKind(): SimulationKind
    fun getState(): CottaState
    fun hopeless(): Boolean
    enum class SimulationKind {
        AUTHORITATIVE,
        GUESSED,
    }
}
