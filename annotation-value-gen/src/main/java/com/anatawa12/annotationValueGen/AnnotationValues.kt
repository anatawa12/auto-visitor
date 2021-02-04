package com.anatawa12.annotationValueGen

import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror

data class GenerateValueClassValue(
    val value: String,
    val forClass: TypeMirror?,
    val targetFormat: TargetFormat,
) {
    companion object {
        fun findFrom(construct: AnnotatedConstruct): GenerateValueClassValue? =
            findFrom(construct.annotationMirrors)

        fun findFrom(list: List<AnnotationMirror>): GenerateValueClassValue? {
            return list.find {
                (it.annotationType.asElement() as TypeElement).qualifiedName
                    .contentEquals(GenerateValueClass::class.java.name)
            }
                ?.let { from(it) }
        }

        fun from(mirror: AnnotationMirror): GenerateValueClassValue {
            var value: String? = null
            var forClass: TypeMirror? = null
            var targetFormat: TargetFormat? = null
            for ((elementKey, elementValue) in mirror.elementValues) {
                when (elementKey.simpleName.toString()) {
                    "value" -> value = elementValue.get(AnnotationValueType.String) { error("invalid: value") }
                    "forClass" -> forClass = elementValue.get(AnnotationValueType.Class) { error("invalid: forClass") }
                    "targetFormat" -> targetFormat = elementValue.value
                        .let { it as VariableElement }
                        .let { enumValueOf<TargetFormat>(it.simpleName.toString()) }
                }
            }
            return GenerateValueClassValue(
                value = value ?: error("not found: value"),
                forClass = forClass,
                targetFormat = targetFormat ?: error("not found: targetFormat"),
            )
        }
    }
}

data class GenerateValueClassListValue(
    val value: List<GenerateValueClassValue>,
) {
    companion object {
        fun findFrom(construct: AnnotatedConstruct): GenerateValueClassListValue? =
            findFrom(construct.annotationMirrors)

        fun findFrom(list: List<AnnotationMirror>): GenerateValueClassListValue? {
            return list.find {
                (it.annotationType.asElement() as TypeElement).qualifiedName
                    .contentEquals(GenerateValueClassList::class.java.name)
            }
                ?.let { from(it) }
        }

        fun from(mirror: AnnotationMirror): GenerateValueClassListValue {
            var value: List<GenerateValueClassValue>? = null
            for ((elementKey, elementValue) in mirror.elementValues) {
                when (elementKey.simpleName.toString()) {
                    "value" -> value = elementValue
                        .value
                        .let { it as List<*> }
                        .map { GenerateValueClassValue.from(it as AnnotationMirror) }
                }
            }
            return GenerateValueClassListValue(
                value = value ?: error("not found: value"),
            )
        }
    }
}
