package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.impl.LocalPlayer.Data.Absent
import com.mgtriffid.games.cotta.client.impl.LocalPlayer.Data.Present
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import kotlin.reflect.KProperty1

class LocalPlayer {
    private var data: Data = Absent

    fun set(metaEntityId: EntityId, playerId: PlayerId) {
        data = Present(metaEntityId, playerId)
    }

    fun isReady(): Boolean = data is Present

    val playerId: PlayerId
        get() = get(Present::playerId)

    val metaEntityId: EntityId
        get() = get(Present::metaEntityId)

    private fun <C: Any> get(kProperty0: KProperty1<Present, C>) = data.let {
        when (it) {
            is Present -> kProperty0.get(it)
            Absent -> throw IllegalStateException("Local player is not ready")
        }
    }

    private sealed interface Data {
        data object Absent : Data
        data class Present(val metaEntityId: EntityId, val playerId: PlayerId) : Data
    }
}
