package com.anatawa12.autoVisitor.processor

import com.anatawa12.autoVisitor.processor.AnnotationsChecker.*
import javax.annotation.processing.Messager
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Types
import javax.tools.Diagnostic.Kind as DiagnosticKind

/**
 * rewrite of com.anatawa12.autoVisitor.compiler.common.AnnotationsChecker for Annotation Processor
 */
class AnnotationsChecker(
    private val messager: Messager,
    private val types: Types,
) {
    fun interface Diagnostic {
        fun run(messager: Messager)
    }

    fun interface AnnotationDiagnosticFactory0 {
        fun on(e: Element): Diagnostic = on(e, null)
        fun on(e: Element, a: AnnotationMirror?): Diagnostic
    }

    fun interface AnnotationDiagnosticFactory1<T1> {
        fun on(t1: T1, e: Element): Diagnostic = on(t1, e, null)
        fun on(t1: T1, e: Element, a: AnnotationMirror?): Diagnostic
    }

    fun interface AnnotationDiagnosticFactory2<T1, T2> {
        fun on(t1: T1, t2: T2, e: Element): Diagnostic = on(t1, t2, e, null)
        fun on(t1: T1, t2: T2, e: Element, a: AnnotationMirror?): Diagnostic
    }

    val VISITOR_CANNOT_HAVE_ABSTRACTS = AnnotationDiagnosticFactory0 { e, a ->
        Diagnostic {
            it.printMessage(DiagnosticKind.ERROR, "Visitor class must not have abstract member", e, a)
        }
    }

    val VISITOR_TYPE_IS_NOT_ABSTRACT_CLASS = AnnotationDiagnosticFactory0 { e, a ->
        Diagnostic {
            it.printMessage(DiagnosticKind.ERROR, "visitor type is not a abstract class", e, a)
        }
    }
    val INVALID_SUBCLASS = AnnotationDiagnosticFactory1<TypeMirror> { type, e, a ->
        Diagnostic {
            it.printMessage(DiagnosticKind.ERROR, "'$type' is invalid for subclass", e, a)
        }
    }
    val NO_HAS_ACCEPT_AT = AnnotationDiagnosticFactory2<TypeMirror, TypeMirror> { forType, type, e, a ->
        Diagnostic {
            it.printMessage(DiagnosticKind.ERROR, "'$type' does not have @HasAccept for '$forType'", e, a)
        }
    }
    val ACCEPT_FUNCTION_NOT_FOUND = AnnotationDiagnosticFactory0 { e, a ->
        Diagnostic {
            it.printMessage(DiagnosticKind.ERROR, "accept function is not found", e, a)
        }
    }
    val VISITOR_HAS_INVALID_COUNT_OF_TYPE_PARAM = AnnotationDiagnosticFactory1<Int> { count, e, a ->
        Diagnostic {
            it.printMessage(DiagnosticKind.ERROR,
                "The visitor type has invalid count of type parameters, expected $count",
                e,
                a)
        }
    }
    val VISITOR_TYPE_VARIABLE_MUST_NOT_HAVE_BOUNDS = AnnotationDiagnosticFactory0 { e, a ->
        Diagnostic {
            it.printMessage(DiagnosticKind.ERROR, "The type variable of visitor cannot have bounds", e, a)
        }
    }
    val INVALID_ROOT_CLASS = AnnotationDiagnosticFactory1<TypeMirror> { rootClass, e, a ->
        Diagnostic {
            it.printMessage(DiagnosticKind.ERROR, "invalid root class: '$rootClass'")
        }

    }
    val NO_HAS_VISITOR_AT_ROOT_CLASS = AnnotationDiagnosticFactory1<TypeMirror> { rootClass, e, a ->
        Diagnostic {
            it.printMessage(DiagnosticKind.ERROR, "NO @HasAccept at rootClass: '$rootClass'")
        }
    }
    val THIS_IS_NOT_SUBCLASS_OF = AnnotationDiagnosticFactory1<TypeMirror> { rootClass, e, a ->
        Diagnostic {
            it.printMessage(DiagnosticKind.ERROR, "This class is not subclass of rootClass, '$rootClass'")
        }
    }

    class VisitDesc(val name: String, val classDesc: TypeElement)

    fun checkHasVisitor(
        hasVisitor: HasVisitorValue,
        declaration: TypeElement,
    ) {
        val annotationMirror = hasVisitor.generatedFrom()!!

        @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
        var wasError = false
        fun report(diagnostic: Diagnostic) {
            diagnostic.run(messager)
            wasError = true
        }

        // null if the visitor type not needed to check

        val visitorType = hasVisitor.visitorType.resolveClassOrNull()
        when {
            visitorType == null -> report(VISITOR_TYPE_IS_NOT_ABSTRACT_CLASS.on(declaration, annotationMirror))
            else -> when {
                visitorType.kind != ElementKind.CLASS -> {
                    report(VISITOR_TYPE_IS_NOT_ABSTRACT_CLASS.on(declaration, annotationMirror))
                }
                visitorType.modifiers.contains(Modifier.ABSTRACT) -> {
                    report(VISITOR_TYPE_IS_NOT_ABSTRACT_CLASS.on(declaration, annotationMirror))
                }
                else -> {
                }
            }
        }

        fun checkHasAccept(classDesc: TypeElement): VisitDesc? {
            val hasAccept = HasAcceptValue.getFrom(classDesc)
            if (hasAccept == null) {
                report(NO_HAS_ACCEPT_AT.on(declaration.asType(),
                    classDesc.asType(),
                    classDesc, null))
                return null
            }
            if (hasAccept.rootClass != declaration.asType()) {
                report(NO_HAS_ACCEPT_AT.on(
                    declaration.asType(),
                    classDesc.asType(),
                    classDesc,
                    hasAccept.generatedFrom()))
                return null
            }
            return VisitDesc(hasAccept.visitName, classDesc)
        }

        val visits = mutableListOf<VisitDesc>()
        checkHasAccept(declaration)?.let(visits::add)
        // subclasses
        for (subclass in hasVisitor.subclasses) {
            val subclassDesc = subclass.resolveClassOrNull()
            if (subclassDesc == null) {
                report(INVALID_SUBCLASS.on(subclass, declaration, annotationMirror))
                continue
            }

            if (!subclassDesc.isSubclassOf(declaration)) {
                report(INVALID_SUBCLASS.on(subclass, declaration, annotationMirror))
                continue
            }
            checkHasAccept(subclassDesc)?.let(visits::add)
        }

        // visitor class
        if (visitorType != null) {
            checkVisitorClass(visitorType, hasVisitor, visits)
        }

        // accept
        val acceptFunction = declaration.enclosedElements.asSequence()
            .filter { it.kind == ElementKind.METHOD }
            .map { it as ExecutableElement }
            .filter { it.simpleName.contentEquals(hasVisitor.acceptName) }
            .firstOrNull(fun(func: ExecutableElement): Boolean {
                if (hasVisitor.hasCustomDataParam) {
                    if (func.typeParameters.size != 2) return false
                    if (func.parameters.size != 2) return false
                    val (returns, data) = func.typeParameters.invertTwoIfTrue(hasVisitor.invertTypeParamsOfAccept)
                    if (!returns.isAnyVariable()) return false
                    if (!data.isAnyVariable()) return false

                    if (types.isSameType(func.returnType, returns.asType())) return false

                    val (visitorParam, dataParam) = func.parameters
                    if (visitorType != null) {
                        val visitorParamType = visitorParam.asType()
                        if (visitorParamType !is DeclaredType)
                            return false
                        if (!types.isSameType(types.erasure(visitorParamType), types.erasure(visitorType.asType())))
                            return false
                        val (returnsV, dataV) = visitorParamType.typeArguments
                            .invertTwoIfTrue(hasVisitor.invertTypeParamsOfVisitor)
                        if (!types.isSameType(returns.asType(), returnsV))
                            return false
                        if (!types.isSameType(data.asType(), dataV))
                            return false
                    }
                    if (types.isSameType(dataParam.asType(), data.asType()))
                        return false
                } else {
                    if (func.typeParameters.size != 1) return false
                    if (func.parameters.size != 1) return false
                    val (returns) = func.typeParameters
                    if (!returns.isAnyVariable()) return false

                    if (types.isSameType(func.returnType, returns.asType())) return false

                    val (visitorParam) = func.parameters
                    if (visitorType != null) {
                        val visitorParamType = visitorParam.asType()
                        if (visitorParamType !is DeclaredType)
                            return false
                        if (!types.isSameType(types.erasure(visitorParamType), types.erasure(visitorType.asType())))
                            return false
                        val (returnsV) = visitorParamType.typeArguments
                        if (!types.isSameType(returns.asType(), returnsV))
                            return false
                    }
                }
                return true
            })
        if (acceptFunction == null)
            report(ACCEPT_FUNCTION_NOT_FOUND.on(declaration, annotationMirror))
    }

    private fun checkVisitorClass(
        visitorType: TypeElement,
        hasVisitor: HasVisitorValue,
        visits: List<VisitDesc>,
    ) {
        fun report(diagnostic: Diagnostic) {
            diagnostic.run(messager)
        }

        val (returns, data) = checkVisitorClassTypeParams(visitorType, hasVisitor, ::report)
            ?: return

        val methodsByName = visits.groupBy { it.name }
        val visitChecker: (ExecutableElement) -> Boolean
        if (data == null) {
            visitChecker = fun(func: ExecutableElement): Boolean {
                if (func.parameters.size != 1) return false
                if (types.isSameType(func.returnType, returns.asType())) return false
                val methods = methodsByName[func.simpleName.toString()] ?: return false
                val (valueParam) = func.parameters
                for (method in methods) {
                    if (types.isSameType(types.erasure(valueParam.asType()), types.erasure(method.classDesc.asType())))
                        return true
                }
                return false
            }
        } else {
            visitChecker = fun(func: ExecutableElement): Boolean {
                if (func.parameters.size != 2) return false
                if (types.isSameType(func.returnType, returns.asType())) return false
                val methods = methodsByName[func.simpleName.toString()] ?: return false
                val (valueParam, dataParam) = func.parameters
                if (types.isSameType(dataParam.asType(), data.asType())) return false
                for (method in methods) {
                    if (types.isSameType(types.erasure(valueParam.asType()), types.erasure(method.classDesc.asType())))
                        return true
                }
                return false
            }
        }

        for (contributedDescriptor in visitorType.enclosedElements) {
            //contributedDescriptor as CallableMemberDescriptor
            // except for visit function
            if (contributedDescriptor is ExecutableElement && visitChecker(contributedDescriptor))
                continue
            if (contributedDescriptor.modifiers.contains(Modifier.ABSTRACT))
                report(VISITOR_CANNOT_HAVE_ABSTRACTS.on(contributedDescriptor))
        }
    }

    fun checkHasAccept(
        hasAccept: HasAcceptValue,
        declaration: TypeElement,
    ) {
        fun report(diagnostic: Diagnostic) {
            diagnostic.run(messager)
        }

        val rootClass = hasAccept.rootClass.resolveClassOrNull()
            ?: return report(INVALID_ROOT_CLASS
                .on(hasAccept.rootClass, declaration, hasAccept.generatedFrom()!!))

        val hasVisitor = HasVisitorValue.getFrom(rootClass)
            ?: return report(NO_HAS_VISITOR_AT_ROOT_CLASS
                .on(hasAccept.rootClass, declaration, hasAccept.generatedFrom()!!))

        if (!hasVisitor.subclasses.asSequence()
                .mapNotNull { it.resolveClassOrNull() }
                .any { types.isSameType(it.asType(), types.erasure(declaration.asType())) }
        )
            return report(THIS_IS_NOT_SUBCLASS_OF
                .on(hasAccept.rootClass, declaration, hasAccept.generatedFrom()!!))
    }

    private fun checkVisitorClassTypeParams(
        visitorType: TypeElement,
        hasVisitor: HasVisitorValue,
        reporter: (Diagnostic) -> Unit,
    ): Pair<TypeParameterElement, TypeParameterElement?>? {
        val typeParams = visitorType.typeParameters
        if (hasVisitor.hasCustomDataParam) {
            if (typeParams.size != 2)
                return reporter(VISITOR_HAS_INVALID_COUNT_OF_TYPE_PARAM.on(2, visitorType)).run { null }
            val (returns, data) = visitorType.typeParameters.invertTwoIfTrue(hasVisitor.invertTypeParamsOfVisitor)
            if (!returns.isAnyVariable())
                reporter(VISITOR_TYPE_VARIABLE_MUST_NOT_HAVE_BOUNDS.on(returns))
            if (!data.isAnyVariable())
                reporter(VISITOR_TYPE_VARIABLE_MUST_NOT_HAVE_BOUNDS.on(data))
            return returns to data
        } else {
            if (typeParams.size != 1)
                return reporter(VISITOR_HAS_INVALID_COUNT_OF_TYPE_PARAM.on(1, visitorType)).run { null }
            val (returns) = visitorType.typeParameters
            if (!returns.isAnyVariable())
                reporter(VISITOR_TYPE_VARIABLE_MUST_NOT_HAVE_BOUNDS.on(returns))
            return returns to null
        }
    }

    private fun TypeElement.isSubclassOf(declaration: TypeElement): Boolean {
        return types.isSubtype(this.asType(), declaration.asType())
    }

    private fun TypeMirror.resolveClassOrNull() = this.let { it as? DeclaredType }
        ?.asElement()
        ?.let { it as? TypeElement }

    private fun TypeParameterElement.isAnyVariable(): Boolean =
        bounds.singleOrNull()?.toString()?.equals("java.lang.Object") ?: false

    private fun <E> List<E>.invertTwoIfTrue(condition: Boolean) =
        if (condition) this[1] to this[0] else this[0] to this[1]
}
