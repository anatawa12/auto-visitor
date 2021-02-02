package com.anatawa12.annotationValueGen

import com.squareup.javapoet.*
import org.jetbrains.annotations.NotNull
import javax.lang.model.element.Modifier

object TypeSpecGenerator {
    fun generateClass(valueClassName: ClassName, annotationClassInfo: AnnotationClassInfo): TypeSpec {
        val annotationName = annotationClassInfo.fqName
        return TypeSpec.classBuilder(valueClassName).apply {
            addModifiers(Modifier.PUBLIC, Modifier.FINAL)

            val builderName = valueClassName.nestedClass("Builder")
            addType(generateBuilder(builderName, valueClassName, annotationClassInfo))

            for ((name, typeWithDefault) in annotationClassInfo.values) {
                addField(FieldSpec.builder(typeWithDefault.typeName, fn(name))
                    .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                    .addAnnotation(NotNull::class.java)
                    .build())
                addMethod(MethodSpec.methodBuilder(prefixedName("get", name))
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addAnnotation(NotNull::class.java)
                    .returns(typeWithDefault.typeName)
                    .addStatement("return this.\$N", fn(name))
                    .build())
            }

            addMethod(MethodSpec.constructorBuilder().apply {
                addModifiers(Modifier.PRIVATE)
                addParameter(ParameterSpec.builder(builderName, "builder")
                    .addAnnotation(NotNull::class.java)
                    .build())

                for ((name, typeWithDefault) in annotationClassInfo.values) {
                    if (typeWithDefault.defaults != null) {
                        addStatement("this.\$N = \$T.requireNonNull(builder.\$N, \$S)", fn(name), S.objects, fn(name),
                            "$name must be assigned")
                    } else {
                        addStatement("this.\$N = builder.\$N", fn(name), fn(name))
                    }
                }
            }.build())

            addMethod(MethodSpec.methodBuilder("builder").apply {
                addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                returns(builderName)

                CodeBlockScope.make(this::addCode) {
                    add("return new ${type(builderName)}();")
                }
            }.build())

            addMethod(MethodSpec.methodBuilder("toString").apply {
                addAnnotation(Override::class.java)
                addModifiers(Modifier.PUBLIC)
                addAnnotation(NotNull::class.java)
                returns(String::class.java)
                addCode(CodeBlock.builder().apply {
                    add("return \$S ", valueClassName.simpleName())
                    val it = annotationClassInfo.values.iterator()
                    while (it.hasNext()) {
                        val (name, _) = it.next()
                        add("+ \$S + this.\$N ", "$name=", fn(name))
                        if (it.hasNext())
                            add("+ \", \" ")
                    }
                    add("+ \")\";")
                }.build())
            }.build())

            addMethod(MethodSpec.methodBuilder("hashCode").apply {
                addAnnotation(Override::class.java)
                addModifiers(Modifier.PUBLIC)
                returns(TypeName.INT)
                addStatement("int hash = 0")
                for ((name, type) in annotationClassInfo.values) {
                    if (type.typeName.isPrimitive) {
                        addStatement("hash = hash * 31 + \$T.hashCode(\$N)", type.typeName.box(), fn(name))
                    } else {
                        addStatement("hash = hash * 31 + this.\$N.hashCode()", fn(name))
                    }
                }
                addStatement("return hash")
            }.build())

            addMethod(MethodSpec.methodBuilder("equals").apply {
                addAnnotation(Override::class.java)
                addModifiers(Modifier.PUBLIC)
                returns(TypeName.BOOLEAN)
                addParameter(TypeName.OBJECT, "other")
                addStatement("if (other == null) return false")
                addStatement("if (other == this) return true")
                addStatement("if (other.getClass() != this.getClass()) return false")
                addStatement("\$T that = (\$T) other", valueClassName, valueClassName)
                for ((name, type) in annotationClassInfo.values) {
                    if (type.typeName.isPrimitive) {
                        when (type.typeName) {
                            TypeName.FLOAT -> {
                                addStatement("if (\$T.floatToIntBits(this.\$N) != \$T.floatToIntBits(that.\$N)) return false",
                                    Float::class.javaObjectType, fn(name),
                                    Float::class.javaObjectType, fn(name))
                            }
                            TypeName.DOUBLE -> {
                                addStatement("if (\$T.doubleToLongBits(this.\$N) != \$T.doubleToLongBits(that.\$N)) return false",
                                    Double::class.javaObjectType, fn(name),
                                    Double::class.javaObjectType, fn(name))
                            }
                            else -> {
                                addStatement("if (this.\$N != that.\$N) return false", fn(name), fn(name))
                            }
                        }
                    } else {
                        addStatement("if (!this.\$N.equals(that.\$N)) return false", fn(name), fn(name))
                    }
                }
                addStatement("return true")
            }.build())

            val annotationField = FieldSpec.builder(S.fqName, "i\$annotationField")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .initializer("new \$T(\$S)", S.fqName, annotationClassInfo.fqName.toString())
                .build()
                .also { addField(it) }

            addMethod(MethodSpec.methodBuilder("fromIrConstructorCall").apply {
                addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                returns(valueClassName)
                addParameter(ParameterSpec.builder(S.irConstructorCall, "call")
                    .addAnnotation(NotNull::class.java)
                    .build())

                CodeBlockScope.make(this::addCode) {
                    add("${type(S.objects)}.requireNonNull(call, ${str("call must not be null")});")
                    add("${type(S.irConstructor)} function = call.getSymbol().getOwner();")
                    add("if (!${type(valueClassName)}.isClassType(function.getReturnType(), ${name(annotationField)}.toUnsafe()))")
                    left("throw new ${type(S.illegalArgumentException)}(${str("the call does not calls ${annotationName.simpleName()}")});")
                    add("")
                    add("${type(builderName)} builder = builder();")
                    add("")
                    beg("for (${type(S.irValueParameter)} valueParameter : function.getValueParameters())")
                    kotlin.run {
                        add("${type(S.irExpression)} value = call.getValueArgument(valueParameter.getIndex());")
                        add("if (value == null) continue;")
                        beg("switch (valueParameter.getName().getIdentifier())")
                        kotlin.run {
                            for ((name, typeWithDefault) in annotationClassInfo.values) {
                                add("case ${str(name)}:")
                                indent()
                                add("builder.${name(prefixedName("with", name))}(${
                                    lit(typeWithDefault.type.fromValue("value"))
                                });")
                                add("break;")
                                unindent()
                            }
                        }
                        end()
                    }
                    end()
                    add("")
                    add("return builder.build();")
                }
            }.build())

            addMethod(MethodSpec.methodBuilder("isClassType").apply {
                addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                returns(TypeName.BOOLEAN)

                addParameter(ParameterSpec.builder(S.irType, "type")
                    .addAnnotation(NotNull::class.java)
                    .build())
                addParameter(ParameterSpec.builder(S.fqNameUnsafe, "fqName")
                    .addAnnotation(NotNull::class.java)
                    .build())

                CodeBlockScope.make(this::addCode) {
                    add("if (!(type instanceof ${type(S.irSimpleType)})) return false;")
                    add("if (((${type(S.irSimpleType)})type).getHasQuestionMark()) return false;")
                    add("return ${type(S.irTypePredicatesKt)}.isClassWithFqName(((${type(S.irSimpleType)})type).getClassifier(), fqName);")
                }
            }.build())
        }.build()
    }

    private fun generateBuilder(
        builderName: ClassName,
        valueClassName: ClassName,
        annotationClassInfo: AnnotationClassInfo,
    ): TypeSpec {
        return TypeSpec.classBuilder(builderName).apply {
            addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            for ((name, typeWithDefault) in annotationClassInfo.values) {
                val buildingType =
                    if (typeWithDefault.defaults != null) typeWithDefault.typeName else typeWithDefault.typeName.box()
                addField(FieldSpec.builder(buildingType, fn(name))
                    .addModifiers(Modifier.PRIVATE)
                    .also {
                        if (typeWithDefault.defaults != null) {
                            it.addAnnotation(NotNull::class.java)
                            it.initializer("\$N", defaultName(name))
                        }
                    }
                    .build())
                addMethod(MethodSpec.methodBuilder(prefixedName("with", name))
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .returns(builderName)
                    .addParameter(ParameterSpec.builder(typeWithDefault.typeName, name)
                        .addAnnotation(NotNull::class.java)
                        .build())
                    .also {
                        if (typeWithDefault.typeName.isPrimitive) {
                            it.addStatement("this.\$N = \$N", fn(name), name)
                        } else {
                            it.addStatement("this.\$N = \$T.requireNonNull(\$N, \$S)", fn(name), S.objects, name, name)
                        }
                    }
                    .addStatement("return this")
                    .build())
            }
            // generate defaults
            for ((name, typeWithDefault) in annotationClassInfo.values) {
                if (typeWithDefault.defaults == null) continue
                addField(FieldSpec.builder(typeWithDefault.typeName, defaultName(name))
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .addAnnotation(NotNull::class.java)
                    .initializer(typeWithDefault.defaultsLiteral())
                    .build())
            }
            addMethod(MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(valueClassName)
                .addStatement("return new \$T(this)", valueClassName)
                .build())
        }.build()
    }

    /**
     * fn: field name
     *
     * the name for fields and local variables.
     */
    private fun fn(name: String) = "f$$name"
    private fun defaultName(name: String) = "d$$name"
}
