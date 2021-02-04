package com.anatawa12.annotationValueGen

import com.google.auto.service.AutoService
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
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
            element as TypeElement

            val annotation = GenerateValueClassValue.findFrom(element)
            if (annotation != null)
                processAnnotation(element, annotation)
            val annotations = GenerateValueClassListValue.findFrom(element)
            for (generateValueClass in annotations?.value.orEmpty()) {
                processAnnotation(element, generateValueClass)
            }
        }

        return true
    }

    private fun processAnnotation(byType: TypeElement, annotation: GenerateValueClassValue) {
        val element = annotation.forClass?.let { forClass ->
            val errorHandler = makeErrorHandler(messager, byType, GenerateValueClass::class.java, "value")
            val element = forClass
                .let { it as? DeclaredType }
                ?.asElement()
                ?.let { it as? TypeElement }
                ?: return errorHandler("invalid 'forClass'").run { }
            if (element.kind != ElementKind.ANNOTATION_TYPE)
                return errorHandler("invalid 'forClass'").run { }
            return@let element
        } ?: byType
        val valueClassName = parseClassName(byType,
            annotation.value,
            makeErrorHandler(messager, byType, GenerateValueClass::class.java, "value"))
            ?: return
        val annotationClassInfo = AnnotationClassInfo.parse(element, annotation, messager)
            ?: return
        val typeSpec = TypeSpecGenerator.generateClass(valueClassName,
            ClassName.get(element),
            annotationClassInfo,
            forIr = annotation.isForIr)
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
