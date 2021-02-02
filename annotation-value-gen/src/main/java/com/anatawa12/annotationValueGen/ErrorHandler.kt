package com.anatawa12.annotationValueGen

import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.tools.Diagnostic

typealias ErrorHandler = (String) -> Nothing?

fun makeErrorHandler(messager: Messager, element: Element): ErrorHandler = {
    messager.printMessage(Diagnostic.Kind.ERROR, it, element)
    null
}

fun makeErrorHandler(messager: Messager, element: Element, annotation: Class<*>, name: String): ErrorHandler =
    handle@{ msg ->
        val annotationName = annotation.name
        val ann = element.annotationMirrors.find { it.annotationType.toString() == annotationName }
            ?: return@handle messager.printMessage(Diagnostic.Kind.ERROR, msg, element).run { null }
        val value = ann.elementValues.asSequence().firstOrNull { it.key.simpleName.toString() == name }
            ?: return@handle messager.printMessage(Diagnostic.Kind.ERROR, msg, element, ann).run { null }
        messager.printMessage(Diagnostic.Kind.ERROR, msg, element, ann, value.value)
        null
    }
