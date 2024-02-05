package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.id.EntityId

@com.mgtriffid.games.cotta.Component
interface BulletComponent: Component<BulletComponent> {
    @ComponentData val shooterId: EntityId
}
