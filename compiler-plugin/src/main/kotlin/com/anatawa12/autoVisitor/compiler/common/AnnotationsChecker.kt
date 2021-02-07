package com.anatawa12.autoVisitor.compiler.common

import com.anatawa12.autoVisitor.compiler.*
import com.anatawa12.autoVisitor.compiler.common.AutoVisitorAnnotationErrors.*
import com.anatawa12.autoVisitor.compiler.visitor.VisitMethodData
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.resolve.checkers.DeclarationChecker
import org.jetbrains.kotlin.resolve.checkers.DeclarationCheckerContext
import org.jetbrains.kotlin.resolve.descriptorUtil.isSubclassOf
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.source.getPsi
import org.jetbrains.kotlin.types.KotlinTypeFactory
import org.jetbrains.kotlin.types.TypeProjectionImpl
import org.jetbrains.kotlin.types.Variance

class AnnotationsChecker : DeclarationChecker {
    fun interface Reporter {
        fun report(diagnostic: Diagnostic)
    }

    override fun check(
        declaration: KtDeclaration,
        descriptor: DeclarationDescriptor,
        context: DeclarationCheckerContext,
    ) {
        GenerateVisitorValueConstant.getFrom(descriptor.annotations)?.let { generateVisitor ->
            checkGenerateVisitor(generateVisitor, declaration, descriptor, context.moduleDescriptor,
                context.trace::report)
        }
        HasVisitorValueConstant.getFrom(descriptor.annotations)?.let { hasVisitor ->
            checkHasVisitor(hasVisitor, declaration, descriptor, context.moduleDescriptor,
                context.trace::report)
        }
        GenerateAcceptValueConstant.getFrom(descriptor.annotations)?.let { generateAccept ->
            checkGenerateAccept(generateAccept, declaration, descriptor,
                context.trace::report)
        }
        HasAcceptValueConstant.getFrom(descriptor.annotations)?.let { hasVisitor ->
            checkHasAccept(hasVisitor, declaration, descriptor, context.moduleDescriptor,
                context.trace::report)
        }
    }

    private fun checkGenerateAccept(
        generateAccept: GenerateAcceptValueConstant,
        declaration: KtDeclaration,
        descriptor: DeclarationDescriptor,
        reporter: Reporter,
    ) {
        val annotationPsi = generateAccept.generatedFrom()!!.source.getPsi()

        if (HasVisitorValueConstant.getFrom(descriptor.annotations) == null)
            reporter.report(GENERATE_ACCEPT_NEEDS_HAS_VISITOR_ANNOTATION.on(annotationPsi ?: declaration))
    }

    private fun checkGenerateVisitor(
        generateVisitor: GenerateVisitorValueConstant,
        declaration: KtDeclaration,
        descriptor: DeclarationDescriptor,
        moduleDescriptor: ModuleDescriptor,
        reporter: Reporter,
    ) {
        var wasError = false
        fun report(diagnostic: Diagnostic) {
            reporter.report(diagnostic)
            wasError = true
        }
        declaration as KtClassOrObject
        descriptor as ClassDescriptor
        val annotationElement = generateVisitor.generatedFrom()!!.source.getPsi()
        if (descriptor.kind != ClassKind.CLASS || descriptor.modality != Modality.ABSTRACT)
            report(GENERATE_VISITOR_FOR_NON_ABSTRACT.on(annotationElement ?: declaration))
        if (!(descriptor.constructors.any {
                it.valueParameters.size == 0
                        && it.typeParameters.size == descriptor.declaredTypeParameters.size
            }))
            report(VISITOR_MUST_HAVE_NO_ARG_CONSTRUCTOR.on(declaration))
        if (wasError) return

        for (contributedDescriptor in descriptor.unsubstitutedMemberScope.getContributedDescriptors(DescriptorKindFilter.CALLABLES)) {
            contributedDescriptor as CallableMemberDescriptor
            // except for generated visit methods
            if (contributedDescriptor.getUserData(VisitMethodData) != null)
                continue
            if (contributedDescriptor.modality == Modality.ABSTRACT)
                report(VISITOR_CANNOT_HAVE_ABSTRACTS.on(contributedDescriptor.source.getPsi()!!))
        }

        val rootClass = generateVisitor.visitorOf.resolveClassOrNull(moduleDescriptor)
            ?: return report(VISITOR_OF_NON_CLASS.on(annotationElement ?: declaration))
        if (rootClass.typeConstructor == moduleDescriptor.builtIns.array.typeConstructor)
            report(VISITOR_OF_NON_CLASS.on(annotationElement ?: declaration))
        if (wasError) return

        val hasVisitor = HasVisitorValueConstant.getFrom(rootClass.annotations)
            ?: return report(TARGET_CLASS_DOESNT_HAVE_VISITOR.on(annotationElement ?: declaration))
        if (hasVisitor.visitorType.resolveKotlinTypeOrNull(moduleDescriptor) != descriptor.defaultType)
            report(VISITOR_OF_TARGET_IS_NOT_THIS_CLASS.on(annotationElement ?: declaration,
                rootClass.defaultType,
                descriptor.defaultType))

        checkVisitorClassTypeParams(descriptor, hasVisitor, declaration, ::report)
    }

