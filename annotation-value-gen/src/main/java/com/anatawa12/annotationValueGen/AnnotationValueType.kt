package com.anatawa12.annotationValueGen

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.ArrayType
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror


sealed class AnnotationValueType<T : Any>(val name: kotlin.String) {
    open fun typeName(targetFormat: TargetFormat, errorHandler: ErrorHandler): TypeName? = typeName()
    protected open fun typeName(): TypeName? =
        error("typeName for unknown is not supported for $this")

    abstract fun literalOf(value: T): CodeBlock
    abstract fun fromValue(targetFormat: TargetFormat, name: kotlin.String): CodeBlock

    object Byte : AnnotationValueType<kotlin.Byte>("byte") {
        override fun typeName(): TypeName = TypeName.BYTE
        override fun literalOf(value: kotlin.Byte): CodeBlock = CodeBlock.of("\$L", value)
        override fun fromValue(targetFormat: TargetFormat, name: kotlin.String): CodeBlock = when (targetFormat) {
            TargetFormat.KotlinIrCompiler ->
                CodeBlock.of("(\$T)((\$T)\$N).getValue()", TypeName.BYTE, S.irConst, name)
            TargetFormat.KotlinDescriptor ->
                CodeBlock.of("((\$T)\$N).getValue()", S.byteValue, name)
            TargetFormat.AnnotationProcessor ->
                CodeBlock.of("(\$T)\$N.getValue()", TypeName.BYTE, name)
        }
    }

    object Short : AnnotationValueType<kotlin.Short>("short") {
        override fun typeName(): TypeName = TypeName.SHORT
        override fun literalOf(value: kotlin.Short): CodeBlock = CodeBlock.of("\$L", value)
        override fun fromValue(targetFormat: TargetFormat, name: kotlin.String): CodeBlock = when (targetFormat) {
            TargetFormat.KotlinIrCompiler ->
                CodeBlock.of("(\$T)((\$T)\$N).getValue()", TypeName.SHORT, S.irConst, name)
            TargetFormat.KotlinDescriptor ->
                CodeBlock.of("((\$T)\$N).getValue()", S.shortValue, name)
            TargetFormat.AnnotationProcessor ->
                CodeBlock.of("(\$T)\$N.getValue()", TypeName.SHORT, name)
        }
    }

    object Int : AnnotationValueType<kotlin.Int>("int") {
        override fun typeName(): TypeName = TypeName.INT
        override fun literalOf(value: kotlin.Int): CodeBlock = CodeBlock.of("\$L", value)
        override fun fromValue(targetFormat: TargetFormat, name: kotlin.String): CodeBlock = when (targetFormat) {
            TargetFormat.KotlinIrCompiler ->
                CodeBlock.of("(\$T)((\$T)\$N).getValue()", TypeName.INT, S.irConst, name)
            TargetFormat.KotlinDescriptor ->
                CodeBlock.of("((\$T)\$N).getValue()", S.intValue, name)
            TargetFormat.AnnotationProcessor ->
                CodeBlock.of("(\$T)\$N.getValue()", TypeName.INT, name)
        }
    }

    object Long : AnnotationValueType<kotlin.Long>("long") {
        override fun typeName(): TypeName = TypeName.LONG
        override fun literalOf(value: kotlin.Long): CodeBlock = CodeBlock.of("\$LL", value)
        override fun fromValue(targetFormat: TargetFormat, name: kotlin.String): CodeBlock = when (targetFormat) {
            TargetFormat.KotlinIrCompiler ->
                CodeBlock.of("(\$T)((\$T)\$N).getValue()", TypeName.LONG, S.irConst, name)
            TargetFormat.KotlinDescriptor ->
                CodeBlock.of("((\$T)\$N).getValue()", S.longValue, name)
            TargetFormat.AnnotationProcessor ->
                CodeBlock.of("(\$T)\$N.getValue()", TypeName.LONG, name)
        }
    }

