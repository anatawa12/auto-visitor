package com.anatawa12.autoVisitor.compiler

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.resolve.constants.KClassValue
import org.jetbrains.kotlin.resolve.descriptorUtil.builtIns
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.KotlinTypeFactory
import org.jetbrains.kotlin.types.TypeProjectionImpl

fun KClassValue.Value.resolveKotlinTypeOrNull(module: ModuleDescriptor): KotlinType? = when (this) {
    is KClassValue.Value.NormalClass -> when (this.arrayDimensions) {
        0 -> module.resolveClassByFqName(this.classId.asSingleFqName(), NoLookupLocation.FROM_BACKEND)
            ?.defaultType
        else -> {
            module.resolveClassByFqName(this.classId.asSingleFqName(),
                NoLookupLocation.FROM_BACKEND)?.defaultType?.let {
                var type = it
                repeat(arrayDimensions) {
                    type = KotlinTypeFactory.simpleType(
                        module.builtIns.array.defaultType,
                        arguments = listOf(TypeProjectionImpl(type)),
                    )
                }
                type
            }
        }
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

fun TypeParameterDescriptor.isAnyVariable(): Boolean {
    return upperBounds.isEmpty()
            || builtIns.nullableAnyType == upperBounds.singleOrNull()
}
