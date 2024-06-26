package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.core.entities.Component

@com.mgtriffid.games.cotta.core.annotations.Component
interface SteamManPlayerComponent : Component<SteamManPlayerComponent> {
    object Instance: SteamManPlayerComponent

    companion object {
        fun create(): SteamManPlayerComponent {
            return Instance
        }
    }

    override fun copy() = this
}
