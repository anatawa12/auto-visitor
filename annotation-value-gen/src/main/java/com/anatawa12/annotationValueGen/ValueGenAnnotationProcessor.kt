package com.anatawa12.annotationValueGen

import com.google.auto.service.AutoService
import com.squareup.javapoet.JavaFile
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
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

    override fun process(unused: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        for (element in roundEnv.getElementsAnnotatedWith(GenerateValueClass::class.java)
                + roundEnv.getElementsAnnotatedWith(GenerateValueClassList::class.java)) {
            if (element.kind != ElementKind.ANNOTATION_TYPE) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Can be applied to class.")
                return true
            }
            val annotation = element.getAnnotation(GenerateValueClass::class.java)
            element as TypeElement
            if (annotation != null)
                processAnnotation(element, annotation)
            val annotations = element.getAnnotation(GenerateValueClassList::class.java)
            for (generateValueClass in annotations?.value.orEmpty()) {
                processAnnotation(element, generateValueClass)
            }
        }

        return true
    }

    private fun processAnnotation(element: TypeElement, annotation: GenerateValueClass) {
        val valueClassName = parseClassName(element,
            annotation.value,
            makeErrorHandler(messager, element, GenerateValueClass::class.java, "value"))
            ?: return
        val annotationClassInfo = AnnotationClassInfo.parse(element, annotation, messager)
            ?: return
        val typeSpec = TypeSpecGenerator.generateClass(valueClassName, annotationClassInfo, forIr = annotation.isForIr)
        JavaFile.builder(valueClassName.packageName(), typeSpec)
            .build()
            .writeTo(processingEnv.filer)
    }

    override fun getSupportedAnnotationTypes(): Set<String> = setOf(
        GenerateValueClass::class.java.name,
        GenerateValueClassList::class.java.name,
    )

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.RELEASE_8
}
