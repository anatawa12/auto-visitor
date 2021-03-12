package com.anatawa12.autoVisitor.compiler.common

import com.anatawa12.autoVisitor.compiler.accept.AcceptResolveExtension
import com.anatawa12.autoVisitor.compiler.visitor.VisitorResolveExtension
import com.google.auto.service.AutoService
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.jvm.compiler.report
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.extensions.StorageComponentContainerContributor
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension

@AutoService(ComponentRegistrar::class)
class AutoVisitorComponentRegistrar : ComponentRegistrar {
    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        // TODO: error for js legacy backend
        if (configuration.get(JVMConfigurationKeys.IR) == false)
            configuration.report(CompilerMessageSeverity.ERROR,
                "ir compiler is required for auto-visitor. Please enable with '-Xuse-ir'.")

        IrGenerationExtension.registerExtension(project, AutoVisitorIrGenerationExtension())
        StorageComponentContainerContributor.registerExtension(project,
            AutoVisitorStorageComponentContainerContributor())
        SyntheticResolveExtension.registerExtension(project, AcceptResolveExtension())
        SyntheticResolveExtension.registerExtension(project, VisitorResolveExtension())
    }
}
