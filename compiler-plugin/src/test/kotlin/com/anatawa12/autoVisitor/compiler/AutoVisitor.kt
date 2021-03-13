package com.anatawa12.autoVisitor.compiler

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

    private fun fileName(name: String): String {
        return "AutoVisitor.$name.kt"
    }
}
