package com.anatawa12.autoVisitor.processor

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
        targetFormat = TargetFormat.AnnotationProcessor),
    GenerateValueClass(forClass = GenerateAccept::class,
        value = ".GenerateAcceptValue",
        targetFormat = TargetFormat.AnnotationProcessor),
    GenerateValueClass(forClass = HasVisitor::class,
        value = ".HasVisitorValue",
        targetFormat = TargetFormat.AnnotationProcessor),
    GenerateValueClass(forClass = HasAccept::class,
        value = ".HasAcceptValue",
        targetFormat = TargetFormat.AnnotationProcessor),
)
@Deprecated("do not use this class", level = DeprecationLevel.HIDDEN)
annotation class GenerateValueClassListCaller
