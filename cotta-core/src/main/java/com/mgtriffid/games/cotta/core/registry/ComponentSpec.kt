package com.mgtriffid.games.cotta.core.registry

interface ComponentSpec {
    val key: ComponentKey
    val fields: List<FieldSpec>
}

interface FieldSpec {
    val type: FieldType
    val mutability: FieldMutability
}

enum class FieldType {
    INT,
    FLOAT,
    BOOLEAN,
    LONG,
    DOUBLE
}

enum class FieldMutability {
    MUTABLE,
    IMMUTABLE
}
