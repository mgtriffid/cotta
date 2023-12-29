package com.mgtriffid.games.cotta.core.input.impl

import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.input.ClientInput

class ClientInputImpl(override val inputs: Map<EntityId, List<InputComponent<*>>>) : ClientInput
