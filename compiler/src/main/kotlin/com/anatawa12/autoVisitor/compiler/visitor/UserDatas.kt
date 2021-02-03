package com.anatawa12.autoVisitor.compiler.visitor

import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor

data class VisitMethodData(
    val superClass: ClassDescriptor?,
    val name: String?,
) {
    companion object : CallableDescriptor.UserDataKey<VisitMethodData>
}
