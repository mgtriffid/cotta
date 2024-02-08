package com.mgtriffid.games.cotta.processor

import com.squareup.kotlinpoet.TypeName

data class ProcessableComponentFieldSpec(
    val name: String,
    val type: TypeName,
    val isMutable: Boolean
)
