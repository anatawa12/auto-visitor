package com.anatawa12.annotationValueGen

import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeName
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class CodeBlockScope(val block: CodeBlock.Builder) {
    private val externals = mutableMapOf<String, Any?>()

    private var index = 0

    private fun addBlock(block: CodeBlock): String {
        val name = "temp${index++}"
        externals[name] = block
        return "\$$name:L"
    }

    fun str(string: String): String = addBlock(CodeBlock.of("\$S", string))
    fun name(obj: Any): String = addBlock(CodeBlock.of("\$N", obj))
    fun lit(obj: Any?): String = literal(obj)
    fun literal(obj: Any?): String = addBlock(CodeBlock.of("\$L", obj))
    fun type(obj: TypeName): String = addBlock(CodeBlock.of("\$T", obj))

    fun add(string: String) {
        block.addNamed("\$[$string\n\$]", externals)
    }

    fun left(string: String) {
        block.indent()
        block.addNamed("\$[$string\n\$]", externals)
        block.unindent()
    }

    fun beg(string: String) {
        add("$string {\n")
        block.indent()
    }

    fun indent() {
        block.indent()
    }

    @Suppress("SpellCheckingInspection")
    fun unindent() {
        block.unindent()
    }

    fun end() {
        block.unindent()
        add("}\n")
    }

    fun build(): CodeBlock = block.build()

    companion object {
        @OptIn(ExperimentalContracts::class)
        inline fun make(user: (CodeBlock) -> Any?, block: CodeBlockScope.() -> Unit) {
            contract {
                callsInPlace(user, InvocationKind.EXACTLY_ONCE)
                callsInPlace(block, InvocationKind.EXACTLY_ONCE)
            }
            user(CodeBlockScope(CodeBlock.builder()).apply(block).build())
        }
    }
}
