package com.anatawa12.autoVisitor.compiler

import com.anatawa12.annotationValueGen.GenerateValueClass
import com.anatawa12.annotationValueGen.GenerateValueClassList
import com.anatawa12.annotationValueGen.TargetFormat
import com.anatawa12.autoVisitor.GenerateAccept
import com.anatawa12.autoVisitor.GenerateVisitor
import com.anatawa12.autoVisitor.HasAccept
import com.anatawa12.autoVisitor.HasVisitor

@GenerateValueClassList(
    GenerateValueClass(forClass = GenerateVisitor::class,
        value = ".GenerateVisitorValue",
        targetFormat = TargetFormat.KotlinIrCompiler),
    GenerateValueClass(forClass = GenerateVisitor::class,
        value = ".GenerateVisitorValueConstant",
        targetFormat = TargetFormat.KotlinDescriptor),
    GenerateValueClass(forClass = GenerateAccept::class,
        value = ".GenerateAcceptValue",
        targetFormat = TargetFormat.KotlinIrCompiler),
    GenerateValueClass(forClass = GenerateAccept::class,
        value = ".GenerateAcceptValueConstant",
        targetFormat = TargetFormat.KotlinDescriptor),
    GenerateValueClass(forClass = HasVisitor::class,
        value = ".HasVisitorValue",
        targetFormat = TargetFormat.KotlinIrCompiler),
    GenerateValueClass(forClass = HasVisitor::class,
        value = ".HasVisitorValueConstant",
        targetFormat = TargetFormat.KotlinDescriptor),
    GenerateValueClass(forClass = HasAccept::class,
        value = ".HasAcceptValue",
        targetFormat = TargetFormat.KotlinIrCompiler),
    GenerateValueClass(forClass = HasAccept::class,
        value = ".HasAcceptValueConstant",
        targetFormat = TargetFormat.KotlinDescriptor),
)
@Deprecated("do not use this class", level = DeprecationLevel.HIDDEN)
annotation class GenerateValueClassListCaller
