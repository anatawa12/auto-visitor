package com.anatawa12.autoVisitor.compiler.accept

import com.anatawa12.autoVisitor.compiler.CommonUtil
import com.anatawa12.autoVisitor.compiler.HasAcceptValue
import com.anatawa12.autoVisitor.compiler.HasVisitorValue
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.classifierOrFail
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.utils.addToStdlib.cast

class AcceptGenerationVisitor(
    val moduleFragment: IrModuleFragment,
    val pluginContext: IrPluginContext,
) : IrElementVisitorVoid {
    override fun visitElement(element: IrElement) = element.acceptChildrenVoid(this)

    override fun visitSimpleFunction(declaration: IrSimpleFunction) {
        processSimpleFunction(declaration)
        super.visitSimpleFunction(declaration)
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    fun processSimpleFunction(declaration: IrSimpleFunction) {
        declaration.descriptor.getUserData(AcceptMethodData) ?: return

        val acceptingClass = declaration.parent as IrClass
        val hasAccept = HasAcceptValue.getFrom(acceptingClass.annotations) ?: return
        val hasVisitor = HasVisitorValue.getFrom(hasAccept.rootClass.classOrNull!!.owner.annotations)!!
        val visitorType = hasVisitor.visitorType.classifierOrFail.cast<IrClassSymbol>()
        val visitorClass = visitorType.owner

        // check type param and functions
        val visitChecker = CommonUtil.getVisitorFunctionChecker(visitorClass.typeParameters, hasVisitor)

        val visitMethod = visitorClass.functions.find { visitChecker(it, acceptingClass.defaultType) }!!
        declaration.body = DeclarationIrBuilder(pluginContext, declaration.symbol).irBlockBody {
            +irReturn(irCall(visitMethod.symbol, declaration.returnType).apply {
                dispatchReceiver = irGet(declaration.valueParameters[0])
                putValueArgument(0, irGet(declaration.dispatchReceiverParameter!!))
                if (hasVisitor.hasCustomDataParam) {
                    putValueArgument(1, irGet(declaration.valueParameters[1]))
                }
            })
        }
    }
}
