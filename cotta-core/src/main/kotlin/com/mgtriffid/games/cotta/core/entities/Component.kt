package com.mgtriffid.games.cotta.core.entities

interface Component<T: Component<T>> {
    fun copy(): T
}
