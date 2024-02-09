package com.mgtriffid.games.cotta.core.registry

interface ComponentKey
data class StringComponentKey(val name: String): ComponentKey
data class ShortComponentKey(val key: Short): ComponentKey
