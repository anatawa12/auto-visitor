package com.anatawa12.autoVisitor.compiler.caller

import com.anatawa12.autoVisitor.compiler.HasVisitorValueConstant
import com.anatawa12.autoVisitor.compiler.Symbols
import com.anatawa12.autoVisitor.compiler.caller.AutoVisitorCallErrors.*
import com.anatawa12.autoVisitor.compiler.common.AnnotationsChecker
import com.anatawa12.autoVisitor.compiler.resolveClassOrNull
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.diagnostics.Severity
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.checkers.CallChecker
import org.jetbrains.kotlin.resolve.calls.checkers.CallCheckerContext
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeConstructor

class FunctionCallChecker : CallChecker {
    private val annotationsChecker = AnnotationsChecker()

    override fun check(resolvedCall: ResolvedCall<*>, reportOn: PsiElement, context: CallCheckerContext) {
        if (!checkIsAutoVisitorFunction(resolvedCall)) return
        if (!resolvedCall.status.isSuccess) return
        // TODO: make compiler error here
        //println("CallCheckerImpl: ${param.javaClass}, $param")
        val call = resolvedCall.call
        val (visitable, _) = getTypeArguments(resolvedCall.typeArguments)
        // nop for hasAcceptArgument
        //val hasAcceptArgument = call.valueArguments[0]
        val lambdaArgument = call.valueArguments[1] as? LambdaArgument
        val subclasses = checkVisitable(visitable, reportOn, context) ?: return
        checkLambda(lambdaArgument, reportOn, subclasses, context)
        //context.deprecationResolver
    }

    fun checkIsAutoVisitorFunction(resolvedCall: ResolvedCall<*>): Boolean {
        val desc = resolvedCall.candidateDescriptor
        if (desc.fqNameSafe != Symbols.autoVisitorFunction) return false
        if (desc.valueParameters.size != 2) return false
        //val value = desc.valueParameters[0]
        val lambda = desc.valueParameters[1]
        val typeCtor = lambda.type.constructor
        val typeDesc = typeCtor.declarationDescriptor
        if (typeCtor.builtIns.getFunction(1) != typeDesc) return false
        return true
    }

    /**
     * @return visitable, returns
     */
    private fun getTypeArguments(
        typeArguments: Map<TypeParameterDescriptor, KotlinType>,
    ): Pair<KotlinType, KotlinType> {
        var visitable: KotlinType? = null
        var returns: KotlinType? = null
        for ((desc, typeArgument) in typeArguments) {
            when (desc.index) {
                0 -> visitable = typeArgument
                1 -> returns = typeArgument
            }
        }
        return visitable!! to returns!!
    }

    private fun checkVisitable(
        visitable: KotlinType,
        reportOn: PsiElement,
        context: CallCheckerContext,
    ): Set<TypeConstructor>? {
        val decl = visitable.constructor.declarationDescriptor
            ?: return context.trace.report(PARAMETER_IS_NOT_VALID_HAS_VISITOR_TYPE.on(reportOn, visitable)).run { null }

        val hasVisitor = HasVisitorValueConstant.getFrom(decl.annotations)
            ?: return context.trace.report(PARAMETER_IS_NOT_VALID_HAS_VISITOR_TYPE.on(reportOn, visitable)).run { null }


        var wasError = false
        annotationsChecker.checkHasVisitor(
            hasVisitor = hasVisitor,
            declaration = reportOn,
            descriptor = decl,
            moduleDescriptor = context.moduleDescriptor,
            reporter = {
                if (it.severity == Severity.ERROR)
                    wasError = true
            },
            allowGenerateVisitorSkipping = decl.source.containingFile.name != null,
        )

        if (wasError)
            context.trace.report(PARAMETER_IS_NOT_VALID_HAS_VISITOR_TYPE.on(reportOn, visitable))

        val subclasses = hasVisitor.subclasses
            .mapNotNullTo(mutableSetOf()) { it.resolveClassOrNull(context.moduleDescriptor)?.typeConstructor }

        return subclasses
    }

