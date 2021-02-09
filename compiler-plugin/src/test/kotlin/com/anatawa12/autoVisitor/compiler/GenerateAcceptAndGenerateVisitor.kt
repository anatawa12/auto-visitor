package com.anatawa12.autoVisitor.compiler

import org.junit.jupiter.api.Test

class GenerateAcceptAndGenerateVisitor {
    @Test
    fun test0() {
        TestFactory.runTest(fileName("test0")) {
        }
    }

    private fun fileName(name: String): String {
        return "GenerateAcceptAndGenerateVisitor.$name.kt"
    }
}
