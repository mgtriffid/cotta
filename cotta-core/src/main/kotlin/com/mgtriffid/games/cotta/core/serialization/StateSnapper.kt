package com.mgtriffid.games.cotta.core.serialization

import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.PlayerId

interface StateSnapper<
    SR: StateRecipe,
    DR: DeltaRecipe,
    PDR: PlayersDeltaRecipe
    > {
    fun snapState(entities: Entities): SR
    fun unpackStateRecipe(entities: Entities, recipe: SR)
    fun snapDelta(prev: Entities, curr: Entities): DR
    fun unpackDeltaRecipe(entities: Entities, recipe: DR)
    fun snapPlayersDelta(
        addedPlayers: List<PlayerId>,
//        removedEntitiesIds: Set<EntityId>
    ): PDR
    fun unpackPlayersDeltaRecipe(recipe: PDR): List<PlayerId>
}
