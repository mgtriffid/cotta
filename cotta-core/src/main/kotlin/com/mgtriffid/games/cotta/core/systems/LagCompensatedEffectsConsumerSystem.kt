package com.mgtriffid.games.cotta.core.systems

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.PlayerId

/**
 * Implement this interface to consume effects that are lag compensated. For
 * instance, if there's an effect meaning a hitscan weapon shot like
 * ```
 * interface RailgunEffect : CottaEffect {
 *     val originX: Float // position of the shooter
 *     val originY: Float
 *     val originZ: Float
 *     val pitch: Float // direction of the shot
 *     val yaw: Float
 *     val shooterPlayerId: PlayerId // a player who shot
 *     val shooterId: EntityId
 * }
 * ```
 * then this effect needs to be lag compensated. This means that the system
 * should query Entities of some previous state to find if there were any hits.
 * Implement this interface to make this system work, implement the [player]
 * method to find out which player is the owner of the effect. That is, who is
 * the player you want to take a state from the past for.
 *
 * IMPORTANT: never mutate the state of the game in this system, because the
 * state is from the past. Instead, fire new Effects. Like, if you did in fact
 * register a railgun hit, don't apply damage to the player, instead fire a
 * `createRailgunDamageEffect(damaged = foundEntity.id)` or something.
 */
interface LagCompensatedEffectsConsumerSystem<T: CottaEffect> : EffectsConsumerSystem<T>  {

    /**
     * Find out who is the player you want to take a state from the past for.
     * Following the example above with `RailgunEffect`, you would want to do
     * just
     * ```
     * override fun player(effect: RailgunEffect) = effect.shooterPlayerId
     * ```
     */
    fun player(effect: T) : PlayerId
}
