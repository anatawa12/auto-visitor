package com.anatawa12.annotationValueGen

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.WildcardTypeName
import java.util.*
import java.util.stream.Collectors

object S {
    val irConstructorCall = ClassName.get("org.jetbrains.kotlin.ir.expressions", "IrConstructorCall")!!
    val irConst = ClassName.get("org.jetbrains.kotlin.ir.expressions", "IrConst")!!
    val irClassReference = ClassName.get("org.jetbrains.kotlin.ir.expressions", "IrClassReference")!!
    val irVararg = ClassName.get("org.jetbrains.kotlin.ir.expressions", "IrVararg")!!
    val irGetEnumValue = ClassName.get("org.jetbrains.kotlin.ir.expressions", "IrGetEnumValue")!!
    val irConstructor = ClassName.get("org.jetbrains.kotlin.ir.declarations", "IrConstructor")!!
    val irType = ClassName.get("org.jetbrains.kotlin.ir.types", "IrType")!!
    val irSimpleType = ClassName.get("org.jetbrains.kotlin.ir.types", "IrSimpleType")!!
    val fqName = ClassName.get("org.jetbrains.kotlin.name", "FqName")!!
    val fqNameUnsafe = ClassName.get("org.jetbrains.kotlin.name", "FqNameUnsafe")!!
    val irValueParameter = ClassName.get("org.jetbrains.kotlin.ir.declarations", "IrValueParameter")!!
    val irExpression = ClassName.get("org.jetbrains.kotlin.ir.expressions", "IrExpression")!!
    val irTypePredicatesKt = ClassName.get("org.jetbrains.kotlin.ir.types", "IrTypePredicatesKt")!!

    val annotationDescriptor = ClassName.get("org.jetbrains.kotlin.descriptors.annotations", "AnnotationDescriptor")!!
    val constantValue = ClassName.get("org.jetbrains.kotlin.resolve.constants", "ConstantValue")!!
    val name = ClassName.get("org.jetbrains.kotlin.name", "Name")!!

    val byteValue = ClassName.get("org.jetbrains.kotlin.resolve.constants", "ByteValue")!!
    val shortValue = ClassName.get("org.jetbrains.kotlin.resolve.constants", "ShortValue")!!
    val intValue = ClassName.get("org.jetbrains.kotlin.resolve.constants", "IntValue")!!
    val longValue = ClassName.get("org.jetbrains.kotlin.resolve.constants", "LongValue")!!
    val charValue = ClassName.get("org.jetbrains.kotlin.resolve.constants", "CharValue")!!
    val floatValue = ClassName.get("org.jetbrains.kotlin.resolve.constants", "FloatValue")!!
    val doubleValue = ClassName.get("org.jetbrains.kotlin.resolve.constants", "DoubleValue")!!
    val booleanValue = ClassName.get("org.jetbrains.kotlin.resolve.constants", "BooleanValue")!!
    val stringValue = ClassName.get("org.jetbrains.kotlin.resolve.constants", "StringValue")!!
    val kClassValue = ClassName.get("org.jetbrains.kotlin.resolve.constants", "KClassValue")!!
    val kClassValueValue = kClassValue.nestedClass("Value")!!
    val enumValue = ClassName.get("org.jetbrains.kotlin.resolve.constants", "EnumValue")!!
    val annotationValue = ClassName.get("org.jetbrains.kotlin.resolve.constants", "AnnotationValue")!!
    val arrayValue = ClassName.get("org.jetbrains.kotlin.resolve.constants", "ArrayValue")!!
    val annotations = ClassName.get("org.jetbrains.kotlin.descriptors.annotations", "Annotations")!!

    val collectors = ClassName.get(Collectors::class.java)!!
    val illegalArgumentException = ClassName.get(IllegalArgumentException::class.java)!!
    val objects = ClassName.get(Objects::class.java)!!
    val arrays = ClassName.get(Arrays::class.java)!!
    val list = ClassName.get(List::class.java)!!
    val entry = ClassName.get(Map.Entry::class.java)!!


    val constantValueStar = ParameterizedTypeName.get(constantValue, WildcardTypeName.subtypeOf(TypeName.OBJECT))!!
    val nameAndConstantValueEntry = ParameterizedTypeName.get(entry, name, constantValueStar)!!
}

/**
 * Annotation Prosessor Symbols
 */
object APS {
    val typeMirror = ClassName.get("javax.lang.model.type", "TypeMirror")!!
    val typeElement = ClassName.get("javax.lang.model.element", "TypeElement")!!
    val annotationValue = ClassName.get("javax.lang.model.element", "AnnotationValue")!!
    val variableElement = ClassName.get("javax.lang.model.element", "VariableElement")!!
    val annotationMirror = ClassName.get("javax.lang.model.element", "AnnotationMirror")!!
    val annotatedConstruct = ClassName.get("javax.lang.model", "AnnotatedConstruct")!!
    val executableElement = ClassName.get("javax.lang.model.element", "ExecutableElement")!!

    val executableAndAnnotationValueEntry = ParameterizedTypeName.get(
        S.entry,
        WildcardTypeName.subtypeOf(executableElement),
        WildcardTypeName.subtypeOf(annotationValue),
    )!!
}
