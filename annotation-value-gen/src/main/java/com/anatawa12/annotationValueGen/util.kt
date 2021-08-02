package com.anatawa12.annotationValueGen

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import javax.annotation.processing.Messager
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.AbstractAnnotationValueVisitor8
import javax.tools.Diagnostic

fun <T : Any> AnnotationValue.get(type: AnnotationValueType<T>, errorHandler: ErrorHandler): T? {
    return accept(AnnotationValueVisitorToGet.getFor(), AnnotationValueVisitorToGetArg(type, errorHandler))
}

fun prefixedName(prefix: String, name: String): String = prefix + name[0].uppercaseChar() + name.substring(1)

fun parseClassName(annotationType: TypeElement, value: String, handler: ErrorHandler): ClassName? {
    val elements = value.split('.')
    if (!elements.asSequence().drop(1).all { it.isNotEmpty() }) {
        handler("invalid type name of $annotationType")
        return null
    }
    return if (elements[0] == "") {
        // relative from package
        val className = ClassName.get(annotationType)
        val packageElements = mutableListOf<String>()
        if (className.packageName() != "")
            packageElements += className.packageName()
        packageElements += elements.subList(1, elements.size - 1)
        ClassName.get(packageElements.joinToString("."), elements.last())
    } else {
        // fqName
        ClassName.get(elements.subList(0, elements.size - 1).joinToString("."), elements.last())
    }
}

tailrec fun <T : Any> AnnotationValueType<T>.hasClass(): Boolean {
    if (this == AnnotationValueType.Class) return true
    if (this !is AnnotationValueType.Array<*>) return false
    return this.type.hasClass()
}

private class AnnotationValueVisitorToGetArg(
    val expectType: AnnotationValueType<*>,
    val errorHandler: ErrorHandler,
)

private object AnnotationValueVisitorToGet : AbstractAnnotationValueVisitor8<Any, AnnotationValueVisitorToGetArg>() {
    private fun <T : Any> getChecking(
        value: T,
        arg: AnnotationValueVisitorToGetArg,
        actual: AnnotationValueType<T>,
    ): T? {
        if (actual != arg.expectType) return arg.errorHandler("expected ${arg.expectType} but was $actual")
        return value
    }

    override fun visitBoolean(b: Boolean, p: AnnotationValueVisitorToGetArg) =
        getChecking(b, p, AnnotationValueType.Boolean)

    override fun visitByte(b: Byte, p: AnnotationValueVisitorToGetArg) = getChecking(b, p, AnnotationValueType.Byte)
    override fun visitChar(c: Char, p: AnnotationValueVisitorToGetArg) = getChecking(c, p, AnnotationValueType.Char)
    override fun visitDouble(d: Double, p: AnnotationValueVisitorToGetArg) =
        getChecking(d, p, AnnotationValueType.Double)

    override fun visitFloat(f: Float, p: AnnotationValueVisitorToGetArg) = getChecking(f, p, AnnotationValueType.Float)
    override fun visitInt(i: Int, p: AnnotationValueVisitorToGetArg) = getChecking(i, p, AnnotationValueType.Int)
    override fun visitLong(i: Long, p: AnnotationValueVisitorToGetArg) = getChecking(i, p, AnnotationValueType.Long)
    override fun visitShort(s: Short, p: AnnotationValueVisitorToGetArg) = getChecking(s, p, AnnotationValueType.Short)
    override fun visitString(s: String, p: AnnotationValueVisitorToGetArg) =
        getChecking(s, p, AnnotationValueType.String)

    override fun visitType(t: TypeMirror, p: AnnotationValueVisitorToGetArg) =
        getChecking(t, p, AnnotationValueType.Class)

    override fun visitEnumConstant(c: VariableElement, p: AnnotationValueVisitorToGetArg) =
        getChecking(c, p, AnnotationValueType.Enum(c.asType() as DeclaredType))

    override fun visitAnnotation(a: AnnotationMirror, p: AnnotationValueVisitorToGetArg) = getChecking(a,
        p,
        AnnotationValueType.Annotation(a.annotationType, AnnotationClassInfo(TypeName.OBJECT, listOf())))

    override fun visitArray(vals: List<AnnotationValue>, p: AnnotationValueVisitorToGetArg): List<*>? {
        if (p.expectType !is AnnotationValueType.Array<*>)
            return p.errorHandler("expected $p but was array")
        return vals.map { it.accept(this, AnnotationValueVisitorToGetArg(p.expectType.type, p.errorHandler)) }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getFor(): AnnotationValueVisitor<T?, AnnotationValueVisitorToGetArg> =
        this as AnnotationValueVisitor<T?, AnnotationValueVisitorToGetArg>
}

object NopMessager : Messager {
    override fun printMessage(kind: Diagnostic.Kind?, msg: CharSequence?) = Unit
    override fun printMessage(kind: Diagnostic.Kind?, msg: CharSequence?, e: Element?) = Unit
    override fun printMessage(kind: Diagnostic.Kind?, msg: CharSequence?, e: Element?, a: AnnotationMirror?) = Unit
    override fun printMessage(
        kind: Diagnostic.Kind?,
        msg: CharSequence?,
        e: Element?,
        a: AnnotationMirror?,
        v: AnnotationValue?,
    ) = Unit
}
