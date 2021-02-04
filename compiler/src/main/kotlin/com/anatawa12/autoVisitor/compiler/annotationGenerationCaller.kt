package com.anatawa12.autoVisitor.compiler

import com.anatawa12.annotationValueGen.GenerateValueClass
import com.anatawa12.annotationValueGen.GenerateValueClassList
import com.anatawa12.autoVisitor.GenerateAccept
import com.anatawa12.autoVisitor.GenerateVisitor
import com.anatawa12.autoVisitor.HasAccept
import com.anatawa12.autoVisitor.HasVisitor

@GenerateValueClassList(
    GenerateValueClass(forClass = GenerateVisitor::class, value = ".GenerateVisitorValue", isForIr = true),
    GenerateValueClass(forClass = GenerateVisitor::class, value = ".GenerateVisitorValueConstant", isForIr = false),
    GenerateValueClass(forClass = GenerateAccept::class, value = ".GenerateAcceptValue", isForIr = true),
    GenerateValueClass(forClass = GenerateAccept::class, value = ".GenerateAcceptValueConstant", isForIr = false),
    GenerateValueClass(forClass = HasVisitor::class, value = ".HasVisitorValue", isForIr = true),
    GenerateValueClass(forClass = HasVisitor::class, value = ".HasVisitorValueConstant", isForIr = false),
    GenerateValueClass(forClass = HasAccept::class, value = ".HasAcceptValue", isForIr = true),
    GenerateValueClass(forClass = HasAccept::class, value = ".HasAcceptValueConstant", isForIr = false),
)
@Deprecated("do not use this class", level = DeprecationLevel.HIDDEN)
annotation class GenerateValueClassListCaller
