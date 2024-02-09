package com.mgtriffid.games.cotta.core.serialization.maps

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.esotericsoftware.kryo.serializers.CollectionSerializer
import com.esotericsoftware.kryo.serializers.MapSerializer
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.id.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.entities.id.PredictedEntityId
import com.mgtriffid.games.cotta.core.entities.id.StaticEntityId
import com.mgtriffid.games.cotta.core.registry.StringComponentKey
import com.mgtriffid.games.cotta.core.registry.StringEffectKey
import com.mgtriffid.games.cotta.core.serialization.SnapsSerialization
import com.mgtriffid.games.cotta.core.serialization.dto.EntityIdDto
import com.mgtriffid.games.cotta.core.serialization.dto.EntityOwnedByDto
import com.mgtriffid.games.cotta.core.serialization.dto.MetaEntityPlayerIdDto
import com.mgtriffid.games.cotta.core.serialization.dto.PlayersSawTicksDto
import com.mgtriffid.games.cotta.core.serialization.dto.TraceElementDtoKind
import com.mgtriffid.games.cotta.core.serialization.maps.dto.*
import com.mgtriffid.games.cotta.core.serialization.maps.recipe.*

private val logger = mu.KotlinLogging.logger {}

class MapsSnapsSerialization : SnapsSerialization<MapsStateRecipe, MapsDeltaRecipe> {
    // this is not thread safe
    private val kryo = Kryo()

    init {
        kryo.register(MapsComponentDeltaRecipeDto::class.java)
        kryo.register(MapsComponentRecipeDto::class.java)
        kryo.register(MapsChangedEntityRecipeDto::class.java)
        kryo.register(MapsDeltaRecipeDto::class.java)
        kryo.register(MapsEntityRecipeDto::class.java)
        kryo.register(MapsStateRecipeDto::class.java)
        kryo.register(EntityOwnedByDto::class.java)
        kryo.register(ArrayList::class.java, CollectionSerializer<ArrayList<Any?>>())
        kryo.register(HashMap::class.java, MapSerializer<HashMap<String, Any?>>())
        kryo.register(LinkedHashMap::class.java, MapSerializer<LinkedHashMap<String, Any?>>())
        kryo.register(EntityIdDto::class.java)
        kryo.register(EntityIdDto.Kind::class.java)
        kryo.register(MetaEntityPlayerIdDto::class.java)
        kryo.register(MapsCreateEntityTraceDto::class.java)
        kryo.register(MapsCreateEntityTracesDto::class.java)
        kryo.register(MapsCreatedEntitiesWithTracesRecipeDto::class.java)
        kryo.register(PlayersSawTicksDto::class.java)
        kryo.register(MapsCottaTraceDto::class.java)
        kryo.register(MapsCottaTraceElementDto::class.java)
        kryo.register(TraceElementDtoKind::class.java)
        kryo.register(MapsEffectRecipeDto::class.java)
        // TODO: these clearly should not be here, consider refactoring
        kryo.register(Entity.OwnedBy.Player::class.java)
        kryo.register(PlayerId::class.java)
        kryo.register(Entity.OwnedBy.System::class.java)
    }

    override fun serializeDeltaRecipe(recipe: MapsDeltaRecipe): ByteArray {
        val output = Output(4096, 1024 * 1024)
        kryo.writeObject(output, recipe.toDto())
        return output.toBytes()
    }

    override fun deserializeDeltaRecipe(bytes: ByteArray): MapsDeltaRecipe {
        return kryo.readObject(Input(bytes), MapsDeltaRecipeDto::class.java).toRecipe()
    }

    override fun serializeStateRecipe(recipe: MapsStateRecipe): ByteArray {
        val output = Output(64, 1024 * 1024)
        kryo.writeObject(output, recipe.toDto())
        return output.toBytes()
    }

    override fun deserializeStateRecipe(bytes: ByteArray): MapsStateRecipe {
        return kryo.readObject(Input(bytes), MapsStateRecipeDto::class.java).toRecipe()
    }

