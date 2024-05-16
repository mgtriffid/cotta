package com.mgtriffid.games.cotta.core.entities

interface MutableComponent : Component {
    fun copy(): MutableComponent
}
