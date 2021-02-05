package com.anatawa12.autoVisitor.compiler

import com.anatawa12.autoVisitor.HasAccept
import com.anatawa12.autoVisitor.compiler.common.AutoVisitorCommandLineProcessor

/*
fun testing() {
    IntrinsicMethods.INTRINSICS_CLASS_NAME
    IntrinsicMethod::class
    org.jetbrains.kotlin.backend.jvm.intrinsics.IntrinsicMethod
    ClassBuilderInterceptorExtension

    ExpressionCodegenExtension

    org.jetbrains.kotlin.backend.jvm.codegen.ExpressionCodegen

    IrGenerationExtension
    IrElementTransformerVoid
}
// */

fun main(args: Array<String>) {
    require(args.isEmpty())
    val pp = "./compiler-plugin/build/tmp/kapt3/classes/main" // TODO: change path
    val cp = mutableListOf<String>()
    cp += java.io.File(AutoVisitorCommandLineProcessor::class.java.protectionDomain.codeSource.location.toURI())
        .toString()
    cp += java.io.File(HasAccept::class.java.protectionDomain.codeSource.location.toURI())
        .toString()
    cp += java.io.File(Unit::class.java.protectionDomain.codeSource.location.toURI()).toString()

    org.jetbrains.kotlin.cli.jvm.K2JVMCompiler.main(
        arrayOf(
            "-Xplugin=${pp}",
            "-cp", cp.joinToString(java.io.File.pathSeparator),
            "-no-stdlib",
            "-Xuse-ir",
            "-d", "test-compile-out",
            "testing.kt"
        ))
}
