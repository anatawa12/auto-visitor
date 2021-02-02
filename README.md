Auto Visitor Kotlin Compiler Plugin
====

A kotlin compiler plugin to make easy to write visitor pattern.

This plugin is going to provides two code generator shown below:

1. Generate calling `accept` with visitor anonymous object from `when` expr 
   with metadata by annotation.
1. Generate `accept` method and visitor abstract class from annotations.

## How to use

First, you need to apply this gradle plugin

    TODO: upload to jcenter or plugin portal and put sample code here

To generate visitor class and accept function, 
add `@GenerateVisitor`, `@HasVisitor`, and `@HasAccept` to the parent class and
add `@HasVisitor` to each child class.

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

- [ ] Automatically include this library to classpath in gradle pluign
- [ ] Generating visitor and accept method
    - [ ] Generating visitor abstract class
    - [ ] Generating accept method
    - [ ] Provide Compilation Error *needs help*
- [ ] Generating calling accept from when
    - [x] Generating calling accept from when
      > However, currently not supported to use outer scope variables *needs help*
    - [ ] Provide Compilation Error *needs help*
    
## Structure of this project

- [compiler](./compiler)

  the compiler plugin of this project
- [annotation-value-gen](./annotation-value-gen)

  a pluggable annotation processor for the compiler plugin. 
  See readme in it for more details

## Motivation

Because the generated code of it is linear search, 
`when` expr with `is` checking is much slower than visitor pattern
(see [benchmarks](./benchmarks)), so It's better to use visitor pattern. 
However, The visitor pattern needs much boilerplate code, 
so I want not to write visitor pattern myself, want to generate it.


