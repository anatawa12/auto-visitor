package com.anatawa12.autoVisitor.compiler.common

import com.anatawa12.autoVisitor.compiler.common.AutoVisitorAnnotationErrors.*
import org.jetbrains.kotlin.diagnostics.rendering.ContextDependentRenderer
import org.jetbrains.kotlin.diagnostics.rendering.DefaultErrorMessages
import org.jetbrains.kotlin.diagnostics.rendering.DiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.rendering.Renderers
import org.jetbrains.kotlin.resolve.constants.KClassValue

object AutoVisitorAnnotationErrorsRendering : DefaultErrorMessages.Extension {
    private val MAP = DiagnosticFactoryToRendererMap("AutoVisitor")
    private val KClassValueValueRenderer = ContextDependentRenderer<KClassValue.Value> { it, ctx ->
        when (it) {
            is KClassValue.Value.NormalClass -> buildString {
                repeat(it.arrayDimensions) {
                    append("Array<")
                }
                append(it.classId.relativeClassName)
                repeat(it.arrayDimensions) {
                    append(">")
                }
            }
            is KClassValue.Value.LocalClass -> Renderers.RENDER_TYPE.render(it.type, ctx)
        }
    }

    init {
        // @GenerateVisitor
        MAP.put(
            GENERATE_VISITOR_FOR_NON_ABSTRACT,
            "@GenerateVisitor can only be applied for abstract class",
        )
        MAP.put(
            VISITOR_MUST_HAVE_NO_ARG_CONSTRUCTOR,
            "Visitor must have no-arg constructor",
        )
        MAP.put(
            VISITOR_CANNOT_HAVE_ABSTRACTS,
            "Visitor class must not have abstract member",
        )
        MAP.put(
            MISSING_VISIT_METHOD,
            "missing visit method of {0}",
            Renderers.RENDER_TYPE,
        )
        MAP.put(
            VISIT_MUST_BE_OPEN,
            "visit function for child classes must be open {0}",
        )
        MAP.put(
            VISIT_MUST_BE_OPEN_OR_ABSTRACT,
            "visit function for root class must be open or abstract {0}",
        )
        MAP.put(
            VISITOR_OF_NON_CLASS,
            "Visitor class of non class declaration is not supported",
        )
        MAP.put(
            TARGET_CLASS_DOESNT_HAVE_VISITOR,
            "The target type of the visitor isn't annotated with @HasVisitor",
        )
        MAP.put(
            VISITOR_OF_TARGET_IS_NOT_THIS_CLASS,
            "The visitor type of ''{0}'' is not ''{1}''",
            Renderers.RENDER_TYPE,
            Renderers.RENDER_TYPE,
        )
        // @HasVisitor
        MAP.put(
            VISITOR_TYPE_IS_NOT_ABSTRACT_CLASS,
            "This visitor type is not a abstract class",
        )
        MAP.put(
            INVALID_SUBCLASS,
            "''{0}'' is invalid for subclass",
            KClassValueValueRenderer,
        )
        MAP.put(
            NO_HAS_ACCEPT_AT,
            "''{1}'' does not have @HasAccept for ''{0}''",
            Renderers.RENDER_TYPE,
            Renderers.RENDER_TYPE,
        )
        MAP.put(
            ACCEPT_FUNCTION_NOT_FOUND,
            "accept function is not found"
        )
        // @GenerateAccept
        MAP.put(
            GENERATE_ACCEPT_NEEDS_HAS_VISITOR_ANNOTATION,
            "@GenerateAccept needs @HasVisitor",
        )
        // common: Visitor class
        MAP.put(
            VISITOR_HAS_INVALID_COUNT_OF_TYPE_PARAM,
            "The visitor type has invalid count of type parameters, expected {0}",
            Renderers.TO_STRING,
        )
        MAP.put(
            VISITOR_RETURN_TYPE_IS_IN_TYPE_VARIABLE,
            "The type variable for return type is declared as 'in'",
        )
        MAP.put(
            VISITOR_DATA_PARAM_IS_OUT_TYPE_VARIABLE,
            "The type variable for data param is declared as 'out'",
        )
        MAP.put(
            VISITOR_TYPE_VARIABLE_MUST_NOT_HAVE_BOUNDS,
            "The type variable of visitor cannot have bounds"
        )
        // @HasAnnotation
        MAP.put(
            INVALID_ROOT_CLASS,
            "invalid root class: ''{0}''",
            KClassValueValueRenderer,
        )
        MAP.put(
            NO_HAS_VISITOR_AT_ROOT_CLASS,
            "NO @HasAccept at rootClass: ''{0}''",
            KClassValueValueRenderer,
        )
        MAP.put(
            THIS_IS_NOT_SUBCLASS_OF,
            "This class is not subclass of rootClass, ''{0}''",
            KClassValueValueRenderer,
        )
    }

    override fun getMap(): DiagnosticFactoryToRendererMap = MAP
}
