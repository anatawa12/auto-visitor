package com.anatawa12.annotationValueGen

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.ANNOTATION_CLASS)
annotation class GenerateValueClass(
    /**
     * name of valueClassName.
     * If starts with '.', this is the relative path of generated class from same package as annotation class.
     * If else, this is the FQN.
     */
    val value: String,
)
