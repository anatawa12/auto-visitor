package com.anatawa12.annotationValueGen

import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeName

class TypeWithDefault<T : Any>(val type: AnnotationValueType<T>, val typeName: TypeName, val defaults: T?) {
    fun defaultsLiteral(): CodeBlock? {
        if (defaults == null) return null
        return type.literalOf(defaults)
    }
}
