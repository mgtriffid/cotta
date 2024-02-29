package com.mgtriffid.games.cotta.client

/**
 *  state[N+1] = logic(state[N], input[N])
 */
interface SimulationDirector {
    fun instruct(tick: Long): List<Instruction>
}

sealed interface Instruction {
    data class IntegrateAuthoritative(val tick: Long) : Instruction
    data object CopyAuthoritativeToGuessed : Instruction
    data class IntegrateGuessed(val tick: Long) : Instruction
}
