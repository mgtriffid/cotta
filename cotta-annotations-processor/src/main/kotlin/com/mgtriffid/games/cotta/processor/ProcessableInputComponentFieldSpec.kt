package com.mgtriffid.games.cotta.processor

import com.squareup.kotlinpoet.TypeName

data class ProcessableInputComponentFieldSpec(
    val name: String,
    val type: TypeName
)