    internal fun checkHasVisitor(
        hasVisitor: HasVisitorValueConstant,
        declaration: PsiElement,
        descriptor: DeclarationDescriptor,
        moduleDescriptor: ModuleDescriptor,
        reporter: Reporter,
        allowGenerateVisitorSkipping: Boolean = true,
    ) {
        val annotationPsi = hasVisitor.generatedFrom()!!.source.getPsi()


        @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
        var wasError = false
        fun report(diagnostic: Diagnostic) {
            reporter.report(diagnostic)
            wasError = true
        }

        var checkHasAcceptDecl = true

        descriptor as ClassDescriptor

        // null if the visitor type not needed to check

        val visitorType = hasVisitor.visitorType.resolveClassOrNull(moduleDescriptor)
        when {
            visitorType == null -> {
                report(VISITOR_TYPE_IS_NOT_ABSTRACT_CLASS.on(annotationPsi ?: declaration))
            }
            allowGenerateVisitorSkipping && GenerateVisitorValueConstant.getFrom(visitorType.annotations) != null -> {
                // no check for visitor class because checked by @GenerateVisitor
                // no check for HasAccept because it will be generated.
                checkHasAcceptDecl = false
            }
            else -> {
                if (visitorType.kind != ClassKind.CLASS) {
                    report(VISITOR_TYPE_IS_NOT_ABSTRACT_CLASS.on(annotationPsi ?: declaration))
                } else if (visitorType.modality != Modality.ABSTRACT) {
                    report(VISITOR_TYPE_IS_NOT_ABSTRACT_CLASS.on(annotationPsi ?: declaration))
                } else {
                    checkVisitorClassTypeParams(visitorType, hasVisitor, declaration, ::report)
                }
            }
        }


        kotlin.run {
            // val generateAccept // TODO: GenerateAccept checking
        }

        fun checkHasAccept(classDesc: ClassDescriptor, checkHasAcceptDecl: Boolean) {
            val hasAccept = HasAcceptValueConstant.getFrom(classDesc.annotations)
            if (hasAccept == null && checkHasAcceptDecl) {
                report(NO_HAS_ACCEPT_AT.on(classDesc.source.getPsi() ?: annotationPsi ?: declaration,
                    descriptor.defaultType,
                    classDesc.defaultType))
                return
            }
            if (hasAccept != null) {
                if (hasAccept.rootClass.resolveClassifierOrNull(moduleDescriptor)?.typeConstructor
                    != descriptor.typeConstructor
                ) {
                    report(NO_HAS_ACCEPT_AT.on(classDesc.source.getPsi() ?: annotationPsi ?: declaration,
                        descriptor.defaultType,
                        classDesc.defaultType))
                    return
                }
            }
        }

        checkHasAccept(descriptor, checkHasAcceptDecl)
        // subclasses
        for (subclass in hasVisitor.subclasses) {
            val subclassDesc = subclass.resolveClassOrNull(moduleDescriptor)
            if (subclassDesc == null) {
                report(INVALID_SUBCLASS.on(annotationPsi ?: declaration, subclass))
                continue
            }
            if (!subclassDesc.isSubclassOf(descriptor)) {
                report(INVALID_SUBCLASS.on(annotationPsi ?: declaration, subclass))
                continue
            }
            checkHasAccept(subclassDesc, checkHasAcceptDecl)
        }

        // accept
        val acceptFunction = descriptor.unsubstitutedMemberScope.getContributedFunctions(
            Name.identifier(hasVisitor.acceptName), NoLookupLocation.FROM_BACKEND)
            .firstOrNull(fun(func: SimpleFunctionDescriptor): Boolean {
                if (hasVisitor.hasCustomDataParam) {
                    if (func.typeParameters.size != 2) return false
                    if (func.valueParameters.size != 2) return false
                    val (returns, data) = func.typeParameters.invertTwoIfTrue(hasVisitor.invertTypeParamsOfAccept)
                    if (!returns.isAnyVariable()) return false
                    if (!data.isAnyVariable()) return false

                    if (func.returnType != returns.defaultType) return false

                    val (visitorParam, dataParam) = func.valueParameters
                    if (visitorType != null) {
                        if (visitorParam.type != KotlinTypeFactory
                                .simpleType(visitorType.defaultType,
                                    arguments = (returns.defaultType to data.defaultType)
                                        .invertTwoIfTrue(hasVisitor.invertTypeParamsOfVisitor)
                                        .toList()
                                        .map { TypeProjectionImpl(it) })
                        )
                            return false
                    }
                    if (dataParam.type != data.defaultType)
                        return false
                } else {
                    if (func.typeParameters.size != 1) return false
                    if (func.valueParameters.size != 1) return false
                    val (returns) = func.typeParameters
                    if (!returns.isAnyVariable()) return false

                    if (func.returnType != returns.defaultType) return false

                    val (visitorParam) = func.valueParameters
                    if (visitorType != null) {
                        if (visitorParam.type != KotlinTypeFactory
                                .simpleType(visitorType.defaultType,
                                    arguments = listOf(returns.defaultType)
                                        .map { TypeProjectionImpl(it) })
                        )
                            return false
                    }
                }
                return true
            })
        if (acceptFunction == null)
            report(ACCEPT_FUNCTION_NOT_FOUND.on(annotationPsi ?: declaration))
    }

