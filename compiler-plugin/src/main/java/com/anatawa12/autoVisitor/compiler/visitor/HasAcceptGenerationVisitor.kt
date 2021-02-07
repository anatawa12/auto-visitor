package com.anatawa12.autoVisitor.compiler.visitor

import com.anatawa12.autoVisitor.compiler.GenerateVisitorValue
import com.anatawa12.autoVisitor.compiler.HasAcceptValue
import com.anatawa12.autoVisitor.compiler.HasVisitorValue
import com.anatawa12.autoVisitor.compiler.prefixedName
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.expressions.impl.IrClassReferenceImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.interpreter.toIrConst
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeBuilder
import org.jetbrains.kotlin.ir.types.impl.buildSimpleType
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.utils.addToStdlib.cast

class HasAcceptGenerationVisitor(
    val moduleFragment: IrModuleFragment,
    val pluginContext: IrPluginContext,
) : IrElementVisitorVoid {
    override fun visitElement(element: IrElement) {
        element.acceptChildrenVoid(this)
    }

    override fun visitClass(declaration: IrClass) {
        val hasVisitor = HasVisitorValue.getFrom(declaration.annotations) ?: return
        val visitor = hasVisitor.visitorType.cast<IrSimpleType>()
            .classifier.cast<IrClassSymbol>().owner

        if (GenerateVisitorValue.getFrom(visitor.annotations) != null) {
            val hasAccept = pluginContext.referenceClass(HasAcceptValue.annotationFqName())!!
            val subClasses =
                hasVisitor.subclasses.map { it.cast<IrSimpleType>().classifier.cast<IrClassSymbol>().owner }
            for (subClass in subClasses + declaration) {
                if (HasAcceptValue.getFrom(subClass.annotations) == null) {
                    subClass.annotations += IrConstructorCallImpl.fromSymbolOwner(
                        type = IrSimpleTypeBuilder().apply {
                            classifier = hasAccept
                        }.buildSimpleType(),
                        constructorSymbol = hasAccept.constructors.single(),
                    ).apply {
                        // visitName
                        putValueArgument(0, prefixedName("visit", subClass.name.identifier)
                            .toIrConst(pluginContext.irBuiltIns.stringType))
                        // rootClass
                        putValueArgument(1, IrClassReferenceImpl(
                            startOffset = UNDEFINED_OFFSET,
                            endOffset = UNDEFINED_OFFSET,
                            type = IrSimpleTypeBuilder()
                                .apply { classifier = pluginContext.irBuiltIns.kClassClass }.buildSimpleType(),
                            symbol = declaration.symbol,
                            classType = IrSimpleTypeBuilder()
                                .apply { classifier = declaration.symbol }.buildSimpleType()
                        ))
                    }
                }
            }
        }

        super.visitClass(declaration)
    }
}
