package com.anatawa12.autoVisitor.backend.ir

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class AutoVisitorIrGenerationExtension : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.irBuiltins
        val transformer = FunctionCallTransformer(moduleFragment, pluginContext)
        for (file in moduleFragment.files) {
            file.transform(transformer, null)
        }
    }
}
