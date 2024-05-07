package com.mgtriffid.games.cotta.core.serialization

import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.impl.EntitiesInternal

interface StateSnapper<
    SR: StateRecipe,
    DR: DeltaRecipe,
    PDR: PlayersDeltaRecipe
    > {
    fun snapState(entities: EntitiesInternal): SR
    fun unpackStateRecipe(entities: EntitiesInternal, recipe: SR)
    fun snapDelta(prev: EntitiesInternal, curr: EntitiesInternal): DR
    fun unpackDeltaRecipe(entities: EntitiesInternal, recipe: DR)
    fun snapPlayersDelta(
        addedPlayers: List<PlayerId>,
//        removedEntitiesIds: Set<EntityId>
    ): PDR
    fun unpackPlayersDeltaRecipe(recipe: PDR): List<PlayerId>
}
