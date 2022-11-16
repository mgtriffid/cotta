package com.mgtriffid.games.panna.screens.menu

import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class MenuState {
    var state = MenuScreen.State.IDLE

    fun idle() {
        logger.debug { "Resetting menu back to IDLE" }
        state = MenuScreen.State.IDLE
    }

    fun startAuthorization() {
        logger.debug { "Starting authorization" }
        state = MenuScreen.State.AUTHORIZATION
    }

    fun startRetrievingCharacterList() {
        logger.info { "Retrieving character list" }
        state = MenuScreen.State.RETRIEVING_CHARACTER_LIST
    }

    fun authorizationFailed() {
        logger.debug { "Authorization failed" }
        state = MenuScreen.State.AUTHORIZATION_FAILED
    }

    fun characterListRetrieved() {
        logger.debug { "Character list retrieved" }
        state = MenuScreen.State.RETRIEVED_CHARACTER_LIST
    }

    fun failedToRetrieveCharactersList() {
        logger.debug { "Failed to retrieve character list" }
        state = MenuScreen.State.FAILED_TO_RETRIEVE_CHARACTERS_LIST
    }
}