    override fun serializeEntityId(entityId: EntityId): ByteArray {
        val output = Output(64, 1024 * 1024)
        kryo.writeObject(output, entityId.toDto())
        return output.toBytes()
    }

    override fun deserializeEntityId(bytes: ByteArray): EntityId {
        return kryo.readObject(Input(bytes), EntityIdDto::class.java).toEntityId()
    }

    override fun serializeMetaEntityId(entityId: EntityId, playerId: PlayerId): ByteArray {
        val output = Output(64, 1024 * 1024)
        kryo.writeObject(output, MetaEntityPlayerIdDto()
            .also {
            it.entityId = entityId.toDto()
            it.playerId = playerId.id
        })
        return output.toBytes()
    }

    override fun deserializeMetaEntityId(bytes: ByteArray): Pair<EntityId, PlayerId> {
        val dto = kryo.readObject(Input(bytes), MetaEntityPlayerIdDto::class.java)
        return Pair(dto.entityId.toEntityId(), PlayerId(dto.playerId))
    }

    override fun serializeEntityCreationTraces(traces: List<Pair<MapsTraceRecipe, EntityId>>): ByteArray {
        val output = Output(64, 1024 * 1024)
        val entries = ArrayList<MapsCreateEntityTraceDto>()
        traces.forEach { (trace, entityId) -> entries.add(
            MapsCreateEntityTraceDto().also {
                it.trace = trace.toDto()
                it.entityId = entityId.toDto()
            }
        ) }
        kryo.writeObject(output, MapsCreateEntityTracesDto()
            .also { it.traces = entries })
        return output.toBytes()
    }

    override fun serializeEntityCreationTracesV2(createdEntities: MapsCreatedEntitiesWithTracesRecipe): ByteArray {
        val dto =
            MapsCreatedEntitiesWithTracesRecipeDto()
        val tracesDtos =  ArrayList<MapsCreateEntityTraceDto>()
        createdEntities.traces.forEach { (trace, entityId) ->
            tracesDtos.add(
                MapsCreateEntityTraceDto().also {
                it.trace = trace.toDto()
                it.entityId = entityId.toDto()
            })
        }
        dto.traces = tracesDtos
        val predictedEntitiesIds = HashMap<EntityIdDto, EntityIdDto>()
        createdEntities.mappedPredictedIds.forEach { (authoritativeEntityId, predictedEntityId) ->
            predictedEntitiesIds[authoritativeEntityId.toDto()] = predictedEntityId.toDto()
        }
        dto.predictedEntitiesIds = predictedEntitiesIds
        val output = Output(64, 1024 * 1024)
        kryo.writeObject(output, dto)
        return output.toBytes()
    }

    override fun deserializeEntityCreationTraces(bytes: ByteArray): List<Pair<MapsTraceRecipe, EntityId>> {
        val dto = kryo.readObject(Input(bytes), MapsCreateEntityTracesDto::class.java)
        return dto.traces.map {
            val trace: MapsCottaTraceDto = it.trace
            val entityIdDto = it.entityId
            Pair(
                MapsTraceRecipe(
                    trace.elements.map { it: MapsCottaTraceElementDto ->
                        when (it.kind) {
                            TraceElementDtoKind.INPUT -> MapsTraceElementRecipe.MapsInputTraceElementRecipe(
                                it.entityId.toEntityId()
                            )
                            TraceElementDtoKind.EFFECT -> MapsTraceElementRecipe.MapsEffectTraceElementRecipe(
                                it.data.toRecipe()
                            )
                            TraceElementDtoKind.ENTITY_PROCESSING -> TODO()
                        }
                    }
                ),
                entityIdDto.toEntityId())
        }
    }

