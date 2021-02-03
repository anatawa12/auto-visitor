package com.anatawa12.autoVisitor.compiler

import com.anatawa12.autoVisitor.compiler.common.AutoVisitorIrGenerationExtension
import com.anatawa12.autoVisitor.compiler.visitor.VisitorResolveExtension
import com.google.auto.service.AutoService
import com.intellij.mock.MockProject
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.container.StorageComponentContainer
import org.jetbrains.kotlin.container.useInstance
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.extensions.StorageComponentContainerContributor
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.resolve.calls.checkers.CallChecker
import org.jetbrains.kotlin.resolve.calls.checkers.CallCheckerContext
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension

/*
fun testing() {
    IntrinsicMethods.INTRINSICS_CLASS_NAME
    IntrinsicMethod::class
    org.jetbrains.kotlin.backend.jvm.intrinsics.IntrinsicMethod
    ClassBuilderInterceptorExtension

    ExpressionCodegenExtension

    org.jetbrains.kotlin.backend.jvm.codegen.ExpressionCodegen

    IrGenerationExtension
    IrElementTransformerVoid
}
// */

fun main(args: Array<String>) {
    require(args.isEmpty())
    val pp = "./compiler/build/tmp/kapt3/classes/main" // TODO: change path
    val cp = mutableListOf<String>()
    cp += java.io.File(AutoVisitor::class.java.protectionDomain.codeSource.location.toURI()).toString()
    cp += java.io.File(Unit::class.java.protectionDomain.codeSource.location.toURI()).toString()

    org.jetbrains.kotlin.cli.jvm.K2JVMCompiler.main(
        arrayOf(
            "-Xplugin=${pp}",
            "-cp", cp.joinToString(java.io.File.pathSeparator),
            "-no-stdlib",
            "-Xuse-ir",
            "testing.kt"
        ))
}

@AutoService(CommandLineProcessor::class)
class AutoVisitor : CommandLineProcessor {
    override val pluginId: String get() = "autovisitor"
    override val pluginOptions: Collection<AbstractCliOption> get() = emptyList()
    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        println("AutoVisitor")
        super.processOption(option, value, configuration)
    }
}

@AutoService(ComponentRegistrar::class)
class ComponentRegistrarImpl : ComponentRegistrar {
    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        IrGenerationExtension.registerExtension(project, AutoVisitorIrGenerationExtension())
        StorageComponentContainerContributor.registerExtension(project, StorageComponentContainerContributorImpl())
        SyntheticResolveExtension.registerExtension(project, VisitorResolveExtension())
    }
}

class StorageComponentContainerContributorImpl : StorageComponentContainerContributor {
    override fun registerModuleComponents(
        container: StorageComponentContainer,
        platform: TargetPlatform,
        moduleDescriptor: ModuleDescriptor,
    ) {
        container.useInstance(CallCheckerImpl())
    }
}

class CallCheckerImpl : CallChecker {
    override fun check(resolvedCall: ResolvedCall<*>, reportOn: PsiElement, context: CallCheckerContext) {
        val desc = resolvedCall.candidateDescriptor
        if (desc.fqNameSafe != Symbols.autoVisitorFunction) return
        if (desc.valueParameters.size != 1) return
        val param = desc.valueParameters[0]
        val typeCtor = param.type.constructor
        val typeDesc = typeCtor.declarationDescriptor
        if (typeCtor.builtIns.getFunction(0) != typeDesc) return
        // TODO: make compiler error here
        //println("CallCheckerImpl: ${param.javaClass}, $param")
        val call = resolvedCall.call
        context.deprecationResolver
    }
}
