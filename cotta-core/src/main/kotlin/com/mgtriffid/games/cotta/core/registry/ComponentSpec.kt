package com.mgtriffid.games.cotta.core.registry

interface ComponentSpec {
    val key: ComponentKey
    val fields: List<FieldSpec>
}

interface EffectSpec {
    val key: EffectKey
    val fields: List<FieldSpec>
}

interface FieldSpec {
    val type: FieldType
    val mutability: FieldMutability
}

enum class FieldType {
    BYTE,
    INT,
    FLOAT,
    BOOLEAN,
    LONG,
    DOUBLE,
    OWNED_BY, // TODO get rid of
    ENTITY_ID
}

enum class FieldMutability {
    MUTABLE,
    IMMUTABLE
}
