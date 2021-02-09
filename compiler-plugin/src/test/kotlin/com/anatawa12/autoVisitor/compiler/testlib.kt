package com.anatawa12.autoVisitor.compiler

import com.anatawa12.autoVisitor.HasAccept
import com.anatawa12.autoVisitor.compiler.common.AutoVisitorCommandLineProcessor
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.*
import org.jetbrains.kotlin.config.Services
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory
import org.jetbrains.kotlin.diagnostics.UnboundDiagnostic
import org.jetbrains.kotlin.diagnostics.rendering.DefaultErrorMessages
import org.jetbrains.kotlin.diagnostics.rendering.DiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.rendering.DiagnosticRenderer
import org.jetbrains.kotlin.utils.addToStdlib.getOrPut
import org.junit.jupiter.api.Assertions.assertEquals
import java.nio.file.Files


class RendererMappingReplacer(
    replacement: List<DiagnosticFactoryToRendererMap>,
) {
    private var cache: List<DiagnosticFactoryToRendererMap>?

    init {
        cache = getRenderers()
        setRenderers(replacement)
    }

    fun reset() {
        setRenderers(checkNotNull(cache) { "already reset" })
        cache = null
    }

    companion object {
        private val RENDERER_MAPS_FIELD = DefaultErrorMessages::class.java.getDeclaredField("RENDERER_MAPS")

        @Suppress("UNCHECKED_CAST")
        private fun getRenderersInternal() = RENDERER_MAPS_FIELD[null] as MutableList<DiagnosticFactoryToRendererMap>
        private fun getRenderers(): List<DiagnosticFactoryToRendererMap> {
            return getRenderersInternal().toList()
        }

        private fun setRenderers(value: List<DiagnosticFactoryToRendererMap>) {
            val rendererMap = getRenderersInternal()
            rendererMap.clear()
            rendererMap.addAll(value)
        }

        init {
            RENDERER_MAPS_FIELD.isAccessible = true
        }
    }
}

object DebuggingDiagnosticFactoryToRendererMapFactory {
    private val mapField = DiagnosticFactoryToRendererMap::class.java.getDeclaredField("map")

    init {
        mapField.isAccessible = true
    }

    fun generate(): DiagnosticFactoryToRendererMap {
        val map = DiagnosticFactoryToRendererMap()
        mapField[map] = object : AbstractMap<DiagnosticFactory<*>, DiagnosticRenderer<*>?>() {
            private val backed = mutableMapOf<DiagnosticFactory<*>, DiagnosticRenderer<*>?>()
            override val entries: Set<Map.Entry<DiagnosticFactory<*>, DiagnosticRenderer<*>?>> get() = backed.entries

            override fun containsKey(key: DiagnosticFactory<*>): Boolean = true

            override fun get(key: DiagnosticFactory<*>): DiagnosticRenderer<*>? {
                return backed.getOrPut(key, ::createRenderer, {})
            }
        }
        return map
    }

    @Suppress("RedundantNullableReturnType", "UNUSED_PARAMETER")
    private fun createRenderer(factory: DiagnosticFactory<*>): DiagnosticRenderer<*>? {
        return DiagnosticRendererImpl()
    }

    private class DiagnosticRendererImpl() : DiagnosticRenderer<UnboundDiagnostic> {
        override fun render(diagnostic: UnboundDiagnostic): String = diagnostic.factory.name
    }
}

class MessageRendererImpl : MessageRenderer {
    val severityMessagePairs = mutableListOf<Pair<CompilerMessageSeverity, String>>()

    override fun renderPreamble(): String = ""

    override fun render(
        severity: CompilerMessageSeverity,
        message: String,
        location: CompilerMessageSourceLocation?,
    ): String {
        severityMessagePairs.add(severity to message)
        return ""
    }

    override fun renderUsage(usage: String): String {
        return render(CompilerMessageSeverity.STRONG_WARNING, usage, null)
    }

    override fun renderConclusion(): String = ""

    override fun getName(): String = "FOR_DEBUGGING"
}

class TestFactory {
    private val expectErrors = mutableSetOf<Pair<CompilerMessageSeverity, String>>()

    fun expectError(severity: CompilerMessageSeverity, error: DiagnosticFactory<*>) {
        expectErrors.add(severity to error.name)
    }

    fun runTest(
        testFileName: String,
    ) {
        val replacer = RendererMappingReplacer(listOf(
            DebuggingDiagnosticFactoryToRendererMapFactory.generate()
        ))

        val renderer = MessageRendererImpl()
        val messageCollector: MessageCollector = PrintingMessageCollector(
            System.err,
            renderer,
            false,
        )
        val services: Services = Services.EMPTY
        val tempDir = Files.createTempDirectory("auto-visitor-testing")
        val arguments: K2JVMCompilerArguments = K2JVMCompilerArguments().apply {
            pluginClasspaths = pluginClassPath
            classpath = classPath
            noStdlib = true
            useIR = true
            destination = tempDir.toString()
            freeArgs = listOf("$testsBaseDir$testFileName")
        }
        val compiler = org.jetbrains.kotlin.cli.jvm.K2JVMCompiler()

        compiler.exec(messageCollector, services, arguments)

        replacer.reset()

        tempDir.toFile().deleteRecursively()

        val expected = expectErrors
        val actual = renderer.severityMessagePairs.toSet()

        assertEquals(expected, actual)
    }

    companion object {
        private inline fun <reified T> getClassPathOf(): String {
            return java.io.File(T::class.java.protectionDomain.codeSource.location.toURI()).toString()
        }

        @OptIn(ExperimentalStdlibApi::class)
        private val classPath = buildList {
            add(getClassPathOf<AutoVisitorCommandLineProcessor>())
            add(getClassPathOf<HasAccept>())
            add(getClassPathOf<Unit>())
        }.joinToString(java.io.File.pathSeparator)

        @OptIn(ExperimentalStdlibApi::class)
        val pluginClassPath = buildList {
            add("./build/tmp/kapt3/classes/main")
        }.toTypedArray()

        val testsBaseDir = "./test-data/"

        fun runTest(value: String, block: TestFactory.() -> Unit) {
            val factory = TestFactory()

            factory.block()

            factory.runTest(
                testFileName = value,
            )
        }
    }
}