    object Char : AnnotationValueType<kotlin.Char>("char") {
        override fun typeName(): TypeName = TypeName.CHAR
        override fun literalOf(value: kotlin.Char): CodeBlock = CodeBlock.of("((char)\$L)", value.code)
        override fun fromValue(targetFormat: TargetFormat, name: kotlin.String): CodeBlock = when (targetFormat) {
            TargetFormat.KotlinIrCompiler ->
                CodeBlock.of("(\$T)((\$T)\$N).getValue()", TypeName.CHAR, S.irConst, name)
            TargetFormat.KotlinDescriptor ->
                CodeBlock.of("((\$T)\$N).getValue()", S.charValue, name)
            TargetFormat.AnnotationProcessor ->
                CodeBlock.of("(\$T)\$N.getValue()", TypeName.CHAR, name)
        }
    }

    object Float : AnnotationValueType<kotlin.Float>("float") {
        override fun typeName(): TypeName = TypeName.FLOAT
        override fun literalOf(value: kotlin.Float): CodeBlock = CodeBlock.of("\$Lf", value)
        override fun fromValue(targetFormat: TargetFormat, name: kotlin.String): CodeBlock = when (targetFormat) {
            TargetFormat.KotlinIrCompiler ->
                CodeBlock.of("(\$T)((\$T)\$N).getValue()", TypeName.FLOAT, S.irConst, name)
            TargetFormat.KotlinDescriptor ->
                CodeBlock.of("((\$T)\$N).getValue()", S.floatValue, name)
            TargetFormat.AnnotationProcessor ->
                CodeBlock.of("(\$T)\$N.getValue()", TypeName.FLOAT, name)
        }
    }

    object Double : AnnotationValueType<kotlin.Double>("double") {
        override fun typeName(): TypeName = TypeName.DOUBLE
        override fun literalOf(value: kotlin.Double): CodeBlock = CodeBlock.of("\$L", value)
        override fun fromValue(targetFormat: TargetFormat, name: kotlin.String): CodeBlock = when (targetFormat) {
            TargetFormat.KotlinIrCompiler ->
                CodeBlock.of("(\$T)((\$T)\$N).getValue()", TypeName.DOUBLE, S.irConst, name)
            TargetFormat.KotlinDescriptor ->
                CodeBlock.of("((\$T)\$N).getValue()", S.doubleValue, name)
            TargetFormat.AnnotationProcessor ->
                CodeBlock.of("(\$T)\$N.getValue()", TypeName.DOUBLE, name)
        }
    }

    object Boolean : AnnotationValueType<kotlin.Boolean>("boolean") {
        override fun typeName(): TypeName = TypeName.BOOLEAN
        override fun literalOf(value: kotlin.Boolean): CodeBlock = CodeBlock.of("\$L", value)
        override fun fromValue(targetFormat: TargetFormat, name: kotlin.String): CodeBlock = when (targetFormat) {
            TargetFormat.KotlinIrCompiler ->
                CodeBlock.of("(\$T)((\$T)\$N).getValue()", TypeName.BOOLEAN, S.irConst, name)
            TargetFormat.KotlinDescriptor ->
                CodeBlock.of("((\$T)\$N).getValue()", S.booleanValue, name)
            TargetFormat.AnnotationProcessor ->
                CodeBlock.of("(\$T)\$N.getValue()", TypeName.BOOLEAN, name)
        }
    }

    object String : AnnotationValueType<kotlin.String>("String") {
        override fun typeName(): TypeName = ClassName.get(kotlin.String::class.java)
        override fun literalOf(value: kotlin.String): CodeBlock = CodeBlock.of("\$S", value)
        override fun fromValue(targetFormat: TargetFormat, name: kotlin.String): CodeBlock = when (targetFormat) {
            TargetFormat.KotlinIrCompiler ->
                CodeBlock.of("(\$T)((\$T)\$N).getValue()", kotlin.String::class.java, S.irConst, name)
            TargetFormat.KotlinDescriptor ->
                CodeBlock.of("((\$T)\$N).getValue()", S.stringValue, name)
            TargetFormat.AnnotationProcessor ->
                CodeBlock.of("(\$T)\$N.getValue()", kotlin.String::class.java, name)
        }
    }

