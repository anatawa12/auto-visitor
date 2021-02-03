package com.anatawa12.autoVisitor.compiler.visitor

import com.anatawa12.autoVisitor.compiler.GenerateVisitorValueConstant
import com.anatawa12.autoVisitor.compiler.resolveClassOrNull
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.SimpleFunctionDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.ValueParameterDescriptorImpl
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension
import org.jetbrains.kotlin.types.KotlinType

/**
 * ResolveExtension for Visitor abstract class.
 */
class VisitorResolveExtension : SyntheticResolveExtension {
    override fun getSyntheticFunctionNames(thisDescriptor: ClassDescriptor): List<Name> {
        val generateVisitor = GenerateVisitorValueConstant.getFrom(thisDescriptor.annotations)
            ?: return emptyList()

        val type = generateVisitor.visitorOf.resolveClassOrNull(thisDescriptor.module) ?: return emptyList()
        val visits = VGUtil.getVisitClasses(type) ?: return emptyList()

        return visits
            .mapTo(mutableSetOf()) { VGUtil.getVisitorNameOf(it, type) }
            .map(Name::identifier)
    }

    override fun generateSyntheticMethods(
        thisDescriptor: ClassDescriptor,
        name: Name,
        bindingContext: BindingContext,
        fromSupertypes: List<SimpleFunctionDescriptor>,
        result: MutableCollection<SimpleFunctionDescriptor>,
    ) {
        val generateVisitor = GenerateVisitorValueConstant.getFrom(thisDescriptor.annotations)
            ?: return

        val type = generateVisitor.visitorOf.resolveClassOrNull(thisDescriptor.module) ?: return
        val (typeR, typeD) = VGUtil.getVisitorTypeVariables(type, thisDescriptor)
        val visits = VGUtil.getVisitClasses(type) ?: return

        for (subclass in visits.filter { name.identifier == VGUtil.getVisitorNameOf(it, type) }) {
            val desc = SimpleFunctionDescriptorImpl.create(
                thisDescriptor,
                Annotations.EMPTY,
                name,
                CallableMemberDescriptor.Kind.SYNTHESIZED,
                thisDescriptor.source,
            )

            val superClass = VGUtil.getSuperTypeOf(type, subclass)

            desc.initialize(
                null,
                thisDescriptor.thisAsReceiverParameter,
                emptyList(),
                listOfNotNull(
                    makeValueParameterDescriptor(
                        containingDeclaration = desc,
                        index = 0,
                        name = Name.identifier("value"),
                        type = subclass.defaultType,
                        source = type.source,
                    ),
                    typeD?.let { _ ->
                        makeValueParameterDescriptor(
                            containingDeclaration = desc,
                            index = 1,
                            name = Name.identifier("data"),
                            type = typeD.defaultType,
                            source = type.source,
                        )
                    },
                ),
                typeR.defaultType,
                if (superClass == null) Modality.ABSTRACT else Modality.OPEN,
                DescriptorVisibilities.PUBLIC,
                mapOf(
                    VisitMethodData to VisitMethodData(
                        superClass,
                        superClass?.let { VGUtil.getVisitorNameOf(it, null) }
                    )
                )
            )
            result.add(desc)
        }
    }

    private fun makeValueParameterDescriptor(
        containingDeclaration: CallableDescriptor,
        index: Int,
        name: Name,
        type: KotlinType,
        source: SourceElement,
    ) = ValueParameterDescriptorImpl.createWithDestructuringDeclarations(
        containingDeclaration = containingDeclaration,
        original = null,
        index = index,
        annotations = Annotations.EMPTY,
        name = name,
        outType = type,
        declaresDefaultValue = false,
        isCrossinline = false,
        isNoinline = false,
        varargElementType = null,
        source = source,
        destructuringVariables = null,
    )
}
