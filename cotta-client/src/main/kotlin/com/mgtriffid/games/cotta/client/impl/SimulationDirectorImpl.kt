package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.Instruction
import com.mgtriffid.games.cotta.client.SimulationDirector
import com.mgtriffid.games.cotta.core.entities.TickProvider
import java.util.*

class SimulationDirectorImpl(
    private val deltasPresent: DeltasPresent,
    private val tickProvider: TickProvider // TODO just a parameter to `instruct`
) : SimulationDirector {

    var currentState: CurrentState = CurrentState.Authoritative

    override fun instruct(): List<Instruction> {
        val ret = LinkedList<Instruction>()
        val s = currentState
        var needToRecalculate = false
        if (s is CurrentState.Guessed) {
            for (t in s.authoritativeTick until tickProvider.tick) {
                if (deltasPresent.hasDelta(t)) {
                    ret.add(Instruction.IntegrateAuthoritative(t))
                    currentState = CurrentState.Guessed(t + 1)
                    needToRecalculate = true
                } else {
                    break
                }
            }
            currentState = if (caughtUp()) {
                CurrentState.Authoritative
            } else {
                currentState
            }
        }
        if (currentState is CurrentState.Authoritative) {
            if (deltasPresent.hasDelta(tickProvider.tick)) {
                ret.add(Instruction.IntegrateAuthoritative(tickProvider.tick))
            } else {
                ret.add(Instruction.CopyAuthoritativeToGuessed)
                ret.add(Instruction.IntegrateGuessed(tickProvider.tick - 1))
                currentState = CurrentState.Guessed(tickProvider.tick)
            }
        } else {
            if (needToRecalculate) {
                ret.add(Instruction.CopyAuthoritativeToGuessed)
                repeat((tickProvider.tick - getAuthoritativeTick() + 1).toInt()) {
                    ret.add(Instruction.IntegrateGuessed(getAuthoritativeTick() - 1))
                }
            }
            ret.add(Instruction.IntegrateGuessed(getAuthoritativeTick() - 1))
        }
        return ret
    }

    private fun caughtUp() = getAuthoritativeTick() == tickProvider.tick

    private fun getAuthoritativeTick() = (currentState as CurrentState.Guessed).authoritativeTick
}

sealed interface CurrentState {
    data object Authoritative : CurrentState
    data class Guessed(val authoritativeTick: Long) : CurrentState
}
fun interface DeltasPresent {
    fun hasDelta(t: Long): Boolean
}
