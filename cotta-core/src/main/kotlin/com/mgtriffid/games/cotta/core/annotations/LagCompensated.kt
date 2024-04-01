package com.mgtriffid.games.cotta.core.annotations

@Target(AnnotationTarget.CLASS, AnnotationTarget.VALUE_PARAMETER)
// BUG it looks like we've got some off-by-one error in lag comp: railgun visibly
// misses, need to shoot slightly ahead which is obvously incorrect. Need to dig.
annotation class LagCompensated
