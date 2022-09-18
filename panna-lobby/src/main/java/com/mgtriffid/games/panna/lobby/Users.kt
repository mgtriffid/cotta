package com.mgtriffid.games.panna.lobby

class Users {
    private val users: List<User> = listOf(
        User(Username("Abel"), Password("1234")),
        User(Username("Baker"), Password("5678")),
        User(Username("Charlie"), Password("qwerty"))
    )

    fun auth(username: Username, password: Password): Boolean {
        return users.find { it.username == username }?.password == password
    }
}

class User(
    val username: Username,
    val password: Password
)

data class Username(val username: String)
data class Password(val password: String)
