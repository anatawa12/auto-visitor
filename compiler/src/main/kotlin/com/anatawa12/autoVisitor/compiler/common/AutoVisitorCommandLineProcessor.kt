package com.anatawa12.autoVisitor.compiler.common

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration

@AutoService(CommandLineProcessor::class)
class AutoVisitorCommandLineProcessor : CommandLineProcessor {
    override val pluginId: String get() = "autovisitor"
    override val pluginOptions: Collection<AbstractCliOption> get() = emptyList()
    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        super.processOption(option, value, configuration)
    }
}
