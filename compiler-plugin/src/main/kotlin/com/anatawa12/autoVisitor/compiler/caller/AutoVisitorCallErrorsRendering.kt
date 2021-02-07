package com.anatawa12.autoVisitor.compiler.caller

import com.anatawa12.autoVisitor.compiler.caller.AutoVisitorCallErrors.*
import org.jetbrains.kotlin.diagnostics.rendering.DefaultErrorMessages
import org.jetbrains.kotlin.diagnostics.rendering.DiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.rendering.Renderers

object AutoVisitorCallErrorsRendering : DefaultErrorMessages.Extension {
    private val MAP = DiagnosticFactoryToRendererMap("AutoVisitorCall")

    init {
        // HasVisitor
        MAP.put(
            PARAMETER_IS_NOT_VALID_HAS_VISITOR_TYPE,
            "parameter is not valid HasVisitor type: ''{0}''",
            Renderers.RENDER_TYPE
        )

        // lambda
        MAP.put(
            SECOND_PARAMETER_IS_NOT_LAMBDA,
            "second parameter is not lambda",
        )
        MAP.put(
            LAMBDA_MUST_HAVE_SINGLE_WHEN_EXPR,
            "lambda must have single when expression",
        )
        MAP.put(
            LAMBDA_PARAM_NAME_SHOULD_BE_SPECIFIED,
            "lambda parameter should be specified, shouldn't 'it'",
        )
        MAP.put(
            WHEN_CANNOT_HAVE_SUBJECT_VARIABLE,
            "when for here cannot declare variable",
        )
        MAP.put(
            WHEN_MUST_BE_USED_AS_SWITCH,
            "when must be used as switch",
        )
        MAP.put(
            WHEN_SUBJECT_MUST_BE_LAMBDA_PARAM,
            "when subject must lambda parameter",
        )
        MAP.put(
            WHEN_CONDITION_MUST_BE_EITHER_OBJECT_REFERENCE_OR_IS_TYPE,
            "when condition must be object or isType",
        )
        MAP.put(
            WHEN_CONDITION_IS_NOT_HAS_ACCEPT,
            "When condition is not HasAccept",
        )

    }

    override fun getMap(): DiagnosticFactoryToRendererMap = MAP
}
