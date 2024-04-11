package com.mgtriffid.games.cotta.core.input

// TODO Maybe Short and then wrap around just to save 2 bytes?
@JvmInline value class ClientInputId(val id: Int) : Comparable<ClientInputId> {
    override fun compareTo(other: ClientInputId): Int {
        return id.compareTo(other.id)
    }
}
