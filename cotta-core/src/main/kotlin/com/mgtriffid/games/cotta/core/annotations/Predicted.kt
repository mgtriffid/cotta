package com.mgtriffid.games.cotta.core.annotations

/**
 * Marks a [com.mgtriffid.games.cotta.core.systems.CottaSystem] that needs to be
 * predicted. If a System is marked with this annotation, it will be run in the
 * Prediction simulation on client, not only in the Authoritative simulation.
 */
@Target(AnnotationTarget.CLASS)
annotation class Predicted
