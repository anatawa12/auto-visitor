package com.anatawa12.annotationValueGen

import com.google.auto.service.AutoService
import com.squareup.javapoet.JavaFile
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@AutoService(Processor::class)
@Suppress("unused")
class ValueGenAnnotationProcessor : AbstractProcessor() {
    private val messager get() = processingEnv!!.messager!!

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        for (element in roundEnv.getElementsAnnotatedWith(GenerateValueClass::class.java)) {
            if (element.kind != ElementKind.ANNOTATION_TYPE) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Can be applied to class.")
                return true
            }
            element as TypeElement
            val annotation = element.getAnnotation(GenerateValueClass::class.java)
            val valueClassName = parseClassName(element,
                annotation.value,
                makeErrorHandler(messager, element, GenerateValueClass::class.java, "value"))
                ?: continue
            val annotationClassInfo = AnnotationClassInfo.parse(element, annotation, messager)
                ?: continue
            val typeSpec = TypeSpecGenerator.generateClass(valueClassName, annotationClassInfo)
            JavaFile.builder(valueClassName.packageName(), typeSpec)
                .build()
                .writeTo(processingEnv.filer)
        }

        return true
    }

    override fun getSupportedAnnotationTypes(): Set<String> = setOf(GenerateValueClass::class.java.name)

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.RELEASE_8
}
