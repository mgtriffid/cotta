package com.mgtriffid.games.cotta.core.registry

interface EffectKey

data class StringEffectKey(val name: String): EffectKey

data class ShortEffectKey(val key: Short): EffectKey
