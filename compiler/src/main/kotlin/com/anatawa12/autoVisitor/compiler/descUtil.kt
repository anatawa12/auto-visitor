package com.anatawa12.autoVisitor.compiler

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassifierDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.resolveClassByFqName
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.resolve.constants.KClassValue
import org.jetbrains.kotlin.types.KotlinType

fun KClassValue.Value.resolveKotlinTypeOrNull(module: ModuleDescriptor): KotlinType? = when (this) {
    is KClassValue.Value.NormalClass -> when (this.arrayDimensions) {
        0 -> module.resolveClassByFqName(this.classId.asSingleFqName(), NoLookupLocation.FROM_BACKEND)
            ?.defaultType
        else -> null
    }
    is KClassValue.Value.LocalClass -> this.type
}

fun KClassValue.Value.resolveClassifierOrNull(module: ModuleDescriptor): ClassifierDescriptor? = when (this) {
    is KClassValue.Value.NormalClass -> when (this.arrayDimensions) {
        0 -> module.resolveClassByFqName(this.classId.asSingleFqName(), NoLookupLocation.FROM_BACKEND)
        else -> null
    }
    is KClassValue.Value.LocalClass -> this.type.constructor.declarationDescriptor
}

fun KClassValue.Value.resolveClassOrNull(module: ModuleDescriptor): ClassDescriptor? =
    resolveClassifierOrNull(module) as? ClassDescriptor

fun ClassDescriptor.getAllSealedSubclasses(): Sequence<ClassDescriptor> = object : Sequence<ClassDescriptor> {
    override fun iterator(): Iterator<ClassDescriptor> = object : Iterator<ClassDescriptor> {
        val queue = ArrayDeque<ClassDescriptor>(sealedSubclasses)

        override fun hasNext(): Boolean = queue.isNotEmpty()

        override fun next(): ClassDescriptor {
            val desc = queue.removeLastOrNull()
                ?: throw NoSuchElementException()
            queue.addAll(desc.sealedSubclasses)
            return desc
        }
    }
}