    object Class : AnnotationValueType<TypeMirror>("Class") {
        override fun typeName(targetFormat: TargetFormat, errorHandler: ErrorHandler): TypeName = when (targetFormat) {
            TargetFormat.KotlinIrCompiler ->
                S.irType
            TargetFormat.KotlinDescriptor ->
                S.kClassValueValue
            TargetFormat.AnnotationProcessor ->
                APS.typeMirror
        }

        override fun literalOf(value: TypeMirror): CodeBlock = error("default value of Class<?> is not supported")
        override fun fromValue(targetFormat: TargetFormat, name: kotlin.String): CodeBlock = when (targetFormat) {
            TargetFormat.KotlinIrCompiler ->
                CodeBlock.of("((\$T)\$N).getClassType()", S.irClassReference, name)
            TargetFormat.KotlinDescriptor ->
                CodeBlock.of("((\$T)\$N).getValue()", S.kClassValue, name)
            TargetFormat.AnnotationProcessor ->
                CodeBlock.of("(\$T)\$N.getValue()", APS.typeMirror, name)
        }
    }

    class Enum(val type: DeclaredType) : AnnotationValueType<VariableElement>(type.toString()) {
        override fun typeName() = ClassName.get(type.asElement() as TypeElement)!!
        override fun literalOf(value: VariableElement): CodeBlock =
            CodeBlock.of("\$T.\$N", TypeName.get(type), value.simpleName.toString())

        override fun fromValue(targetFormat: TargetFormat, name: kotlin.String): CodeBlock = when (targetFormat) {
            TargetFormat.KotlinIrCompiler ->
                CodeBlock.of("\$T.valueOf(((\$T)\$N).getSymbol().getOwner().getName().getIdentifier())",
                    ClassName.get(type.asElement() as TypeElement), S.irGetEnumValue, name)
            TargetFormat.KotlinDescriptor ->
                CodeBlock.of("\$T.valueOf(((\$T)\$N).getValue().getSecond().getIdentifier())",
                    TypeName.get(type), S.enumValue, name)
            TargetFormat.AnnotationProcessor ->
                CodeBlock.of("\$T.valueOf(((\$T)\$N.getValue()).getSimpleName().asString())",
                    TypeName.get(type), APS.variableElement, name)
        }

        override fun equals(other: Any?) = type == (other as? Enum)?.type
        override fun hashCode() = type.hashCode()
    }

    class Annotation(val type: DeclaredType, val info: AnnotationClassInfo) :
        AnnotationValueType<AnnotationMirror>(type.toString()) {
        override fun typeName() = info.fqName
        override fun literalOf(value: AnnotationMirror): CodeBlock {
            return CodeBlock.builder().apply {
                add("\$T.builder()\n", info.fqName)
                indent()
                for ((elem, elemValue) in value.elementValues) {
                    @Suppress("UNCHECKED_CAST")
                    val valueType = info.values
                        .first { elem.simpleName.contentEquals(it.first) }
                        .second.type as AnnotationValueType<Any>
                    add(".\$N(\$L)\n", prefixedName("with", elem.simpleName.toString()),
                        valueType.literalOf(elemValue))
                }
                add(".build()")
                unindent()
            }.build()
        }

        override fun fromValue(targetFormat: TargetFormat, name: kotlin.String): CodeBlock = when (targetFormat) {
            TargetFormat.KotlinIrCompiler ->
                CodeBlock.of("\$T.fromIrConstructorCall((\$T)\$N)", info.fqName, S.irClassReference, name)
            TargetFormat.KotlinDescriptor ->
                CodeBlock.of("\$T.fromAnnotationDescriptor(((\$T)\$N).getValue())",
                    info.fqName, S.annotationValue, name)
            TargetFormat.AnnotationProcessor ->
                CodeBlock.of("\$T.fromAnnotationMirror((\$T)\$N.getValue())",
                    info.fqName, APS.annotationMirror, name)
        }

        override fun equals(other: Any?) = type == (other as? Enum)?.type
        override fun hashCode() = type.hashCode()
    }

