package com.mgtriffid.games.panna.screens.game.graphics.actors

import com.badlogic.gdx.scenes.scene2d.Actor
import com.mgtriffid.games.cotta.core.entities.Entity

abstract class PannaActor : Actor() {
    abstract fun update(entity: Entity)
}
