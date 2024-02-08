package com.mgtriffid.games.cotta.processor.serialization

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.serialization.bytes.ConversionUtils
import com.mgtriffid.games.cotta.processor.ProcessableComponentFieldSpec
import com.mgtriffid.games.cotta.processor.getProcessableComponentFieldSpecs
import com.mgtriffid.games.cotta.utils.divideRoundUp
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import javax.swing.text.html.parser.Entity

private const val DELTA_MASK_VARIABLE = "deltaMask"

class SerializerGenerator(
    private val resolver: Resolver,
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) {
    fun generate(components: List<KSClassDeclaration>) {
        components
            .filter { getProcessableComponentFieldSpecs(it).isNotEmpty() }
            .forEach(::generate)
    }

    private fun generate(component: KSClassDeclaration) {
        FileSpec.builder(getPackageName(component), getClassName(component))
            .addImport("kotlin.experimental", "or")
            .addImport("kotlin.experimental", "and")
            .addType(serializer(component))
            .build()
            .writeTo(codeGenerator, false)
    }

    private fun serializer(component: KSClassDeclaration) =
        TypeSpec.classBuilder(getClassName(component))
            .addFunction(
                serializeFunction(component)
            )
            .addFunction(
                deserializeFunction(component)
            )
            .addFunction(serializeDeltaFunction(component))
            .addFunction(deserializeDeltaFunction(component))
            .build()

    private fun deserializeFunction(component: KSClassDeclaration) = FunSpec.builder("deserialize")
        .addParameter("bytes", ByteArray::class)
        .returns(component.asStarProjectedType().toTypeName())
        .apply {
            val specs = getProcessableComponentFieldSpecs(component)
            var offset = 0
            specs.forEach { spec ->
                addStatement(deserializeField(spec, offset))
                offset += spec.getByteLength()
            }
            addStatement("return ${component.simpleName.asString()}Impl(${specs.joinToString(", ") { it.name }})")
        }
        .build()

    private fun serializeFunction(component: KSClassDeclaration) = FunSpec.builder("serialize")
        .addParameter("component", component.asStarProjectedType().toTypeName())
        .returns(ByteArray::class)
        .addStatement(declareBytes(component))
        .apply {
            val specs = getProcessableComponentFieldSpecs(component)
            var offset = 0
            specs.forEach { spec ->
                addStatement(serializeField(spec, offset))
                offset += spec.getByteLength()
            }
        }
        .addStatement("return bytes")
        .build()

    private fun serializeDeltaFunction(component: KSClassDeclaration): FunSpec {
        val mutableComponents = getMutableComponents(component)
        val deltaMaskSize = divideRoundUp(mutableComponents.size, 8)
        return FunSpec.builder("serializeDelta")
            .addParameter("component", component.asStarProjectedType().toTypeName())
            .addParameter("previous", component.asStarProjectedType().toTypeName())
            .returns(ByteArray::class)
            .addStatement(declareBytes(DELTA_MASK_VARIABLE, deltaMaskSize))
            .addStatement("var dataSizeCounter = 0")
            .apply {
                mutableComponents.forEachIndexed { i, spec ->
                    addCompareAndRecordDifferenceStatements(i, spec)
                }
            }
            .addStatement(declareBytes("$deltaMaskSize + dataSizeCounter"))
            .addStatement("System.arraycopy($DELTA_MASK_VARIABLE, 0, bytes, 0, $deltaMaskSize)")
            .addStatement("var offset = $deltaMaskSize")
            .apply {
                mutableComponents.forEach { spec ->
                    beginControlFlow("if (component.${spec.name} != previous.${spec.name})")
                    addStatement(serializeField(spec, "offset"))
                    addStatement("offset += ${spec.getByteLength()}")
                    endControlFlow()
                }
            }
            .addStatement("return bytes")
            .build()
    }

    private fun deserializeDeltaFunction(component: KSClassDeclaration): FunSpec {
        val mutableComponents = getMutableComponents(component)
        val deltaMaskSize = divideRoundUp(mutableComponents.size, 8)
        return FunSpec.builder("deserializeDelta")
            .addParameter("bytes", ByteArray::class)
            .addParameter("component", component.asStarProjectedType().toTypeName())
            .returns(UNIT)
            .addStatement(declareBytes(DELTA_MASK_VARIABLE, deltaMaskSize))
            .addStatement("System.arraycopy(bytes, 0, $DELTA_MASK_VARIABLE, 0, $deltaMaskSize)")
            .addStatement("var offset = $deltaMaskSize")
            .apply {
                mutableComponents.forEachIndexed { i, spec ->
                    beginControlFlow("if ($DELTA_MASK_VARIABLE[$i / 8] and (1 shl ($i %% 8)).toByte() != 0.toByte())")
                    addStatement(deserializeField(spec, "offset"))
                    addStatement("component.${spec.name} = ${spec.name}")
                    addStatement("offset += ${spec.getByteLength()}")
                    endControlFlow()
                }
            }
            .build()
    }

    private fun FunSpec.Builder.addCompareAndRecordDifferenceStatements(i: Int, spec: ProcessableComponentFieldSpec) {
        beginControlFlow("if (component.${spec.name} != previous.${spec.name})")
        addStatement("$DELTA_MASK_VARIABLE[$i / 8] = deltaMask[$i / 8] or (1 shl ($i %% 8)).toByte()")
        addStatement("dataSizeCounter += ${spec.getByteLength()}")
        endControlFlow()
    }

    private fun declareBytes(component: KSClassDeclaration) = declareBytes(getFullLength(component))

    private fun declareBytes(size: Int) = declareBytes("bytes", size)

    private fun declareBytes(varName: String, size: Int) = "val $varName = ByteArray($size)"

    private fun declareBytes(size: String) = "val bytes = ByteArray($size)"

    private fun getMutableComponents(component: KSClassDeclaration) = getProcessableComponentFieldSpecs(component)
        .filter { it.isMutable }

    private fun serializeField(field: ProcessableComponentFieldSpec, offset: Int): String {
        val function = when (field.type) {
            INT -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::writeInt.name
            LONG -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::writeLong.name
            FLOAT -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::writeFloat.name
            DOUBLE -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::writeDouble.name
            BYTE -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::writeByte.name
            SHORT -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::writeShort.name
            EntityId::class.asTypeName() -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::writeEntityId.name
//            ClassName("com.mgtriffid.games.cotta.core.entities.id", "EntityId") -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::writeEntityId.name
            else -> "TODO()//"
        }
        return "$function(bytes, component.${field.name}, $offset)"
    }

    private fun serializeField(field: ProcessableComponentFieldSpec, offsetVariable: String): String {
        val function = when (field.type) {
            INT -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::writeInt.name
            LONG -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::writeLong.name
            FLOAT -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::writeFloat.name
            DOUBLE -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::writeDouble.name
            BYTE -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::writeByte.name
            SHORT -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::writeShort.name
            EntityId::class.asTypeName() -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::writeEntityId.name
//            ClassName("com.mgtriffid.games.cotta.core.entities.id", "EntityId") -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::writeEntityId.name
            else -> "TODO()//"
        }
        return "$function(bytes, component.${field.name}, $offsetVariable)"
    }

    private fun deserializeField(field: ProcessableComponentFieldSpec, offset: Int): String {
        val function = when (val type = field.type) {
            INT -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::readInt.name
            LONG -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::readLong.name
            FLOAT -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::readFloat.name
            DOUBLE -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::readDouble.name
            BYTE -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::readByte.name
            SHORT -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::readShort.name
            EntityId::class.asTypeName() -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::readEntityId.name
//            ClassName("com.mgtriffid.games.cotta.core.entities.id", "EntityId") -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::readEntityId.name
            else -> "TODO()//".also { logger.warn(field.type.toString()) }
        }
        return "val ${field.name} = $function(bytes, $offset)"
    }

    private fun deserializeField(field: ProcessableComponentFieldSpec, offsetVariable: String): String {
        val function = when (field.type) {
            INT -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::readInt.name
            LONG -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::readLong.name
            FLOAT -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::readFloat.name
            DOUBLE -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::readDouble.name
            BYTE -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::readByte.name
            SHORT -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::readShort.name
            EntityId::class.asTypeName() -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::readEntityId.name
//            ClassName("com.mgtriffid.games.cotta.core.entities.id", "EntityId") -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::readEntityId.name
            else -> "TODO()//"
        }
        return "val ${field.name} = $function(bytes, $offsetVariable)"
    }

    private fun getPackageName(component: KSClassDeclaration) = component.packageName.asString()

    private fun getClassName(component: KSClassDeclaration) =
        "${component.simpleName.asString()}Serializer"

    private fun getFullLength(component: KSClassDeclaration) =
        getProcessableComponentFieldSpecs(component).sumOf { it.getByteLength() }
}

private fun ProcessableComponentFieldSpec.getByteLength() = when (type) {
    INT -> 4
    LONG -> 8
    FLOAT -> 4
    DOUBLE -> 8
    BYTE -> 1
    SHORT -> 2
    EntityId::class.asTypeName() -> 8
    else -> 4 // TODO
}
