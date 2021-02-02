package com.anatawa12.autoVisitor.compiler

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.isClassWithFqName
import org.jetbrains.kotlin.ir.types.isNullableAny
import org.jetbrains.kotlin.ir.util.DumpIrTreeVisitor
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.FqNameUnsafe

fun IrTypeParameter.isAnyVariable(): Boolean = superTypes.singleOrNull()?.isNullableAny() ?: false

fun IrType.isClassType(fqName: FqNameUnsafe): Boolean {
    if (this !is IrSimpleType) return false
    if (this.hasQuestionMark) return false
    return classifier.isClassWithFqName(fqName)
}

val IrType.fqName: FqName?
    get() {
        if (this !is IrSimpleType) return null
        if (this.hasQuestionMark) return null
        val classifier = classifier as? IrClassSymbol ?: return null
        val owner = classifier.owner
        return owner.fqNameWhenAvailable
    }

fun IrElement.dump(prefix: String) {
    val appendable = StringBuilder()
    accept(DumpIrTreeVisitor(appendable), prefix)
    println(appendable)
}

fun <E> List<E>.invertTwoIfTrue(condition: Boolean) = if (condition) this[1] to this[0] else this[0] to this[1]
