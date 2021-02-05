package com.anatawa12.autoVisitor.compiler

fun prefixedName(prefix: String, name: String): String = prefix + name[0].toUpperCase() + name.substring(1)
