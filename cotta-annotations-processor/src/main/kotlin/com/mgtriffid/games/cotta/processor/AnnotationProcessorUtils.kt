package com.mgtriffid.games.cotta.processor

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ksp.toTypeName

fun getProcessableComponentFieldSpecs(component: KSClassDeclaration) =
    component.getDeclaredProperties().map { prop ->
        ProcessableComponentFieldSpec(prop.simpleName.asString(), prop.type.resolve().toTypeName(), prop.isMutable)
    }.toList()
