package com.anatawa12.annotationValueGen

import com.squareup.javapoet.ClassName
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

    val collectors = ClassName.get(Collectors::class.java)!!
    val illegalArgumentException = ClassName.get(IllegalArgumentException::class.java)!!
    val objects = ClassName.get(Objects::class.java)!!
    val arrays = ClassName.get(Arrays::class.java)!!
    val list = ClassName.get(List::class.java)!!
}
