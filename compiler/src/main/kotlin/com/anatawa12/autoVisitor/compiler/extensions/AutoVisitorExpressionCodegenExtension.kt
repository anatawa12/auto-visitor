package com.anatawa12.autoVisitor.compiler.extensions

import org.jetbrains.kotlin.codegen.StackValue
import org.jetbrains.kotlin.codegen.extensions.ExpressionCodegenExtension
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.org.objectweb.asm.Type

@Deprecated("maybe not used")
class AutoVisitorExpressionCodegenExtension : ExpressionCodegenExtension {
    // TODO: IMPLEMENTATION
    override fun applyFunction(
        receiver: StackValue,
        resolvedCall: ResolvedCall<*>,
        c: ExpressionCodegenExtension.Context,
    ): StackValue? {
        val targetDesc = resolvedCall.resultingDescriptor
        resolvedCall.resultingDescriptor.fqNameSafe
        StackValue.functionCall(Type.VOID_TYPE, null) {

        }
        return super.applyFunction(receiver, resolvedCall, c)
    }
}
