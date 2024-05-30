package com.mgtriffid.games.cotta.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.mgtriffid.games.cotta.core.annotations.Historical
import com.mgtriffid.games.cotta.core.codegen.Constants.COMPONENTS_CLASS_SUFFIX
import com.mgtriffid.games.cotta.core.codegen.Constants.COPY_METHOD
import com.mgtriffid.games.cotta.core.codegen.Constants.FACTORY_METHOD_PREFIX
import com.mgtriffid.games.cotta.core.codegen.Constants.GET_COMPONENTS_METHOD
import com.mgtriffid.games.cotta.core.codegen.Constants.IMPL_SUFFIX
import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.MutableComponent
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.arrays.ComponentStorage
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.entities.impl.ComponentInternal
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import kotlin.reflect.KClass

// TODO this class is full of weird assumptions and should be refactored to return more specific objects defining the
//  components and their properties, it should not rely on naming conventions.
class ComponentProcessor(
    private val resolver: Resolver,
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) {
    fun process(game: KSClassDeclaration) {
        val components = getComponentInterfaces(game)
        components.forEach {
            writeImplementationV1(it)
            writeImplementationV2(it)
        }
        writeRegistry(components, game)
    }

    private fun getComponentInterfaces(game: KSClassDeclaration): List<KSClassDeclaration> {
        return resolver.getAllFiles()
            .flatMap { it.declarations }
            .filterIsInstance<KSClassDeclaration>()
            .filter {
                it.superTypes.any { superType ->
                    superType.resolve().declaration.qualifiedName?.asString() in
                        listOf(
                            Component::class.qualifiedName,
                            MutableComponent::class.qualifiedName
                        )
                }
            }
            .filter {
                it.packageName.asString()
                    .startsWith(game.packageName.asString())
            }.toList()
    }

    private fun writeImplementationV1(component: KSClassDeclaration) {
        val pkg = component.packageName.asString()
        val componentName = component.simpleName.asString()
        val properties: List<ProcessableComponentFieldSpec> =
            getProcessableComponentFieldSpecs(component)
        val fileSpecBuilder =
            FileSpec.builder(pkg, "${componentName}$IMPL_SUFFIX")
        if (properties.isNotEmpty()) {
            buildDataClassImplementation(
                fileSpecBuilder,
                componentName,
                component,
                properties
            )
        } else {
            buildSingletonImplementation(
                fileSpecBuilder,
                componentName,
                component
            )
        }
        fileSpecBuilder.build().writeTo(codeGenerator, false)
    }

    private fun writeImplementationV2(component: KSClassDeclaration) {
        // make a storage which is a class with a bunch of indices
        writeDataStorage(component)
        writeProxy(component)
    }

    private fun writeDataStorage(component: KSClassDeclaration) {
        if (isHistorical(component)) {
            logger.warn("Mutable components are not supported yet")
            writeHistoricalArraysDataStorage(component)
        } else {
            writeRegularArraysDataStorage(component)
        }
//        writeRegularArraysDataStorage(component)
    }

    private fun writeHistoricalArraysDataStorage(component: KSClassDeclaration) {
        val pkg = component.packageName.asString()
        val componentName = component.simpleName.asString()
        val fileSpecBuilder =
            FileSpec.builder(pkg, "${componentName}DataStorage")
        val properties: List<ProcessableComponentFieldSpec> =
            getProcessableComponentFieldSpecs(component)
        fileSpecBuilder.addType(
            buildHistoricalStorageType(
                componentName,
                component,
                properties
            )
        )
        fileSpecBuilder.build().writeTo(codeGenerator, false)
    }

    private fun buildHistoricalStorageType(
        componentName: String,
        component: KSClassDeclaration,
        properties: List<ProcessableComponentFieldSpec>
    ): TypeSpec {
        val builder = TypeSpec.classBuilder("${componentName}DataStorage")
        properties.forEach { fieldSpec ->
            builder.addProperty(writeHistoricalArrayStorage(fieldSpec))
            builder.addProperty(writeHistoricalArrayAccessors(fieldSpec))
        }
        builder.addProperty(tick())
        builder.addSuperinterface(
            ComponentStorage.HistoricalData::class.asTypeName().parameterizedBy(
                component.asStarProjectedType().toTypeName()
            )
        )
        builder.addFunction(grow(properties))
        builder.addFunction(set(properties, component))
        builder.addFunction(remove(properties))
        builder.addFunction(fGet(component))
        builder.addFunction(getHistorical(component))
        builder.addFunction(advance(properties, component))
        return builder.build()
    }

    private fun advance(
        properties: List<ProcessableComponentFieldSpec>,
        component: KSClassDeclaration
    ): FunSpec {
        return FunSpec.builder("advance")
            .addModifiers(KModifier.OVERRIDE)
            .addStatement("val newTick = (tick + 1) %% 64")
            .also { builder ->
                properties.forEach { fieldSpec ->
                    builder.addStatement(copyArray(fieldSpec))
                }
            }
            .addStatement("tick = newTick")
            .build()
    }

    private fun copyArray(fieldSpec: ProcessableComponentFieldSpec): String {
        val name = fieldSpec.name
        val arrayClass = getArrayClass(fieldSpec.type)
        return """
            if (${name}Array[tick].size != ${name}Array[newTick].size) {
                ${name}Array[newTick] = ${arrayClass.simpleName}(${name}Array[tick].size)
            }
            System.arraycopy(${name}Array[tick], 0, ${name}Array[newTick], 0, ${name}Array[tick].size)
        """.trimIndent()
    }

    private fun tick(): PropertySpec {
        return PropertySpec.builder("tick", Int::class, KModifier.PRIVATE)
            .initializer("0")
            .mutable()
            .build()
    }

    private fun writeHistoricalArrayStorage(fieldSpec: ProcessableComponentFieldSpec): PropertySpec {
        val arrayClass = getArrayClass(fieldSpec.type)
        val arrayOfArraysClass =
            Array::class.asClassName().parameterizedBy(arrayClass.asTypeName())
        return PropertySpec.builder(
            name = "${fieldSpec.name}Array",
            type = arrayOfArraysClass,
            KModifier.INTERNAL
        )
            .initializer("${arrayOfArraysClass}(64) { ${arrayClass.simpleName}(8) }")
            .build()
    }

    private fun writeHistoricalArrayAccessors(fieldSpec: ProcessableComponentFieldSpec): PropertySpec {
        val arrayClass = getArrayClass(fieldSpec.type)
        return PropertySpec.builder(
            name = fieldSpec.name,
            type = arrayClass,
            KModifier.INTERNAL
        )
            .mutable()
            .getter(
                FunSpec.getterBuilder()
                    .addStatement("return ${fieldSpec.name}Array[tick]")
                    .build()
            )
            .setter(
                FunSpec.setterBuilder()
                    .addParameter("value", arrayClass)
                    .addStatement("${fieldSpec.name}Array[tick] = value")
                    .build()
            )
            .build()
    }

    private fun writeArrayStorage(fieldSpec: ProcessableComponentFieldSpec): PropertySpec {
        val arrayClass = getArrayClass(fieldSpec.type)
        return PropertySpec.builder(
            name = fieldSpec.name,
            type = arrayClass,
            KModifier.INTERNAL
        )
            .mutable()
            .initializer("${arrayClass.simpleName}(8)")
            .build()
    }

    private fun writeRegularArraysDataStorage(component: KSClassDeclaration) {
        val pkg = component.packageName.asString()
        val componentName = component.simpleName.asString()
        val fileSpecBuilder =
            FileSpec.builder(pkg, "${componentName}DataStorage")
        val properties: List<ProcessableComponentFieldSpec> =
            getProcessableComponentFieldSpecs(component)
        fileSpecBuilder.addType(
            buildStorageType(
                componentName,
                component,
                properties
            )
        )
        fileSpecBuilder.build().writeTo(codeGenerator, false)
    }

    @OptIn(KspExperimental::class)
    private fun isHistorical(component: KSClassDeclaration): Boolean {
        return component.isAnnotationPresent(Historical::class)
    }

    private fun buildStorageType(
        componentName: String,
        component: KSClassDeclaration,
        properties: List<ProcessableComponentFieldSpec>
    ): TypeSpec {
        val builder = TypeSpec.classBuilder("${componentName}DataStorage")
        properties.forEach { fieldSpec ->
            builder.addProperty(writeArrayStorage(fieldSpec))
        }
        builder.addSuperinterface(
            ComponentStorage.Data::class.asTypeName().parameterizedBy(
                component.asStarProjectedType().toTypeName()
            )
        )


        builder.addFunction(grow(properties))
        builder.addFunction(set(properties, component))
        builder.addFunction(remove(properties))
        builder.addFunction(fGet(component))

        return builder.build()
    }

    private fun fGet(component: KSClassDeclaration): FunSpec {
        return FunSpec.builder("get")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("index", Int::class)
            .returns(component.asStarProjectedType().toTypeName())
            .addStatement("val ret = ${component.simpleName.asString()}Proxy(this)")
            .addStatement("ret.pointer = index")
            .also {
                if (isHistorical(component)) {
                    it.addStatement("ret.tick = tick.toLong()")
                }
            }
            .addStatement("return ret")
            .build()
    }

    private fun getHistorical(component: KSClassDeclaration): FunSpec {
        return FunSpec.builder("get")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("index", Int::class)
            .addParameter("tick", Long::class)
            .returns(component.asStarProjectedType().toTypeName())
            .addStatement("val ret = ${component.simpleName.asString()}Proxy(this)")
            .addStatement("ret.pointer = index")
            .addStatement("ret.tick = tick")
            .addStatement("return ret")
            .build()
    }

    private fun grow(properties: List<ProcessableComponentFieldSpec>): FunSpec {
        val builder = FunSpec.builder("grow")
        builder.addModifiers(KModifier.OVERRIDE)

        properties.forEach {
            builder.addStatement("val ${it.name}New = ${it.name}.copyOf(${it.name}.size * 2)")
            builder.addStatement("${it.name} = ${it.name}New")
        }
        return builder.build()
    }

    private fun set(
        properties: List<ProcessableComponentFieldSpec>,
        component: KSClassDeclaration
    ): FunSpec {
        val builder = FunSpec.builder("set")
            .addModifiers(KModifier.OVERRIDE)
            .addModifiers(KModifier.OPERATOR)
            .addParameter("index", Int::class)
            .addParameter(
                "component",
                component.asStarProjectedType().toTypeName()
            )

        properties.forEach {
            builder.addStatement(
                "${it.name}[index] = ${
                    when (it.type) {
                        EntityId::class.asTypeName() -> "component.${it.name}.id"
                        PlayerId::class.asTypeName() -> "component.${it.name}.id"
                        else -> "component.${it.name}"
                    }
                }"
            )
        }
        return builder.build()
    }

    private fun remove(
        properties: List<ProcessableComponentFieldSpec>
    ): FunSpec {
        val builder = FunSpec.builder("remove")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("index", Int::class)
            .addParameter("size", Int::class)

        properties.forEach {
            builder.addStatement("${it.name}[index] = ${it.name}[size]")
        }
        return builder.build()
    }

    private fun getArrayClass(type: TypeName): KClass<*> {
        return when (type) {
            Int::class.asTypeName() -> IntArray::class
            Float::class.asTypeName() -> FloatArray::class
            Double::class.asTypeName() -> DoubleArray::class
            Long::class.asTypeName() -> LongArray::class
            Short::class.asTypeName() -> ShortArray::class
            Byte::class.asTypeName() -> ByteArray::class
            EntityId::class.asTypeName() -> IntArray::class
            PlayerId::class.asTypeName() -> IntArray::class
            Boolean::class.asTypeName() -> BooleanArray::class
            else -> throw IllegalArgumentException("Unsupported type $type")
        }
    }

    private fun writeProxy(component: KSClassDeclaration) {
        val pkg = component.packageName.asString()
        val componentName = component.simpleName.asString()
        val fileSpecBuilder =
            FileSpec.builder(pkg, "${componentName}Proxy")
        val properties: List<ProcessableComponentFieldSpec> =
            getProcessableComponentFieldSpecs(component)
        fileSpecBuilder.addType(
            if (isHistorical(component)) {
                buildHistoricalProxyType(
                    componentName,
                    component,
                    properties
                )
            } else {
                buildProxyType(
                    componentName,
                    component,
                    properties
                )
            }
        )
        fileSpecBuilder.build().writeTo(codeGenerator, false)
    }

    private fun buildProxyType(
        componentName: String,
        component: KSClassDeclaration,
        properties: List<ProcessableComponentFieldSpec>
    ): TypeSpec {
        val builder = TypeSpec.classBuilder("${componentName}Proxy")
        builder.addProperty(
            PropertySpec.builder(
                "storage",
                ClassName(
                    component.packageName.asString(),
                    "${componentName}DataStorage"
                )
            )
                .initializer("storage")
                .build()
        )
        builder.primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter(
                    ParameterSpec.builder(
                        name = "storage",
                        type = ClassName(
                            component.packageName.asString(),
                            "${componentName}DataStorage"
                        )
                    ).build()
                )
                .build()
        )
        builder.addSuperinterface(component.asStarProjectedType().toTypeName())
        builder.addProperty(
            PropertySpec.builder("pointer", Int::class.asTypeName())
                .initializer("0")
                .mutable()
                .build()
        )
        properties.forEach { fieldSpec ->
            builder.addProperty(writeProxyProperty(fieldSpec))
        }
        if (properties.any { it.isMutable }) {
            builder.addFunction(
                FunSpec.builder(COPY_METHOD).addStatement("return this")
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(
                        component.asStarProjectedType().toTypeName()
                    )
                    .build()
            )
        }
        builder.addProperty(
            PropertySpec.builder("entities", IntArray::class, KModifier.PRIVATE)
                .initializer("IntArray(8)")
                .build()
        )

        return builder.build()
    }

    private fun buildHistoricalProxyType(
        componentName: String,
        component: KSClassDeclaration,
        properties: List<ProcessableComponentFieldSpec>
    ): TypeSpec {
        val builder = TypeSpec.classBuilder("${componentName}Proxy")
        builder.addProperty(
            PropertySpec.builder(
                "storage",
                ClassName(
                    component.packageName.asString(),
                    "${componentName}DataStorage"
                )
            )
                .initializer("storage")
                .build()
        )
        builder.primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter(
                    ParameterSpec.builder(
                        name = "storage",
                        type = ClassName(
                            component.packageName.asString(),
                            "${componentName}DataStorage"
                        )
                    ).build()
                )
                .build()
        )
        builder.addSuperinterface(component.asStarProjectedType().toTypeName())
        builder.addProperty(
            PropertySpec.builder("pointer", Int::class.asTypeName())
                .initializer("0")
                .mutable()
                .build()
        )
        builder.addProperty(
            PropertySpec.builder("tick", Long::class.asTypeName())
                .initializer("0")
                .mutable()
                .build()
        )

        if (properties.any { it.isMutable }) {
            builder.addFunction(
                FunSpec.builder(COPY_METHOD).addStatement("return this")
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(
                        component.asStarProjectedType().toTypeName()
                    )
                    .build()
            )
        }

        properties.forEach { fieldSpec ->
            builder.addProperty(writeHistoricalProxyProperty(fieldSpec))
        }

        builder.addProperty(
            PropertySpec.builder("entities", IntArray::class, KModifier.PRIVATE)
                .initializer("IntArray(8)")
                .build()
        )

        return builder.build()
    }

    private fun writeProxyProperty(fieldSpec: ProcessableComponentFieldSpec): PropertySpec {
        val builder = PropertySpec.builder(
            name = fieldSpec.name,
            type = fieldSpec.type,
            KModifier.OVERRIDE
        )
        builder
            .getter(
                FunSpec.getterBuilder()
                    .addStatement(
                        when (fieldSpec.type) {
                            EntityId::class.asTypeName() -> "return EntityId(storage.${fieldSpec.name}[pointer])"
                            PlayerId::class.asTypeName() -> "return PlayerId(storage.${fieldSpec.name}[pointer])"
                            else -> "return storage.${fieldSpec.name}[pointer]"
                        }
                    ).build()
            )
            .build()

        if (fieldSpec.isMutable) {
            builder.mutable()

            builder.setter(
                FunSpec.setterBuilder()
                    .addParameter("value", fieldSpec.type)
                    .addStatement("storage.${fieldSpec.name}[pointer] = value")
                    .build()
            )
        }
        return builder.build()
    }

    private fun writeHistoricalProxyProperty(fieldSpec: ProcessableComponentFieldSpec): PropertySpec {
        val builder = PropertySpec.builder(
            name = fieldSpec.name,
            type = fieldSpec.type,
            KModifier.OVERRIDE
        )
        builder
            .getter(
                FunSpec.getterBuilder()
                    .addStatement("val tickIndex = (tick %% 64).toInt()")
                    .addStatement(
                        when (fieldSpec.type) {
                            EntityId::class.asTypeName() -> "return EntityId(storage.${fieldSpec.name}Array[tickIndex][pointer])"
                            PlayerId::class.asTypeName() -> "return PlayerId(storage.${fieldSpec.name}Array[tickIndex][pointer])"
                            else -> "return storage.${fieldSpec.name}Array[tickIndex][pointer]"
                        }
                    ).build()
            )
            .build()

        if (fieldSpec.isMutable) {
            builder.mutable()

            builder.setter(
                FunSpec.setterBuilder()
                    .addParameter("value", fieldSpec.type)
                    .addStatement("val tickIndex = (tick %% 64).toInt()")
                    .addStatement("storage.${fieldSpec.name}Array[tickIndex][pointer] = value")
                    .build()
            )
        }
        return builder.build()
    }

    private fun buildSingletonImplementation(
        fileSpecBuilder: FileSpec.Builder,
        componentName: String,
        component: KSClassDeclaration
    ) {
        val name = "${componentName}$IMPL_SUFFIX"
        fileSpecBuilder.addFunction(
            FunSpec.builder("$FACTORY_METHOD_PREFIX${componentName}")
                .addModifiers(KModifier.PUBLIC)
                .returns(component.asStarProjectedType().toTypeName())
                .addStatement("return $name")
                .build()
        )
            .addType(
                TypeSpec.objectBuilder(name)
                    .addSuperinterface(
                        component.asStarProjectedType().toTypeName()
                    )
                    .addSuperinterface(ComponentInternal::class.asTypeName())
                    /*.addFunction(
                        FunSpec.builder(COPY_METHOD).addStatement("return this")
                            .addModifiers(KModifier.OVERRIDE)
                            .returns(
                                component.asStarProjectedType().toTypeName()
                            )
                            .build()
                    )
    */.build()
            )
    }


    private fun buildDataClassImplementation(
        fileSpecBuilder: FileSpec.Builder,
        componentName: String,
        component: KSClassDeclaration,
        properties: List<ProcessableComponentFieldSpec>
    ) {
        fileSpecBuilder
            .addFunction(factoryMethod(componentName, component, properties))
            .addType(implementation(componentName, component, properties))
    }

    private fun implementation(
        componentName: String,
        component: KSClassDeclaration,
        properties: List<ProcessableComponentFieldSpec>
    ) = TypeSpec.classBuilder("${componentName}${IMPL_SUFFIX}")
        .addSuperinterface(component.asStarProjectedType().toTypeName())
        .addSuperinterface(ComponentInternal::class.asTypeName())
        .addModifiers(KModifier.DATA, KModifier.INTERNAL)
        .primaryConstructor(
            implPrimaryConstructor(properties)
        )
        .also {
            if (properties.any { it.isMutable }) {
                it.addFunction(
                    copy(properties, component)
                )
            }
        }
        .addProperties(
            properties.map { prop ->
                PropertySpec.builder(prop.name, prop.type)
                    .initializer(prop.name)
                    .addModifiers(KModifier.OVERRIDE)
                    .also { if (prop.isMutable) it.mutable() }
                    .build()
            }
        )
        .build()

    private fun copy(
        properties: List<ProcessableComponentFieldSpec>,
        component: KSClassDeclaration
    ) = FunSpec.builder(COPY_METHOD).addStatement(
        if (properties.none { p -> p.isMutable })
            "return this"
        else
            "return this.copy(${
                properties.filter { p -> p.isMutable }
                    .joinToString(", ") { "${it.name} = ${it.name}" }
            })"
    )
        .addModifiers(KModifier.OVERRIDE)
        .returns(component.asStarProjectedType().toTypeName())
        .build()

    private fun implPrimaryConstructor(properties: List<ProcessableComponentFieldSpec>) =
        FunSpec.constructorBuilder()
            .addParameters(
                properties.map { prop ->
                    ParameterSpec.builder(prop.name, prop.type)
                        .build()
                }
            )
            .build()

    private fun factoryMethod(
        componentName: String,
        component: KSClassDeclaration,
        properties: List<ProcessableComponentFieldSpec>
    ) = FunSpec.builder("$FACTORY_METHOD_PREFIX$componentName")
        .addModifiers(KModifier.PUBLIC)
        .returns(component.asStarProjectedType().toTypeName())
        .addParameters(
            properties.map { spec ->
                ParameterSpec.builder(spec.name, spec.type).build()
            }
        )
        .addStatement(
            "return $componentName$IMPL_SUFFIX(${
                properties.joinToString(
                    ", "
                ) { it.name }
            })"
        )
        .build()

    private fun writeRegistry(
        components: List<KSClassDeclaration>,
        game: KSClassDeclaration
    ) {
        if (components.isEmpty()) return
        val pkg = game.packageName.asString()
        val gameName = game.simpleName.asString()
        val fileSpecBuilder = FileSpec.builder(pkg, "$gameName$IMPL_SUFFIX")
        fileSpecBuilder.addType(
            TypeSpec.classBuilder("$gameName$COMPONENTS_CLASS_SUFFIX")
                .addFunction(
                    FunSpec.builder(GET_COMPONENTS_METHOD)
                        .returns(
                            List::class.asTypeName().parameterizedBy(
                                ClassName(
                                    KClass::class.java.`package`.name,
                                    KClass::class.simpleName!!
                                ).parameterizedBy(STAR)
                            )
                        )
                        .addModifiers(KModifier.PUBLIC)
                        .addStatement("return listOf(" +
                            components.joinToString(", ") { "${it.qualifiedName!!.asString()}::class" } +
                            ")")
                        .build()
                ).build()
        )
        fileSpecBuilder.build().writeTo(codeGenerator, false)
    }
}
