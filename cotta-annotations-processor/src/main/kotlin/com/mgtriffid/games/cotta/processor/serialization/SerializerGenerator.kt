package com.mgtriffid.games.cotta.processor.serialization

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.mgtriffid.games.cotta.core.serialization.bytes.ConversionUtils
import com.mgtriffid.games.cotta.processor.ProcessableComponentFieldSpec
import com.mgtriffid.games.cotta.processor.getProcessableComponentFieldSpecs
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

class SerializerGenerator(
    private val resolver: Resolver,
    private val codeGenerator: CodeGenerator
) {
    fun generate(components: List<KSClassDeclaration>) {
        components
            .filter { getProcessableComponentFieldSpecs(it).isNotEmpty() }
            .forEach(::generate)
    }

    private fun generate(component: KSClassDeclaration) {
        FileSpec.builder(getPackageName(component), getClassName(component))
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

    private fun serializeField(field: ProcessableComponentFieldSpec, offset: Int): String {
        val function = when (field.type) {
            INT -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::writeInt.name
            LONG -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::writeLong.name
            FLOAT -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::writeFloat.name
            DOUBLE -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::writeDouble.name
            BYTE -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::writeByte.name
            SHORT -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::writeShort.name
            else -> "TODO()//"
        }
        return "$function(bytes, component.${field.name}, $offset)"
    }

    private fun deserializeField(field: ProcessableComponentFieldSpec, offset: Int): String {
        val function = when (field.type) {
            INT -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::readInt.name
            LONG -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::readLong.name
            FLOAT -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::readFloat.name
            DOUBLE -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::readDouble.name
            BYTE -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::readByte.name
            SHORT -> ConversionUtils::class.qualifiedName + "." + ConversionUtils::readShort.name
            else -> "TODO()//"
        }
        return "val ${field.name} = $function(bytes, $offset)"
    }

    private fun getPackageName(component: KSClassDeclaration) = component.packageName.asString()

    private fun getClassName(component: KSClassDeclaration) =
        "${component.simpleName.asString()}Serializer"

    private fun declareBytes(component: KSClassDeclaration) = "val bytes = ByteArray(${getFullLength(component)})"

    private fun getFullLength(component: KSClassDeclaration) =
        getProcessableComponentFieldSpecs(component).sumOf { it.getByteLength() }
}

private fun ProcessableComponentFieldSpec.getByteLength() = when (type) {
    Int::class.asTypeName() -> 4
    else -> 4 // TODO
}