    private fun checkHasAccept(
        hasAccept: HasAcceptValueConstant,
        declaration: KtDeclaration,
        descriptor: DeclarationDescriptor,
        moduleDescriptor: ModuleDescriptor,
        reporter: Reporter,
    ) {
        descriptor as ClassDescriptor
        val rootClass = hasAccept.rootClass.resolveClassifierOrNull(moduleDescriptor)
            ?: return reporter.report(INVALID_ROOT_CLASS
                .on(hasAccept.generatedFrom()!!.source.getPsi() ?: declaration, hasAccept.rootClass))

        val hasVisitor = HasVisitorValueConstant.getFrom(rootClass.annotations)
            ?: return reporter.report(NO_HAS_VISITOR_AT_ROOT_CLASS
                .on(hasAccept.generatedFrom()!!.source.getPsi() ?: declaration, hasAccept.rootClass))

        if (!hasVisitor.subclasses.asSequence()
                .mapNotNull { it.resolveClassOrNull(moduleDescriptor) }
                .any { it.typeConstructor == descriptor.typeConstructor }
        )
            return reporter.report(THIS_IS_NOT_SUBCLASS_OF
                .on(hasAccept.generatedFrom()!!.source.getPsi() ?: declaration, hasAccept.rootClass))
    }

    private fun checkVisitorClassTypeParams(
        visitorType: ClassDescriptor,
        hasVisitor: HasVisitorValueConstant,
        fallbackElement: PsiElement,
        reporter: (Diagnostic) -> Unit,
    ): Pair<TypeParameterDescriptor, TypeParameterDescriptor?>? {
        val typeParams = visitorType.declaredTypeParameters
        if (hasVisitor.hasCustomDataParam) {
            if (typeParams.size != 2)
                return reporter(VISITOR_HAS_INVALID_COUNT_OF_TYPE_PARAM.on(
                    visitorType.source.getPsi() ?: fallbackElement, 2)).run { null }
            val (returns, data) = visitorType.declaredTypeParameters.invertTwoIfTrue(hasVisitor.invertTypeParamsOfVisitor)
            if (!returns.variance.allowsPosition(Variance.OUT_VARIANCE))
                reporter(VISITOR_RETURN_TYPE_IS_IN_TYPE_VARIABLE.on(
                    returns.source.getPsi() ?: visitorType.source.getPsi() ?: fallbackElement))
            if (!data.variance.allowsPosition(Variance.IN_VARIANCE))
                reporter(VISITOR_DATA_PARAM_IS_OUT_TYPE_VARIABLE.on(
                    data.source.getPsi() ?: visitorType.source.getPsi() ?: fallbackElement))
            if (!returns.isAnyVariable())
                reporter(VISITOR_TYPE_VARIABLE_MUST_NOT_HAVE_BOUNDS.on(
                    returns.source.getPsi() ?: visitorType.source.getPsi() ?: fallbackElement))
            if (!data.isAnyVariable())
                reporter(VISITOR_TYPE_VARIABLE_MUST_NOT_HAVE_BOUNDS.on(
                    data.source.getPsi() ?: visitorType.source.getPsi() ?: fallbackElement))
            return returns to data
        } else {
            if (typeParams.size != 1)
                return reporter(VISITOR_HAS_INVALID_COUNT_OF_TYPE_PARAM.on(
                    visitorType.source.getPsi() ?: fallbackElement, 1)).run { null }
            val (returns) = visitorType.declaredTypeParameters
            if (!returns.variance.allowsPosition(Variance.OUT_VARIANCE))
                reporter(VISITOR_RETURN_TYPE_IS_IN_TYPE_VARIABLE.on(
                    returns.source.getPsi() ?: visitorType.source.getPsi() ?: fallbackElement))
            if (!returns.isAnyVariable())
                reporter(VISITOR_TYPE_VARIABLE_MUST_NOT_HAVE_BOUNDS.on(
                    returns.source.getPsi() ?: visitorType.source.getPsi() ?: fallbackElement))
            return returns to null
        }
    }
}
