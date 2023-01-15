package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.core.annotations.Input
import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.panna.shared.game.MovementDirection

@Input
data class BossInputComponent(
    var movementDirection: MovementDirection,
    var jump: Boolean,
    var spellCast: SpellCast?
) : Component<BossInputComponent> {
    enum class SpellCast {
        FIREBALL,
        HEAL
    }

    override fun copy(): BossInputComponent {
        return BossInputComponent(movementDirection, jump, spellCast)
    }
}
