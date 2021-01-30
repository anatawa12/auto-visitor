package com.anatawa12.autoVisitor

import com.anatawa12.autoVisitor.backend.ir.Symbols
import com.anatawa12.autoVisitor.backend.ir.isClassType
import org.jetbrains.kotlin.ir.expressions.IrClassReference
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.utils.addToStdlib.cast
import kotlin.reflect.KClass

data class HasVisitorValue(
    val visitorType: IrType,
    val hasCustomDataParam: Boolean,
    val acceptName: String,
    val subclasses: List<IrType>,
    val invertTypeParamsOfVisitor: Boolean = false,
    val invertTypeParamsOfAccept: Boolean = false,
) {
    companion object {
        fun fromIrConstructorCall(call: IrConstructorCall): HasVisitorValue {
            val function = call.symbol.owner
            require(function.returnType.isClassType(Symbols.hasVisitor.toUnsafe())) { "the call does not calls HasVisitor" }

            var visitorType: IrType? = null
            var hasCustomDataParam: Boolean? = null
            var acceptName: String? = null
            var subclasses: List<IrType>? = null
            var invertTypeParamsOfVisitor: Boolean = false
            var invertTypeParamsOfAccept: Boolean = false

            for (valueParameter in function.valueParameters) {
                val value = call.getValueArgument(valueParameter.index) ?: continue
                when (valueParameter.name.identifier) {
                    "visitorType" -> visitorType = value.cast<IrClassReference>().classType
                    "hasCustomDataParam" -> hasCustomDataParam = value.cast<IrConst<*>>().value as Boolean
                    "acceptName" -> acceptName = value.cast<IrConst<*>>().value as String
                    "subclasses" -> subclasses = value.cast<IrVararg>().elements.map { it.cast<IrClassReference>().classType }
                    "invertTypeParamsOfVisito" -> invertTypeParamsOfVisitor = value.cast<IrConst<*>>().value as Boolean
                    "invertTypeParamsOfAccept" -> invertTypeParamsOfAccept = value.cast<IrConst<*>>().value as Boolean
                    else -> {}
                }
            }

            @Suppress("USELESS_ELVIS")
            return HasVisitorValue(
                visitorType = visitorType ?: error("visitorType not found"),
                hasCustomDataParam = hasCustomDataParam ?: error("hasCustomDataParam not found"),
                acceptName = acceptName ?: error("acceptName not found"),
                subclasses = subclasses ?: error("subclasses not found"),
                invertTypeParamsOfVisitor = invertTypeParamsOfVisitor ?: error("invertTypeParamsOfVisitor not found"),
                invertTypeParamsOfAccept = invertTypeParamsOfAccept ?: error("invertTypeParamsOfAccept not found"),
            )
        }
    }
}

data class HasAcceptValue(
    val visitName: String,
    val rootClass: IrType,
) {
    companion object {
        fun fromIrConstructorCall(call: IrConstructorCall): HasAcceptValue {
            val function = call.symbol.owner
            require(function.returnType.isClassType(Symbols.hasAccept.toUnsafe())) { "the call does not calls HasVisitor" }

            var visitName: String? = null
            var rootClass: IrType? = null

            for (valueParameter in function.valueParameters) {
                val value = call.getValueArgument(valueParameter.index) ?: continue
                when (valueParameter.name.identifier) {
                    "visitName" -> visitName = value.cast<IrConst<*>>().value as String
                    "rootClass" -> rootClass = value.cast<IrClassReference>().classType
                    else -> {}
                }
            }

            @Suppress("USELESS_ELVIS")
            return HasAcceptValue(
                visitName = visitName ?: error("visitName not found"),
                rootClass = rootClass ?: error("rootClass not found"),
            )
        }
    }
}
