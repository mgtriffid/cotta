package com.mgtriffid.games.cotta.utils

import java.util.concurrent.ConcurrentLinkedQueue

fun <T> ConcurrentLinkedQueue<T>.drain(): Collection<T> {
    val ret = ArrayList<T>()
    val iterator = iterator()
    while (iterator.hasNext()) {
        ret.add(iterator.next())
        iterator.remove()
    }
    return ret
}

fun now() = System.currentTimeMillis()

fun divideRoundUp(dividend: Int, divisor: Int) = (dividend + divisor - 1) / divisor
