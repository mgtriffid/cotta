package com.mgtriffid.games.panna.shared.game.systems

import com.mgtriffid.games.cotta.core.annotations.Predicted
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.systems.InputProcessingSystem
import com.mgtriffid.games.panna.shared.game.components.input.JoinBattleMetaEntityInputComponent
import com.mgtriffid.games.panna.shared.game.effects.JoinBattleEffect
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

// This should somehow be marked as "predicted" because we want the Client to actually spawn a predicted entity for
// our player so that player could start controlling it right away.
// Unlike, for example, naive rockets: naive rockets get spawned on Server and then get sent to Client. Naive rockets
// don't have prediction.

// alright two possible cases. One - joining the battle is not predicted, another - it is predicted.
// if it's not predicted then the effect processing system doesn't need to kick in on client until we get info from server.
// overall everything that's not predicted should not be processed on Client prematurely; only when authoritative info comes
// from server.

// so where does that annotation go? I suppose right here. Because if we put it further like on the Effects Consumer
// then running this system on Client doesn't make sense at all. Effect is a consequence.
// Do we need to put @Predicted on all systems in chain? Like here, then Effect Consumer?
@Predicted
class JoinBattleSystem(
    private val entities: Entities,
    private val effectBus: EffectBus
) : InputProcessingSystem {
    override fun process(e: Entity) {
        if (e.hasInputComponent(JoinBattleMetaEntityInputComponent::class)) {
            val join = e.getInputComponent(JoinBattleMetaEntityInputComponent::class).join
            if (join) {
                // todo make sure it doesn't fire twice
                // when this fires then we record some context
                effectBus.fire(JoinBattleEffect(e.ownedBy))
            }
        }
    }
}
