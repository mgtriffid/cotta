package com.mgtriffid.games.panna.shared.game.components.input

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.panna.shared.game.components.input.JoinBattleMetaEntityInputComponent.Companion.IDLE

@com.mgtriffid.games.cotta.Component
interface JoinBattleMetaEntityInputComponent : InputComponent<JoinBattleMetaEntityInputComponent> {
    @ComponentData
    val join: Boolean

    companion object {
        fun createBlank(): JoinBattleMetaEntityInputComponent = JoinBattleMetaEntityInputComponentImpl(IDLE)

        const val JOIN_BATTLE: Boolean = true
        const val IDLE = false
    }
}
