 Auto Visitor Kotlin Compiler Plugin
====

[![a12 maintenance: Active](https://anatawa12.com/short.php?q=a12-active-svg)](https://anatawa12.com/short.php?q=a12-active-doc)
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/anatawa12/auto-visitor/com.anatawa12.auto-visitor.gradle.plugin/maven-metadata.xml.svg?colorB=007ec6&label=gradle&logo=gradle)](https://plugins.gradle.org/plugin/com.anatawa12.auto-visitor)

A kotlin compiler plugin to make easy to write visitor pattern.

This plugin is going to provides two code generator shown below:

1. Generate calling `accept` with visitor anonymous object from `when` expr with metadata by annotation.
1. Generate `accept` method and visitor abstract class from annotations.

## How to use

First, you need to apply this gradle plugin

```kotlin
plugins {
  // 1.4.32 is required for 1.0.2
  id("org.jetbrains.kotlin.jvm") version "1.4.32"
  id("com.anatawa12.auto-visitor") version "1.0.4"
}
```

To generate visitor class and accept function, add `@GenerateAccept`, `@HasVisitor`, and `@HasAccept` to the parent
class, add `@GenerateVisitor` to the visitor abstract class, and add `@HasVisitor` to each child class.

TODO: add example code and link to it.

To generate calling accept function, surround when expr with `autoVisitor` like shown below:

```kotlin
autoVisitor(some_expr) { variable ->
    when (variable) {
        is SomeClass -> {
            statements
        }
        else -> {
            statements
        }
    }
}
```

## Status of implementation

- [x] Automatically include this library to classpath in gradle plugin
- [x] Generating visitor and accept method
  - [x] Generating visitor abstract class
  - [x] Generating accept method
  - [x] Provide Compilation Error
- [x] Generating calling accept from when
  - [x] Generating calling accept from when
  - [x] Provide Compilation Error
    
## Structure of this project

- [compiler-plugin](./compiler-plugin)

  The compiler plugin of Kotlin.

- [gradle-plugin](./gradle-plugin)

  The gradle plugin. This includes applying Kotlin compiler plugin, applying Annotation Processor.

- [annotation-processor](./annotation-processor)

  The pluggable annotation processor to verify annotation usages from java.

- [annotation-value-gen](./annotation-value-gen)

  A pluggable annotation processor for the compiler plugin. See readme in it for more details

## Motivation

Because the generated code of it is linear search, 
`when` expr with `is` checking is much slower than visitor pattern
(see [benchmarks](./benchmarks)), so It's better to use visitor pattern. 
However, The visitor pattern needs much boilerplate code, 
so I want not to write visitor pattern myself, want to generate it.


