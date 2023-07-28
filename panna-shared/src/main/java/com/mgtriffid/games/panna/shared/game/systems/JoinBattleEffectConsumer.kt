package com.mgtriffid.games.panna.shared.game.systems

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.effects.EffectsConsumer
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.panna.shared.game.components.DrawableComponent
import com.mgtriffid.games.panna.shared.game.components.ORIENTATION_LEFT
import com.mgtriffid.games.panna.shared.game.components.PannaTextureIds
import com.mgtriffid.games.panna.shared.game.components.PositionComponent
import com.mgtriffid.games.panna.shared.game.components.SteamManPlayerComponent
import com.mgtriffid.games.panna.shared.game.components.WalkingComponent
import com.mgtriffid.games.panna.shared.game.components.input.WalkingInputComponent
import com.mgtriffid.games.panna.shared.game.effects.JoinBattleEffect

class JoinBattleEffectConsumer(private val entities: Entities) : EffectsConsumer {
    override fun handleEffect(e: CottaEffect) {
        if (e is JoinBattleEffect) {
            val dude = entities.createEntity(ownedBy = e.ownedBy)
            dude.addComponent(PositionComponent.create(200, 250, ORIENTATION_LEFT))
            dude.addInputComponent(WalkingInputComponent::class)
            dude.addComponent(WalkingComponent.create(15))
            dude.addComponent(SteamManPlayerComponent.create())
            dude.addComponent(DrawableComponent.create(PannaTextureIds.TEXTURE_ID_PLAYER_ENTITY))
        }
    }
}
