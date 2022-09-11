package com.mgtriffid.games.panna.lobby

import java.util.HashMap

class Sessions {
    private val sessions = HashMap<SessionToken, Username>()

    operator fun set(token: SessionToken, value: Username) {
        sessions[token] = value
    }

    operator fun get(token: SessionToken) = sessions[token]
}
