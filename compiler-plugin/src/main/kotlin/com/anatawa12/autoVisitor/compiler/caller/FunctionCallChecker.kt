package com.anatawa12.autoVisitor.compiler.caller

import com.anatawa12.autoVisitor.compiler.Symbols
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.resolve.calls.checkers.CallChecker
import org.jetbrains.kotlin.resolve.calls.checkers.CallCheckerContext
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

class FunctionCallChecker : CallChecker {
    override fun check(resolvedCall: ResolvedCall<*>, reportOn: PsiElement, context: CallCheckerContext) {
        val desc = resolvedCall.candidateDescriptor
        if (desc.fqNameSafe != Symbols.autoVisitorFunction) return
        if (desc.valueParameters.size != 1) return
        val param = desc.valueParameters[0]
        val typeCtor = param.type.constructor
        val typeDesc = typeCtor.declarationDescriptor
        if (typeCtor.builtIns.getFunction(0) != typeDesc) return
        // TODO: make compiler error here
        //println("CallCheckerImpl: ${param.javaClass}, $param")
        //val call = resolvedCall.call
        //context.deprecationResolver
    }
}
