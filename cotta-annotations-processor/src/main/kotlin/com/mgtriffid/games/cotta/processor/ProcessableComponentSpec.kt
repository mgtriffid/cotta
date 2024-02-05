package com.mgtriffid.games.cotta.processor

import com.squareup.kotlinpoet.TypeName

data class ProcessableComponentSpec(
    val packageName: String,
    val className: String,
    val fields: List<ProcessableComponentFieldSpec>,
)

data class ProcessableComponentFieldSpec(
    val name: String,
    val type: TypeName,
    val isMutable: Boolean
)
