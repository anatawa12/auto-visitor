package com.anatawa12.autoVisitor.compiler

import com.anatawa12.autoVisitor.compiler.common.AutoVisitorAnnotationErrors
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.junit.jupiter.api.Test

class HasVisitor {
    @Test
    fun `missing-visit-of-children-0`() {
        TestFactory.runTest(fileName("missing-visit-of-children-0")) {
            expectError(CompilerMessageSeverity.ERROR, AutoVisitorAnnotationErrors.MISSING_VISIT_METHOD)
        }
    }

    @Test
    fun `missing-visit-of-root-0`() {
        TestFactory.runTest(fileName("missing-visit-of-root-0")) {
            expectError(CompilerMessageSeverity.ERROR, AutoVisitorAnnotationErrors.MISSING_VISIT_METHOD)
        }
    }

    @Test
    fun `abstract-visit-of-root-0`() {
        TestFactory.runTest(fileName("abstract-visit-of-children-0")) {
            expectError(CompilerMessageSeverity.ERROR, AutoVisitorAnnotationErrors.VISIT_MUST_BE_OPEN)
        }
    }

    private fun fileName(name: String): String {
        return "HasVisitor.$name.kt"
    }
}
