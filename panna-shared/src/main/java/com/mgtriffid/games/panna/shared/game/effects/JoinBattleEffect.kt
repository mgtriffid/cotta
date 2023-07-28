package com.mgtriffid.games.panna.shared.game.effects

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Entity

data class JoinBattleEffect(val ownedBy: Entity.OwnedBy) : CottaEffect
