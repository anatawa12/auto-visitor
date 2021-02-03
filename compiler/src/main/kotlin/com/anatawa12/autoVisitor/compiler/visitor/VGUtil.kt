package com.anatawa12.autoVisitor.compiler.visitor

import com.anatawa12.autoVisitor.compiler.*
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.getAllSuperclassesWithoutAny
import org.jetbrains.kotlin.resolve.descriptorUtil.isSubclassOf
import org.jetbrains.kotlin.resolve.descriptorUtil.module

/**
 * Visitor (and accept) Generation Util
 */
object VGUtil {
    fun getVisitorNameOf(classDesc: ClassDescriptor, rootClass: ClassDescriptor?): String {
        val hasVisitor = HasAcceptValueConstant.getFrom(classDesc.annotations)
            ?: return prefixedName("visit", classDesc.name.identifier)
        if (rootClass != null)
            check(hasVisitor.rootClass.resolveClassifierOrNull(classDesc.module)
                ?.typeConstructor == rootClass.typeConstructor) { "HasAccept" }
        return hasVisitor.visitName
    }

    fun getSubclasses(type: ClassDescriptor): Sequence<ClassDescriptor>? {
        val hasVisitor = HasVisitorValueConstant.getFrom(type.annotations) ?: return null
        return if (hasVisitor.subclasses.isEmpty() && type.modality == Modality.SEALED) type.getAllSealedSubclasses()
        else hasVisitor.subclasses.asSequence()
            .mapNotNull { it.resolveClassOrNull(type.module) }
            .filter { it.isSubclassOf(type) }
    }

    fun getVisitClasses(type: ClassDescriptor): Sequence<ClassDescriptor>? {
        return getSubclasses(type)?.plus(type)
    }

    fun getSuperTypeOf(rootType: ClassDescriptor, subClass: ClassDescriptor): ClassDescriptor? {
        if (rootType.typeConstructor == subClass.typeConstructor) return null
        val subclasses = getSubclasses(rootType)!!.mapTo(mutableSetOf()) { it.typeConstructor }
        for (superClass in subClass.getAllSuperclassesWithoutAny()) {
            if (superClass.typeConstructor in subclasses) return superClass
            if (superClass.typeConstructor == rootType.typeConstructor) return superClass
        }
        error("rootType is not superclass of subclass ($rootType is not of $subClass)")
    }

    fun getVisitorTypeVariables(
        type: ClassDescriptor,
        visitorType: ClassDescriptor,
    ): Pair<TypeParameterDescriptor, TypeParameterDescriptor?> {
        val hasVisitor = HasVisitorValueConstant.getFrom(type.annotations)!!
        return if (hasVisitor.hasCustomDataParam) {
            check(visitorType.declaredTypeParameters.size == 2)
            visitorType.declaredTypeParameters.invertTwoIfTrue(hasVisitor.invertTypeParamsOfVisitor)
        } else {
            check(visitorType.declaredTypeParameters.size == 1)
            visitorType.declaredTypeParameters[0] to null
        }
    }
}
