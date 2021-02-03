package com.anatawa12.autoVisitor.compiler.visitor

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.utils.addToStdlib.safeAs

class VisitorGenerationTransformer(
    val moduleFragment: IrModuleFragment,
    val pluginContext: IrPluginContext,
) : IrElementTransformerVoid() {
    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
        val data = declaration.descriptor.getUserData(VisitMethodData) ?: return super.visitSimpleFunction(declaration)
        println("VisitMethodData: $data")
        if (data.superClass == null) return super.visitSimpleFunction(declaration)
        val superClass = data.superClass//!!
        val name = data.name!!

        val superVisit = resolveFunction(declaration.parent, declaration, name, superClass)

        declaration.body = pluginContext.irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET).also {
            it.statements.add(
                IrCallImpl(
                    startOffset = UNDEFINED_OFFSET,
                    endOffset = UNDEFINED_OFFSET,
                    type = declaration.returnType,
                    symbol = superVisit.symbol,
                    typeArgumentsCount = superVisit.typeParameters.size,
                    valueArgumentsCount = superVisit.valueParameters.size,
                ).apply {
                    dispatchReceiver = IrGetValueImpl(
                        startOffset = UNDEFINED_OFFSET,
                        endOffset = UNDEFINED_OFFSET,
                        symbol = declaration.dispatchReceiverParameter!!.symbol
                    )
                    for ((i, param) in declaration.valueParameters.withIndex()) {
                        putValueArgument(i, IrGetValueImpl(
                            startOffset = UNDEFINED_OFFSET,
                            endOffset = UNDEFINED_OFFSET,
                            symbol = param.symbol
                        ))
                    }
                }
            )
        }
        return super.visitSimpleFunction(declaration)
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    private fun resolveFunction(
        parent: IrDeclarationParent,
        declaration: IrSimpleFunction,
        name: String,
        superClass: ClassDescriptor,
    ): IrSimpleFunction {
        parent as IrClass
        return parent.functions.firstOrNull { func ->
            if (func.name.identifier != name) return@firstOrNull false
            if (func.valueParameters.size != declaration.valueParameters.size) return@firstOrNull false
            if (func.returnType != declaration.returnType) return@firstOrNull false
            if (func.valueParameters[0].type
                    .safeAs<IrSimpleType>()
                    ?.classifier
                    ?.descriptor
                    ?.typeConstructor != superClass.typeConstructor
            ) return@firstOrNull false
            if (func.valueParameters.size == 2) {
                if (func.valueParameters[1].type != declaration.valueParameters[1].type) return@firstOrNull false
            }
            true
        } ?: error("visitor function for $superClass not found (named $name, from $declaration in $parent)")
    }
}