    fun checkLambda(
        lambdaArgument: LambdaArgument?,
        reportOn: PsiElement,
        subclasses: Set<TypeConstructor>,
        context: CallCheckerContext,
    ) {
        lambdaArgument
            ?: return context.trace.report(SECOND_PARAMETER_IS_NOT_LAMBDA.on(reportOn))
        val lambda = lambdaArgument.getLambdaExpression()
            ?: return context.trace.report(SECOND_PARAMETER_IS_NOT_LAMBDA.on(reportOn))

        // LAMBDA_PARAM_NAME_SHOULD_BE_SPECIFIED
        val parameterName = kotlin.run {
            val param = lambda.valueParameters.singleOrNull()
            if (param == null) {
                context.trace.report(LAMBDA_PARAM_NAME_SHOULD_BE_SPECIFIED.on(lambda.asElement()))
                "it"
            } else {
                param.name
            }
        }

        val bodyExpression = lambda.bodyExpression
            ?: return context.trace.report(LAMBDA_MUST_HAVE_SINGLE_WHEN_EXPR.on(lambda.asElement()))

        val whenExpr = bodyExpression.statements.singleOrNull()
            ?.let { it as? KtWhenExpression }
            ?: return context.trace.report(LAMBDA_MUST_HAVE_SINGLE_WHEN_EXPR.on(lambda.asElement()))

        whenExpr.subjectVariable?.let { subject ->
            context.trace.report(WHEN_CANNOT_HAVE_SUBJECT_VARIABLE.on(subject))
        }

        val subjectExpr = whenExpr.subjectExpression
        if (subjectExpr == null) {
            return context.trace.report(WHEN_MUST_BE_USED_AS_SWITCH.on(whenExpr))
        } else kotlin.run subject@{
            val nameExpr = subjectExpr as? KtSimpleNameExpression
                ?: return@subject context.trace.report(WHEN_SUBJECT_MUST_BE_LAMBDA_PARAM.on(whenExpr))
            if (nameExpr.getReferencedName() != parameterName)
                return@subject context.trace.report(WHEN_SUBJECT_MUST_BE_LAMBDA_PARAM.on(whenExpr))
        }

        for (entry in whenExpr.entries) {
            for (condition in entry.conditions) {
                checkCondition(condition, subclasses, context)
            }
        }
    }

    fun checkCondition(
        condition: KtWhenCondition,
        subclasses: Set<TypeConstructor>,
        context: CallCheckerContext,
    ) {
        val binding = context.trace.bindingContext
        when (condition) {
            is KtWhenConditionIsPattern -> {
                val type = binding.get(BindingContext.TYPE, condition.typeReference!!)!!
                if (!type.arguments.all { it.isStarProjection })
                    return context.trace.report(
                        WHEN_CONDITION_IS_NOT_HAS_ACCEPT.on(condition))

                if (type.constructor !in subclasses)
                    return context.trace.report(
                        WHEN_CONDITION_IS_NOT_HAS_ACCEPT.on(condition))
            }
            is KtWhenConditionWithExpression -> {
                // KtSimpleNameExpression
                val refers = binding.getType(condition.expression!!)
                    ?.constructor?.declarationDescriptor
                        as? ClassDescriptor
                    ?: return context.trace.report(
                        WHEN_CONDITION_IS_NOT_HAS_ACCEPT.on(condition))
                if (refers.kind != ClassKind.OBJECT)
                    return context.trace.report(
                        WHEN_CONDITION_IS_NOT_HAS_ACCEPT.on(condition))
                if (refers.typeConstructor !in subclasses)
                    return context.trace.report(
                        WHEN_CONDITION_IS_NOT_HAS_ACCEPT.on(condition))
            }
            else -> return context.trace.report(
                WHEN_CONDITION_MUST_BE_EITHER_OBJECT_REFERENCE_OR_IS_TYPE.on(condition))
        }
    }
}
