package com.anatawa12.autoVisitor.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrTypeProjection
import org.jetbrains.kotlin.name.FqName

object Symbols {
    val autoVisitorFunction = FqName("com.anatawa12.autoVisitor.autoVisitor")

    fun getAutoVisitorFunction(pluginContext: IrPluginContext): IrSimpleFunctionSymbol {
        val typeFunction1 = pluginContext.irBuiltIns.function(1)

        return pluginContext.referenceFunctions(autoVisitorFunction)
            .singleOrNull { symbol -> isAutoVisitorFun(symbol, typeFunction1) }
            ?: error("autoVisitor function not found in classpath")
    }

    private fun isAutoVisitorFun(
        symbol: IrSimpleFunctionSymbol,
        typeFunction1: IrClassSymbol,
    ): Boolean {
        val desc = symbol.owner
        if (desc.typeParameters.size != 2) return false
        val (typeT, typeR) = desc.typeParameters
        if (desc.valueParameters.size != 2) return false
        val (value, lambda) = desc.valueParameters
        val valueType = value.type as? IrSimpleType ?: return false
        val lambdaType = lambda.type as? IrSimpleType ?: return false

        if (valueType.classifier != typeT.symbol) return false
        if (lambdaType.classifier != typeFunction1) return false
        val (lambdaParam1, lambdaReturn1) = lambdaType.arguments
        val lambdaParam = (lambdaParam1 as? IrTypeProjection)?.type as? IrSimpleType ?: return false
        val lambdaReturn = (lambdaReturn1 as? IrTypeProjection)?.type as? IrSimpleType ?: return false

        if (lambdaParam.classifier != typeT.symbol) return false
        if (lambdaReturn.classifier != typeR.symbol) return false
        return true
    }
}
