package com.mgtriffid.games.panna.devutils.animationplayer.gdxadapter

import com.badlogic.gdx.Game

class AnimationPlayerGdxGame : Game() {
    override fun create() {
        setScreen(AnimationPlayerScreen())
    }
}
