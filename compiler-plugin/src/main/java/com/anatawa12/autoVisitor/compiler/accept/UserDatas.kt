package com.anatawa12.autoVisitor.compiler.accept

import org.jetbrains.kotlin.descriptors.CallableDescriptor

class AcceptMethodData() {
    companion object : CallableDescriptor.UserDataKey<AcceptMethodData>
}
