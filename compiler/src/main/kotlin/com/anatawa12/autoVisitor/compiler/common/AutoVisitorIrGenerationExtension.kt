package com.anatawa12.autoVisitor.compiler.common

import com.anatawa12.autoVisitor.compiler.caller.FunctionCallTransformer
import com.anatawa12.autoVisitor.compiler.visitor.VisitorGenerationTransformer
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class AutoVisitorIrGenerationExtension() : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.irBuiltins
        val transformers = mutableListOf(
            FunctionCallTransformer(moduleFragment, pluginContext),
            VisitorGenerationTransformer(moduleFragment, pluginContext),
        )
        for (file in moduleFragment.files) {
            for (transformer in transformers) {
                file.transform(transformer, null)
            }
        }
    }
}
