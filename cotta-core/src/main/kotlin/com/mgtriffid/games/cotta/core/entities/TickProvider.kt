package com.mgtriffid.games.cotta.core.entities


// GROOM unified name. Sometimes it's tick, sometimes tickProvider.
// TODO review usage: where do we ++, where do we get, where do we set. Maybe
//  different tick providers should have different interfaces.
interface TickProvider {
    var tick: Long
}
