package com.mgtriffid.games.cotta.network

interface CottaServerNetwork {
    fun initialize()

    fun dispatch(data: String)
}
