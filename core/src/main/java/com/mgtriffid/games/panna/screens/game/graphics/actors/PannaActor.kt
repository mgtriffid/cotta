package com.mgtriffid.games.panna.screens.game.graphics.actors

import com.badlogic.gdx.scenes.scene2d.Actor
import com.mgtriffid.games.cotta.core.entities.Entity

interface PannaActor {
    val actor: Actor
    fun update(entity: Entity)
}
