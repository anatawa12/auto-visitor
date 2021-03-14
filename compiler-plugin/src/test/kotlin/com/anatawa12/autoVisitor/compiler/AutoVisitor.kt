package com.anatawa12.autoVisitor.compiler

import com.anatawa12.autoVisitor.compiler.caller.AutoVisitorCallErrors
import com.anatawa12.autoVisitor.compiler.common.AutoVisitorAnnotationErrors
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.junit.jupiter.api.Test

class AutoVisitor {
    @Test
    fun test0() {
        TestFactory.runTest(fileName("test0")) {
        }
    }

    @Test
    fun test1() {
        TestFactory.runTest(fileName("test1")) {
        }
    }

    @Test
    fun `invalid-visitor-0`() {
        TestFactory.runTest(fileName("invalid-visitor-0")) {
            expectError(CompilerMessageSeverity.ERROR, AutoVisitorAnnotationErrors.MISSING_VISIT_METHOD)
            expectError(CompilerMessageSeverity.ERROR, AutoVisitorCallErrors.PARAMETER_IS_NOT_VALID_HAS_VISITOR_TYPE)
        }
    }

    private fun fileName(name: String): String {
        return "AutoVisitor.$name.kt"
    }
}