    override fun deserializeEntityCreationTracesV2(bytes: ByteArray): MapsCreatedEntitiesWithTracesRecipe {
        val dto = kryo.readObject(Input(bytes), MapsCreatedEntitiesWithTracesRecipeDto::class.java)
        val traces = dto.traces.map {
            val trace: MapsCottaTraceDto = it.trace
            val entityIdDto = it.entityId
            Pair(
                MapsTraceRecipe(
                    trace.elements.map { it: MapsCottaTraceElementDto ->
                        when (it.kind) {
                            TraceElementDtoKind.INPUT -> MapsTraceElementRecipe.MapsInputTraceElementRecipe(
                                it.entityId.toEntityId()
                            )
                            TraceElementDtoKind.EFFECT -> MapsTraceElementRecipe.MapsEffectTraceElementRecipe(
                                it.data.toRecipe()
                            )
                            TraceElementDtoKind.ENTITY_PROCESSING -> TODO()
                        }
                    }
                ),
                entityIdDto.toEntityId())
        }
        val mappedPredictedIds = HashMap<AuthoritativeEntityId, PredictedEntityId>()
        dto.predictedEntitiesIds.forEach { (authoritativeEntityIdDto, predictedEntityIdDto) ->
            mappedPredictedIds[authoritativeEntityIdDto.toEntityId() as AuthoritativeEntityId] = predictedEntityIdDto.toEntityId() as PredictedEntityId
        }
        return MapsCreatedEntitiesWithTracesRecipe(traces, mappedPredictedIds)
    }

    override fun serializePlayersSawTicks(playersSawTicks: Map<PlayerId, Long>): ByteArray {
        val output = Output(64, 1024 * 1024)
        val dto = PlayersSawTicksDto()
        dto.playersSawTicks = playersSawTicks.mapKeys { (playerId, _) ->
            playerId.id
        }
        kryo.writeObject(output, dto)
        return output.toBytes()
    }

    override fun deserializePlayersSawTicks(bytes: ByteArray): Map<PlayerId, Long> {
        return kryo.readObject(
            Input(bytes),
            PlayersSawTicksDto::class.java
        ).playersSawTicks.mapKeys { (playerId, _) ->
            PlayerId(playerId)
        }
    }
}

// <editor-fold desc="Converters">
fun MapsComponentDeltaRecipe.toDto(): MapsComponentDeltaRecipeDto {
    val ret = MapsComponentDeltaRecipeDto()
    ret.key = componentKey.name
    ret.data = HashMap(this.data)
    return ret
}

fun MapsComponentRecipe.toDto(): MapsComponentRecipeDto {
    val ret = MapsComponentRecipeDto()
    ret.key = componentKey.name
    ret.data = HashMap(this.data)
    return ret
}

fun MapsChangedEntityRecipe.toDto(): MapsChangedEntityRecipeDto {
    val ret = MapsChangedEntityRecipeDto()
    ret.entityId = entityId.toDto()
    ret.addedComponents = ArrayList(this.addedComponents.map(MapsComponentRecipe::toDto))
    ret.removedComponents = ArrayList(removedComponents.map { it.name })
    ret.changedComponents = ArrayList(this.changedComponents.map(MapsComponentDeltaRecipe::toDto))
    return ret
}

fun MapsDeltaRecipe.toDto(): MapsDeltaRecipeDto {
    val ret = MapsDeltaRecipeDto()
    ret.addedEntities = ArrayList(addedEntities.map { it.toDto() })
    ret.changedEntities = ArrayList(changedEntities.map { it.toDto() })
    ret.removedEntitiesIds = ArrayList(removedEntitiesIds.map { it.toDto() })
    return ret
}

fun MapsEntityRecipe.toDto(): MapsEntityRecipeDto {
    val ret = MapsEntityRecipeDto()
    ret.entityId = entityId.toDto()
    ret.ownedBy = ownedBy.toDto()
    ret.components = ArrayList(components.map { it.toDto() })
    ret.inputComponents = ArrayList(inputComponents.map { it.name })
    return ret
}

fun MapsStateRecipe.toDto(): MapsStateRecipeDto {
    val ret = MapsStateRecipeDto()
    ret.entities = ArrayList(entities.map { it.toDto() })
    return ret
}

fun MapsComponentDeltaRecipeDto.toRecipe() = MapsComponentDeltaRecipe(
    componentKey = StringComponentKey(key),
    data = data
)

fun MapsComponentRecipeDto.toRecipe() = MapsComponentRecipe(
    componentKey = StringComponentKey(key),
    data = data
)

