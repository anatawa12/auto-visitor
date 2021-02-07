package com.anatawa12.autoVisitor.compiler

import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classifierOrNull

object CommonUtil {
    fun getVisitorFunctionChecker(
        typeParameters: List<IrTypeParameter>,
        hasVisitor: HasVisitorValue,
    ): (func: IrSimpleFunction, forType: IrType) -> Boolean {
        // check type param and functions
        val visitChecker: (IrSimpleFunction, IrType) -> Boolean
        if (hasVisitor.hasCustomDataParam) {
            check(typeParameters.size == 2)
            val (typeR, typeD) = typeParameters.invertTwoIfTrue(hasVisitor.invertTypeParamsOfVisitor)
            check(typeR.isAnyVariable())
            check(typeD.isAnyVariable())
            visitChecker = fun(func, type): Boolean {
                if (func.typeParameters.isNotEmpty()) return false
                if (func.valueParameters.size != 2) return false
                if (func.returnType.classifierOrNull != typeR.symbol) return false
                val (value, data) = func.valueParameters
                if (value.type != type) return false
                if (data.type.classifierOrNull != typeD.symbol) return false
                return true
            }
        } else {
            check(typeParameters.size == 1)
            val (typeR) = typeParameters
            check(typeR.isAnyVariable())
            visitChecker = fun(func, type): Boolean {
                if (func.typeParameters.isNotEmpty()) return false
                if (func.valueParameters.size != 1) return false
                if (func.returnType.classifierOrNull != typeR.symbol) return false
                val (value) = func.valueParameters
                if (value.type != type) return false
                return true
            }
        }

        return visitChecker
    }
}
