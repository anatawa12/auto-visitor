package com.anatawa12.annotationValueGen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.ANNOTATION_TYPE)
@Repeatable(GenerateValueClassList.class)
public @interface GenerateValueClass {
    /**
     * name of valueClassName.
     * If starts with '.', this is the relative path of generated class from same package as annotation class.
     * If else, this is the FQN.
     */
    String value();

    /**
     * generates ValueClass for specified annotation class.
     */
    Class<?> forClass() default Object.class;

    TargetFormat targetFormat();
}
