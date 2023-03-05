package com.mgtriffid.games.cotta.core.serialization

import com.mgtriffid.games.cotta.core.entities.Component

interface ComponentSerializer {
    fun <T: Component<T>> delta(old: T, new: T): ComponentDelta<T>
}
