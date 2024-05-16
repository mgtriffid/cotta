package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.core.entities.Component

@com.mgtriffid.games.cotta.core.annotations.Component
interface GraverobberNpcComponent : Component {
    object Instance: GraverobberNpcComponent

    companion object {
        fun create(): GraverobberNpcComponent {
            return Instance
        }
    }
}
