package com.mgtriffid.games.cotta.core.simulation

import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.input.ClientInput
import com.mgtriffid.games.cotta.core.input.NonPlayerInput
import com.mgtriffid.games.cotta.core.input.PlayerInput
import mu.KotlinLogging
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance

private val logger = KotlinLogging.logger {}

interface SimulationInput {
    fun inputForPlayers(): Map<PlayerId, PlayerInput>

    fun nonPlayerInput(): NonPlayerInput

    fun playersSawTicks(): Map<PlayerId, Long>
}
