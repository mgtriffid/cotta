package com.mgtriffid.games.cotta.network.protocol.serialization

import com.mgtriffid.games.cotta.core.entities.Component

sealed interface ComponentDelta<T: Component<T>> {
    class Added<T: Component<T>>(val data: FullComponentData<T>): ComponentDelta<T>
    class Removed<T: Component<T>>: ComponentDelta<T>
    class Changed<T: Component<T>>(val data: ChangedComponentData<T>): ComponentDelta<T>
}

/**
 * Allows to reconstruct the whole state of particular Component. Used to transfer snapshot
 * of Entity as a whole when it is discovered or spawned.
 */
interface FullComponentData<T> {
    fun get(): T
}

/**
 * Allows to modify part of Component state. For example when something is moved to the left
 * then this allows to transfer change of xPos but leave yPos intact.
 */
interface ChangedComponentData<T> {
    fun apply(to: T)
}
