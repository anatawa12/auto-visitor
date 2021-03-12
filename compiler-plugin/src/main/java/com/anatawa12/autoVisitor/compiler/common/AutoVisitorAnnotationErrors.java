package com.anatawa12.autoVisitor.compiler.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.com.intellij.psi.PsiElement;
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory0;
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory1;
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory2;
import org.jetbrains.kotlin.diagnostics.Errors;
import org.jetbrains.kotlin.diagnostics.Severity;
import org.jetbrains.kotlin.resolve.constants.KClassValue;
import org.jetbrains.kotlin.types.KotlinType;

public class AutoVisitorAnnotationErrors {
    // do not new this class
    @Deprecated
    private AutoVisitorAnnotationErrors() {
    }

    // @GenerateVisitor
    @NotNull
    public static final DiagnosticFactory0<PsiElement> GENERATE_VISITOR_FOR_NON_ABSTRACT = DiagnosticFactory0.create(Severity.ERROR);
    @NotNull
    public static final DiagnosticFactory0<PsiElement> VISITOR_MUST_HAVE_NO_ARG_CONSTRUCTOR = DiagnosticFactory0.create(Severity.ERROR);
    @NotNull
    public static final DiagnosticFactory0<PsiElement> VISITOR_CANNOT_HAVE_ABSTRACTS = DiagnosticFactory0.create(Severity.ERROR);
    @NotNull
    public static final DiagnosticFactory0<PsiElement> VISITOR_OF_NON_CLASS = DiagnosticFactory0.create(Severity.ERROR);
    @NotNull
    public static final DiagnosticFactory0<PsiElement> TARGET_CLASS_DOESNT_HAVE_VISITOR = DiagnosticFactory0.create(Severity.ERROR);
    @NotNull
    public static final DiagnosticFactory2<PsiElement, KotlinType, KotlinType> VISITOR_OF_TARGET_IS_NOT_THIS_CLASS = DiagnosticFactory2.create(Severity.ERROR);

    // @HasVisitor
    @NotNull
    public static final DiagnosticFactory0<PsiElement> VISITOR_TYPE_IS_NOT_ABSTRACT_CLASS = DiagnosticFactory0.create(Severity.ERROR);
    @NotNull
    public static final DiagnosticFactory1<PsiElement, KClassValue.Value> INVALID_SUBCLASS = DiagnosticFactory1.create(Severity.ERROR);
    @NotNull
    public static final DiagnosticFactory2<PsiElement, KotlinType, KotlinType> NO_HAS_ACCEPT_AT = DiagnosticFactory2.create(Severity.ERROR);
    @NotNull
    public static final DiagnosticFactory0<PsiElement> ACCEPT_FUNCTION_NOT_FOUND = DiagnosticFactory0.create(Severity.ERROR);

    // @GenerateAccept
    @NotNull
    public static final DiagnosticFactory0<PsiElement> GENERATE_ACCEPT_NEEDS_HAS_VISITOR_ANNOTATION = DiagnosticFactory0.create(Severity.ERROR);

    // common: Visitor class
    @NotNull
    public static final DiagnosticFactory1<PsiElement, Integer> VISITOR_HAS_INVALID_COUNT_OF_TYPE_PARAM = DiagnosticFactory1.create(Severity.ERROR);
    @NotNull
    public static final DiagnosticFactory0<PsiElement> VISITOR_RETURN_TYPE_IS_IN_TYPE_VARIABLE = DiagnosticFactory0.create(Severity.ERROR);
    @NotNull
    public static final DiagnosticFactory0<PsiElement> VISITOR_DATA_PARAM_IS_OUT_TYPE_VARIABLE = DiagnosticFactory0.create(Severity.ERROR);
    @NotNull
    public static final DiagnosticFactory0<PsiElement> VISITOR_TYPE_VARIABLE_MUST_NOT_HAVE_BOUNDS = DiagnosticFactory0.create(Severity.ERROR);

    // @HasAnnotation
    @NotNull
    public static final DiagnosticFactory1<PsiElement, KClassValue.Value> INVALID_ROOT_CLASS = DiagnosticFactory1.create(Severity.ERROR);
    @NotNull
    public static final DiagnosticFactory1<PsiElement, KClassValue.Value> NO_HAS_VISITOR_AT_ROOT_CLASS = DiagnosticFactory1.create(Severity.ERROR);
    @NotNull
    public static final DiagnosticFactory1<PsiElement, KClassValue.Value> THIS_IS_NOT_SUBCLASS_OF = DiagnosticFactory1.create(Severity.ERROR);

    static {

        Errors.Initializer.initializeFactoryNamesAndDefaultErrorMessages(AutoVisitorAnnotationErrors.class,
                AutoVisitorAnnotationErrorsRendering.INSTANCE);
    }
}