fun MapsChangedEntityRecipeDto.toRecipe() = MapsChangedEntityRecipe(
    entityId = entityId.toEntityId(),
    addedComponents = addedComponents.map { it.toRecipe() },
    changedComponents = changedComponents.map { it.toRecipe() },
    removedComponents = removedComponents.map(::StringComponentKey)
)

fun MapsDeltaRecipeDto.toRecipe() = MapsDeltaRecipe(
    addedEntities = addedEntities.map { it.toRecipe() },
    changedEntities = changedEntities.map { it.toRecipe() },
    removedEntitiesIds = removedEntitiesIds.map { it.toEntityId() }.toSet()
)

fun MapsEntityRecipeDto.toRecipe() = MapsEntityRecipe(
    entityId = entityId.toEntityId(),
    ownedBy = ownedBy.toOwnedBy(),
    components = components.map { it.toRecipe() },
    inputComponents = inputComponents.map(::StringComponentKey)
)

fun MapsStateRecipeDto.toRecipe() = MapsStateRecipe(
    entities = entities.map { it.toRecipe() }
)

fun EntityId.toDto(): EntityIdDto {
    return when (this) {
        is AuthoritativeEntityId -> EntityIdDto().also {
            it.id = id
            it.kind = EntityIdDto.Kind.AUTHORITATIVE
        }
        is PredictedEntityId -> EntityIdDto().also {
            it.id = id
            it.kind = EntityIdDto.Kind.PREDICTED
            it.playerId = playerId.id
        }
        is StaticEntityId -> EntityIdDto().also {
            it.id = id
            it.kind = EntityIdDto.Kind.STATIC
        }
    }
}

fun EntityIdDto.toEntityId(): EntityId {
    return when (kind) {
        EntityIdDto.Kind.AUTHORITATIVE -> AuthoritativeEntityId(id)
        EntityIdDto.Kind.PREDICTED -> PredictedEntityId(PlayerId(playerId), id)
        EntityIdDto.Kind.STATIC -> StaticEntityId(id)
        null -> throw IllegalStateException("${EntityIdDto::class.simpleName}.${EntityIdDto::kind.name} is null")
    }
}

fun Entity.OwnedBy.toDto(): EntityOwnedByDto {
    val ret = EntityOwnedByDto();
    when (this) {
        is Entity.OwnedBy.Player -> {
            ret.ownedBySystem = false
            ret.playerId = playerId.id
        }

        is Entity.OwnedBy.System -> {
            ret.ownedBySystem = true
            ret.playerId = 0
        }
    }
    return ret
}

fun EntityOwnedByDto.toOwnedBy(): Entity.OwnedBy {
    return if (ownedBySystem) {
        Entity.OwnedBy.System
    } else {
        Entity.OwnedBy.Player(PlayerId(playerId))
    }
}

fun MapsTraceRecipe.toDto(): MapsCottaTraceDto {
    val ret = MapsCottaTraceDto()
    ret.elements = elements.map { it.toDto() }
    return ret
}

private fun MapsTraceElementRecipe.toDto(): MapsCottaTraceElementDto {
    val ret = MapsCottaTraceElementDto()
    when (this) {
        is MapsTraceElementRecipe.MapsEffectTraceElementRecipe -> {
            ret.kind = TraceElementDtoKind.EFFECT
            ret.data = this.effectRecipe.toDto()
        }

        is MapsTraceElementRecipe.MapsEntityProcessingTraceElementRecipe -> TODO()
        is MapsTraceElementRecipe.MapsInputTraceElementRecipe -> {
            ret.kind = TraceElementDtoKind.INPUT
            ret.entityId = entityId.toDto()
        }
    }
    return ret
}

private fun MapsEffectRecipe.toDto(): MapsEffectRecipeDto {
    val ret = MapsEffectRecipeDto()
    ret.key = effectKey.name
    ret.data = HashMap(data)
    return ret
}

private fun MapsEffectRecipeDto.toRecipe(): MapsEffectRecipe {
    return MapsEffectRecipe(
        effectKey = StringEffectKey(key),
        data = data
    )
}
// </editor-fold>
