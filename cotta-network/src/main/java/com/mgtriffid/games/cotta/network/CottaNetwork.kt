package com.mgtriffid.games.cotta.network

interface CottaNetwork {
    fun createServerNetwork(): CottaServerNetwork
    fun createClientNetwork(): CottaClientNetwork
}