    class Array<T : Any>(val type: AnnotationValueType<T>) : AnnotationValueType<List<T>>(type.name + "[]") {
        override fun typeName(targetFormat: TargetFormat, errorHandler: ErrorHandler) =
            type.typeName(targetFormat, errorHandler)?.let { ParameterizedTypeName.get(S.list, it) }

        override fun literalOf(value: List<T>): CodeBlock = CodeBlock.builder().apply {
            add("\$T.asList(new \$T[]{\n", S.arrays, type.typeName())
            indent()
            for (t in value) {
                add("\$L", type.literalOf(t))
            }
            unindent()
            add("})")
        }.build()

        override fun fromValue(targetFormat: TargetFormat, name: kotlin.String): CodeBlock = CodeBlock.builder().apply {
            when (targetFormat) {
                TargetFormat.KotlinIrCompiler -> {
                    add("((\$T)\$N)\n", S.irVararg, name)
                    indent()
                    add(".getElements()\n")
                    add(".stream()\n")
                }
                TargetFormat.KotlinDescriptor -> {
                    add("((\$T)\$N)\n", S.arrayValue, name)
                    indent()
                    add(".getValue()\n")
                    add(".stream()\n")
                }
                TargetFormat.AnnotationProcessor -> {
                    add("((\$T<?>)\$N.getValue())\n", S.list, name)
                    indent()
                    add(".stream()\n")
                    add(".map(value1 -> (\$T)value1)\n", APS.annotationValue)
                }
            }
            add(".map(\$N -> \$L)\n", "${name}_", type.fromValue(targetFormat, "${name}_"))
            add(".collect(\$L.toList())", S.collectors)
            unindent()
        }.build()

        override fun equals(other: Any?) = type == (other as? Enum)?.type
        override fun hashCode() = type.hashCode()
    }

    override fun toString(): kotlin.String = name

    companion object {
        fun from(type: TypeMirror, errorHandler: ErrorHandler): AnnotationValueType<*>? {
            return when (val typeKind = type.kind!!) {
                TypeKind.BOOLEAN -> Boolean
                TypeKind.BYTE -> Byte
                TypeKind.SHORT -> Short
                TypeKind.INT -> Int
                TypeKind.LONG -> Long
                TypeKind.CHAR -> Char
                TypeKind.FLOAT -> Float
                TypeKind.DOUBLE -> Double
                TypeKind.ARRAY -> {
                    type as ArrayType
                    val component = from(type.componentType, errorHandler) ?: return null
                    Array(component)
                }
                TypeKind.DECLARED -> {
                    type as DeclaredType
                    val element = type.asElement()
                    element as TypeElement
                    if (element.qualifiedName.contentEquals("java.lang.Class"))
                        return Class
                    if (element.qualifiedName.contentEquals("java.lang.String"))
                        return String
                    if (element.kind != ElementKind.ANNOTATION_TYPE)
                        return errorHandler("invalid annotation value type: class or interface: $typeKind")
                    val generate = GenerateValueClassValue.findFrom(element)
                        ?: return errorHandler("invalid annotation value type: @interface without @GenerateValueClass: $type")
                    val info = AnnotationClassInfo.parse(element, generate, NopMessager)
                        ?: return errorHandler("contains some invalid value: $type")
                    Annotation(type, info)
                }
                else -> errorHandler("invalid annotation value type: $type")
            }
        }
    }
}
