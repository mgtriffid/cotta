package com.mgtriffid.games.panna.shared.utils

/**
 * 1 means clockwise, -1 means counterclockwise, 0 means collinear
 */
fun orientation(
    x1: Int, y1: Int,
    x2: Int, y2: Int,
    x3: Int, y3: Int
): Int {
    ((y2 - y1) * (x3 - x2) - (y3 - y2) * (x2 - x1)).let {
        return when {
            it > 0 -> 1
            it < 0 -> -1
            else -> 0
        }
    }
}

fun orientation(
    x1: Float, y1: Float,
    x2: Float, y2: Float,
    x3: Float, y3: Float
): Int {
    ((y2 - y1) * (x3 - x2) - (y3 - y2) * (x2 - x1)).let {
        return when {
            it > 0 -> 1
            it < 0 -> -1
            else -> 0
        }
    }
}

fun intersect(
    l1: Float, r1: Float, b1: Float, t1: Float,
    l2: Float, r2: Float, b2: Float, t2: Float,
): Boolean {
    return l1 < r2 && r1 > l2 && b1 < t2 && t1 > b2
}

object Orientation {
    const val CLOCKWISE = 1
    const val COUNTERCLOCKWISE = -1
    const val COLLINEAR = 0
}
