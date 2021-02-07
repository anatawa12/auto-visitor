package com.anatawa12.autoVisitor.processor

import com.anatawa12.autoVisitor.GenerateAccept
import com.anatawa12.autoVisitor.GenerateVisitor
import com.anatawa12.autoVisitor.HasAccept
import com.anatawa12.autoVisitor.HasVisitor
import com.google.auto.service.AutoService
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@AutoService(AbstractProcessor::class)
class AutoVisitorAnnotationProcessor : AbstractProcessor() {
    private lateinit var annotationsChecker: AnnotationsChecker

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        annotationsChecker = AnnotationsChecker(processingEnv.messager, processingEnv.typeUtils)
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        for (element in roundEnv.getElementsAnnotatedWith(GenerateVisitor::class.java)) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR,
                "@GenerateVisitor cannot be used with java", element)
        }
        for (element in roundEnv.getElementsAnnotatedWith(GenerateAccept::class.java)) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR,
                "@GenerateAccept cannot be used with java", element)
        }
        for (element in roundEnv.getElementsAnnotatedWith(HasVisitor::class.java)) {
            annotationsChecker.checkHasVisitor(
                HasVisitorValue.getFrom(element)!!,
                element as TypeElement)
        }
        for (element in roundEnv.getElementsAnnotatedWith(HasAccept::class.java)) {
            annotationsChecker.checkHasAccept(
                HasAcceptValue.getFrom(element)!!,
                element as TypeElement)
        }
        return true
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(
            GenerateVisitorValue.annotationFqName(),
            GenerateAcceptValue.annotationFqName(),
            HasVisitorValue.annotationFqName(),
            HasAcceptValue.annotationFqName(),
        )
    }
}
