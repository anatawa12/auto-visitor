package com.anatawa12.autoVisitor.compiler.accept

import com.anatawa12.autoVisitor.compiler.HasVisitorValueConstant
import com.anatawa12.autoVisitor.compiler.invertTwoIfTrue
import com.anatawa12.autoVisitor.compiler.resolveClassifierOrNull
import com.anatawa12.autoVisitor.compiler.resolveKotlinTypeOrNull
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.SimpleFunctionDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.TypeParameterDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.ValueParameterDescriptorImpl
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.getAllSuperclassesWithoutAny
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension
import org.jetbrains.kotlin.storage.LockBasedStorageManager
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.utils.addToStdlib.safeAs

class AcceptResolveExtension : SyntheticResolveExtension {
    private fun findHasVisitorAndRootType(thisDescriptor: ClassDescriptor):
            Pair<HasVisitorValueConstant, ClassDescriptor>? {
        for (mayRootClass in sequenceOf(thisDescriptor) + thisDescriptor.getAllSuperclassesWithoutAny()) {
            val hasVisitorByMayRoot = HasVisitorValueConstant.getFrom(mayRootClass.annotations) ?: continue
            if (mayRootClass.typeConstructor == thisDescriptor.typeConstructor
                || hasVisitorByMayRoot.subclasses.any {
                    it.resolveClassifierOrNull(thisDescriptor.module)?.typeConstructor ==
                            thisDescriptor.typeConstructor
                }
            ) {
                return hasVisitorByMayRoot to mayRootClass
            }
        }
        return null
    }

    override fun getSyntheticFunctionNames(thisDescriptor: ClassDescriptor): List<Name> {
        val (hasVisitor, _) = findHasVisitorAndRootType(thisDescriptor) ?: return emptyList()

        hasVisitor.visitorType.resolveKotlinTypeOrNull(thisDescriptor.module).safeAs<SimpleType>()
            ?: return emptyList()

        return listOf(Name.identifier(hasVisitor.acceptName))
    }

    override fun generateSyntheticMethods(
        thisDescriptor: ClassDescriptor,
        name: Name,
        bindingContext: BindingContext,
        fromSupertypes: List<SimpleFunctionDescriptor>,
        result: MutableCollection<SimpleFunctionDescriptor>,
    ) {
        val (hasVisitor, _) = findHasVisitorAndRootType(thisDescriptor) ?: return
        if (name.identifier != hasVisitor.acceptName) return

        val visitorType = hasVisitor.visitorType.resolveKotlinTypeOrNull(thisDescriptor.module).safeAs<SimpleType>()
            ?: return

        val desc = SimpleFunctionDescriptorImpl.create(
            thisDescriptor,
            Annotations.EMPTY,
            name,
            CallableMemberDescriptor.Kind.SYNTHESIZED,
            thisDescriptor.source,
        )

        fun createTypeParameterDescriptor(name: String, index: Int): TypeParameterDescriptor =
            TypeParameterDescriptorImpl.createWithDefaultBound(
                /* containingDeclaration */
                desc,
                /* annotations */
                Annotations.EMPTY,
                /* reified */
                false,
                /* variance */
                Variance.INVARIANT,
                /* name */
                Name.identifier(name),
                /* index */
                index,
                /* storageManager */
                LockBasedStorageManager.NO_LOCKS,
            )

        val (returnType, dataType) = if (hasVisitor.hasCustomDataParam) {
            // typeParameters
            val (returnIndex, dataIndex) = listOf(0, 1).invertTwoIfTrue(hasVisitor.invertTypeParamsOfAccept)
            val returnType = createTypeParameterDescriptor(
                name = "R",
                index = returnIndex,
            )
            val dataType = createTypeParameterDescriptor(
                name = "D",
                index = dataIndex,
            )
            returnType to dataType
        } else {
            val returnType = createTypeParameterDescriptor(
                name = "R",
                index = 0,
            )
            returnType to null
        }

        desc.initialize(
            null,
            thisDescriptor.thisAsReceiverParameter,
            listOfNotNull(returnType, dataType),
            listOfNotNull(
                makeValueParameterDescriptor(
                    containingDeclaration = desc,
                    index = 0,
                    name = Name.identifier("visitor"),
                    type = KotlinTypeFactory.simpleType(
                        baseType = visitorType,
                        arguments = listOfNotNull(
                            returnType.defaultType.let { TypeProjectionImpl(Variance.INVARIANT, it) },
                            dataType?.defaultType?.let { TypeProjectionImpl(Variance.INVARIANT, it) },
                        )
                            .let {
                                if (hasVisitor.hasCustomDataParam)
                                    it.invertTwoIfTrue(hasVisitor.invertTypeParamsOfVisitor).toList()
                                else it
                            },
                    ),
                    source = returnType.source,
                ),
                dataType?.let { _ ->
                    makeValueParameterDescriptor(
                        containingDeclaration = desc,
                        index = 1,
                        name = Name.identifier("data"),
                        type = dataType.defaultType,
                        source = dataType.source,
                    )
                },
            ),
            returnType.defaultType,
            Modality.OPEN,
            DescriptorVisibilities.PUBLIC,
            mapOf(
                AcceptMethodData to AcceptMethodData()
            )
        )
        result.add(desc)
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
