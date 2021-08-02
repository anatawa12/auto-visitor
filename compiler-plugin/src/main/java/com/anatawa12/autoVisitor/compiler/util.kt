package com.anatawa12.autoVisitor.compiler

fun prefixedName(prefix: String, name: String): String = prefix + name[0].uppercaseChar() + name.substring(1)
