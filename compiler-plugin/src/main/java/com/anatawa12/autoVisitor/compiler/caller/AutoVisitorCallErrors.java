package com.anatawa12.autoVisitor.compiler.caller;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory0;
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory1;
import org.jetbrains.kotlin.diagnostics.Errors;
import org.jetbrains.kotlin.diagnostics.Severity;
import org.jetbrains.kotlin.types.KotlinType;

public class AutoVisitorCallErrors {
    // HasVisitor
    @NotNull
    public static final DiagnosticFactory1<PsiElement, KotlinType> PARAMETER_IS_NOT_VALID_HAS_VISITOR_TYPE = DiagnosticFactory1.create(Severity.ERROR);

    // lambda
    @NotNull
    public static final DiagnosticFactory0<PsiElement> SECOND_PARAMETER_IS_NOT_LAMBDA = DiagnosticFactory0.create(Severity.ERROR);
    @NotNull
    public static final DiagnosticFactory0<PsiElement> LAMBDA_MUST_HAVE_SINGLE_WHEN_EXPR = DiagnosticFactory0.create(Severity.ERROR);
    @NotNull
    public static final DiagnosticFactory0<PsiElement> LAMBDA_PARAM_NAME_SHOULD_BE_SPECIFIED = DiagnosticFactory0.create(Severity.WARNING);
    @NotNull
    public static final DiagnosticFactory0<PsiElement> WHEN_CANNOT_HAVE_SUBJECT_VARIABLE = DiagnosticFactory0.create(Severity.ERROR);
    @NotNull
    public static final DiagnosticFactory0<PsiElement> WHEN_MUST_BE_USED_AS_SWITCH = DiagnosticFactory0.create(Severity.ERROR);
    @NotNull
    public static final DiagnosticFactory0<PsiElement> WHEN_SUBJECT_MUST_BE_LAMBDA_PARAM = DiagnosticFactory0.create(Severity.ERROR);
    @NotNull
    public static final DiagnosticFactory0<PsiElement> WHEN_CONDITION_MUST_BE_EITHER_OBJECT_REFERENCE_OR_IS_TYPE = DiagnosticFactory0.create(Severity.ERROR);
    @NotNull
    public static final DiagnosticFactory0<PsiElement> WHEN_CONDITION_IS_NOT_HAS_ACCEPT = DiagnosticFactory0.create(Severity.ERROR);

    static {

        Errors.Initializer.initializeFactoryNamesAndDefaultErrorMessages(AutoVisitorCallErrors.class,
                AutoVisitorCallErrorsRendering.INSTANCE);
    }
}
