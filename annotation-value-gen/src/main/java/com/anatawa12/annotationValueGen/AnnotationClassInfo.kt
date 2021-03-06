package com.anatawa12.annotationValueGen

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import javax.annotation.processing.Messager
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

class AnnotationClassInfo(
    val fqName: ClassName,
    val values: List<Pair<String, TypeWithDefault<*>>>,
) {
    companion object {
        fun parse(element: TypeElement, generate: GenerateValueClassValue, messager: Messager): AnnotationClassInfo? {
            val values = mutableListOf<Pair<String, TypeWithDefault<*>>>()
            var wasError = false
            for (enclosedElement in element.enclosedElements) {
                if (enclosedElement.kind != ElementKind.METHOD) continue
                enclosedElement as ExecutableElement

                val typeWithDefault = getTypeWithDefault(enclosedElement, generate.targetFormat, messager)
                if (typeWithDefault == null) {
                    wasError = true
                    continue
                }
                values += enclosedElement.simpleName.toString() to typeWithDefault
            }
            if (wasError) return null
            return AnnotationClassInfo(
                fqName = parseClassName(element, generate.value, makeErrorHandler(messager, element)) ?: return null,
                values = values,
            )
        }

        private fun typeNameFrom(
            type: AnnotationValueType<*>,
            targetFormat: TargetFormat,
            errorHandler: ErrorHandler,
        ): TypeName? {
            return type.typeName(targetFormat, errorHandler)
        }

        private fun getTypeWithDefault(
            enclosedElement: ExecutableElement,
            targetFormat: TargetFormat,
            messager: Messager,
        ): TypeWithDefault<*>? {
            val type = AnnotationValueType.from(enclosedElement.returnType, makeErrorHandler(messager, enclosedElement))
                ?: return null
            return getTypeWithDefault(enclosedElement, type, targetFormat, messager)
        }

        private fun <T : Any> getTypeWithDefault(
            enclosedElement: ExecutableElement,
            type: AnnotationValueType<T>,
            targetFormat: TargetFormat,
            messager: Messager,
        ): TypeWithDefault<T>? {
            val errorHandler = makeErrorHandler(messager, enclosedElement)
            if (enclosedElement.defaultValue != null && type.hasClass())
                return errorHandler("default value for Class<*> is not supported")
            return TypeWithDefault(
                type,
                typeNameFrom(type, targetFormat = targetFormat, errorHandler = errorHandler) ?: return null,
                enclosedElement.defaultValue?.run { get(type, errorHandler) ?: return null },
            )
        }
    }
}
