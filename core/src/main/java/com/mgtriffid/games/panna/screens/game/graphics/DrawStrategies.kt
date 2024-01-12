package com.mgtriffid.games.panna.screens.game.graphics

import com.mgtriffid.games.panna.screens.game.graphics.strategies.BulletStrategy
import com.mgtriffid.games.panna.screens.game.graphics.strategies.CharacterStrategy
import com.mgtriffid.games.panna.screens.game.graphics.strategies.SolidTerrainTileStrategy
import com.mgtriffid.games.panna.shared.BULLET_STRATEGY
import com.mgtriffid.games.panna.shared.CHARACTER_STRATEGY
import com.mgtriffid.games.panna.shared.SOLID_TERRAIN_TILE_STRATEGY

fun getStrategy(drawStrategyId: Int) = when (drawStrategyId) {
    SOLID_TERRAIN_TILE_STRATEGY -> SolidTerrainTileStrategy
    CHARACTER_STRATEGY -> CharacterStrategy
    BULLET_STRATEGY -> BulletStrategy
    else -> throw IllegalArgumentException("Unknown draw strategy id: $drawStrategyId")
}