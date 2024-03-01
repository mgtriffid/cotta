package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.Instruction
import com.mgtriffid.games.cotta.client.SimulationDirector
import jakarta.inject.Inject
import java.util.*

class SimulationDirectorImpl @Inject constructor(
    private val deltasPresent: DeltasPresent,
) : SimulationDirector {

    var currentState: CurrentState = CurrentState.Authoritative

    override fun instruct(tick: Long): List<Instruction> {
        val ret = LinkedList<Instruction>()
        val s = currentState
        var needToRecalculate = false
        if (s is CurrentState.Guessed) {
            for (t in s.authoritativeTick until tick) {
                if (deltasPresent.hasDelta(t)) {
                    ret.add(Instruction.IntegrateAuthoritative(t))
                    currentState = CurrentState.Guessed(t + 1)
                    needToRecalculate = true
                } else {
                    break
                }
            }
            currentState = if (caughtUp(tick)) {
                CurrentState.Authoritative
            } else {
                currentState
            }
        }
        if (currentState is CurrentState.Authoritative) {
            if (deltasPresent.hasDelta(tick)) {
                ret.add(Instruction.IntegrateAuthoritative(tick))
            } else {
                ret.add(Instruction.CopyAuthoritativeToGuessed)
                ret.add(Instruction.IntegrateGuessed(tick - 1))
                currentState = CurrentState.Guessed(tick)
            }
        } else {
            if (needToRecalculate) {
                ret.add(Instruction.CopyAuthoritativeToGuessed)
                repeat((tick - getAuthoritativeTick() + 1).toInt()) {
                    ret.add(Instruction.IntegrateGuessed(getAuthoritativeTick() - 1))
                }
            }
            ret.add(Instruction.IntegrateGuessed(getAuthoritativeTick() - 1))
        }
        return ret
    }

    private fun caughtUp(tick: Long) = getAuthoritativeTick() == tick

    private fun getAuthoritativeTick() = (currentState as CurrentState.Guessed).authoritativeTick
}

sealed interface CurrentState {
    data object Authoritative : CurrentState
    data class Guessed(val authoritativeTick: Long) : CurrentState
}
fun interface DeltasPresent {
    fun hasDelta(tick: Long): Boolean
}
