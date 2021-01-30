package com.anatawa12.autoVisitor.backend.ir

import com.anatawa12.autoVisitor.*
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.ir.createParameterDeclarations
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeBuilder
import org.jetbrains.kotlin.ir.types.impl.buildSimpleType
import org.jetbrains.kotlin.ir.types.impl.makeTypeProjection
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.utils.addToStdlib.cast


class FunctionCallTransformer(
    val moduleFragment: IrModuleFragment,
    val pluginContext: IrPluginContext,
) : IrElementTransformerVoid() {
    private val autoVisitor = Symbols.getAutoVisitorFunction(pluginContext)
    private val irFactory get() = pluginContext.irFactory
    private val builtIns get() = pluginContext.irBuiltIns

    private val parentReplacements = mutableMapOf<IrDeclarationParent, IrDeclarationParent>()

    override fun visitDeclaration(declaration: IrDeclarationBase): IrStatement {
        try {
            val replacement = parentReplacements[declaration.parent]
            if (replacement != null)
                declaration.parent = replacement
        } catch (ignore: Throwable) {}
        return super.visitDeclaration(declaration)
    }

    private val variableReplacements = mutableMapOf<IrValueSymbol, IrValueSymbol>()

    override fun visitGetValue(expression: IrGetValue): IrExpression {
        return replaceValueSymbol(expression) {
            IrGetValueImpl(
                startOffset = expression.startOffset,
                endOffset = expression.endOffset,
                type = expression.type,
                symbol = it,
                origin = expression.origin,
            )
        }
    }

    override fun visitSetValue(expression: IrSetValue): IrExpression {
        return replaceValueSymbol(expression) {
            IrSetValueImpl(
                startOffset = expression.startOffset,
                endOffset = expression.endOffset,
                type = expression.type,
                symbol = it,
                value = expression.value,
                origin = expression.origin,
            )
        }
    }

    private inline fun <T : IrValueAccessExpression> replaceValueSymbol(expression: T, replace: (IrValueSymbol) -> T): IrExpression {
        try {
            val replacement = variableReplacements[expression.symbol]
            if (replacement != null)
                return replace(replacement).also { it.transform(this, null) }
        } catch (ignore: Throwable) {}
        return super.visitDeclarationReference(expression)
    }

    inner class CallGeneratorCtx(
        private val data: VisitableData,
        expression: IrCall,
    ) {
        val returningType = expression.type

        val visitorType = IrSimpleTypeBuilder().apply {
            classifier = data.visitorType.classifierOrFail
            val returnType = makeTypeProjection(returningType, Variance.INVARIANT)
            val dataType = makeTypeProjection(builtIns.unitType, Variance.INVARIANT)
            arguments = if (data.hasCustomDataParam) {
                if (!data.invertTypeParamsOfVisitor) {
                    listOf(returnType, dataType)
                } else {
                    listOf(dataType, returnType)
                }
            } else {
                listOf(returnType)
            }
        }.buildSimpleType()

        val visitorCtor get() = data.visitorConstructor.symbol
        val methodsByType get() = data.methodsByType
        val rootType get() = data.rootType

        fun createCall(valueArgument: IrExpression, visitor: IrExpression): IrExpression {
            return IrCallImpl(
                startOffset = UNDEFINED_OFFSET,
                endOffset = UNDEFINED_OFFSET,
                type = returningType,
                symbol = data.acceptMethod.symbol,
                typeArgumentsCount = data.acceptMethod.typeParameters.size,
                valueArgumentsCount = data.acceptMethod.valueParameters.size,
            ).apply {
                dispatchReceiver = valueArgument
                if (data.hasCustomDataParam) {
                    putValueArgument(0, visitor)
                    putValueArgument(1, IrGetObjectValueImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, builtIns.unitType, builtIns.unitClass))
                    if (!data.invertTypeParamsOfAccept) {
                        putTypeArgument(0, returningType)
                        putTypeArgument(1, builtIns.unitType)
                    } else {
                        putTypeArgument(0, builtIns.unitType)
                        putTypeArgument(1, returningType)
                    }
                } else {
                    putValueArgument(0, visitor)
                    putTypeArgument(0, returningType)
                }
            }
        }
    }

    override fun visitCall(expression: IrCall): IrExpression {
        //*
        if (expression.symbol == autoVisitor) {
            println("autoVisitor")
            expression.dump("")
            check(expression.valueArgumentsCount == 2)
            val valueArgument = expression.getValueArgument(0) ?: error("valueArgument not found")
            val lambda = expression.getValueArgument(1)
            check(lambda is IrFunctionExpression) { "autoVisitor must be called with a lambda expr" }
            val func = lambda.function.body ?: error("no body on the lambda")
            val funcParam = lambda.function.valueParameters.single()
            val stat = func.statements.singleOrNull() ?: error("there are two or more statements inside the lambda")
            val block = stat as? IrBlock ?: unsupportedWhenExpr()
            if (block.origin != IrStatementOrigin.WHEN) unsupportedWhenExpr()
            if (block.statements.size != 2) unsupportedWhenExpr()
            val decl = block.statements[0] as? IrVariable ?: unsupportedWhenExpr()
            val variable = decl.symbol
            if (variable.owner.origin != IrDeclarationOrigin.IR_TEMPORARY_VARIABLE) unsupportedWhenExpr()
            val whenBlock = block.statements[1] as? IrWhen ?: unsupportedWhenExpr()

            valueArgument.type
            val ctx = CallGeneratorCtx(
                VisitableData.computeData(valueArgument.type) ?: error("invalid visitor"),
                expression,
            )

            val creatingBlock = IrBlockImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, ctx.visitorType, IrStatementOrigin.OBJECT_LITERAL)

            val newClass = irFactory.buildClass {
                name = Name.special("<no name provided>")
            }
            newClass.createParameterDeclarations()
            creatingBlock.statements.add(newClass)

            newClass.superTypes += ctx.visitorType
            newClass.parent = lambda.function.parent
            // create constructor
            val newClassCtor = newClass.addConstructor {
                visibility = DescriptorVisibilities.LOCAL
            }.apply {
                body = irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET).apply {
                    statements.add(IrDelegatingConstructorCallImpl(
                        startOffset = UNDEFINED_OFFSET, endOffset = UNDEFINED_OFFSET,
                        type = builtIns.unitType,
                        symbol = ctx.visitorCtor,
                        typeArgumentsCount = 0,
                        valueArgumentsCount = 0,
                    ))
                    statements.add(IrInstanceInitializerCallImpl(
                        startOffset = UNDEFINED_OFFSET, endOffset = UNDEFINED_OFFSET,
                        classSymbol = newClass.symbol,
                        type = builtIns.unitType,
                    ))
                }
            }

            creatingBlock.statements.add(IrConstructorCallImpl(
                startOffset = UNDEFINED_OFFSET, endOffset = UNDEFINED_OFFSET,
                type = IrSimpleTypeBuilder().apply { classifier = newClass.symbol }.buildSimpleType(),
                symbol = newClassCtor.symbol,
                typeArgumentsCount = 0,
                constructorTypeArgumentsCount = 0,
                valueArgumentsCount = 0,
            ))

            val methods = ctx.methodsByType.toMutableMap()

            for (branch in whenBlock.branches) {
                val type = when (val condition = branch.condition) {
                    is IrTypeOperatorCall -> {
                        if (!condition.argument.isVariableOf(variable)) unsupportedWhenBranch()
                        if (condition.operator != IrTypeOperator.INSTANCEOF) unsupportedWhenBranch()
                        condition.typeOperand
                    }
                    is IrCall -> {
                        if (condition.symbol != moduleFragment.irBuiltins.eqeqSymbol) unsupportedWhenBranch()
                        if (condition.valueArgumentsCount != 2) unsupportedWhenBranch()
                        if (!condition.getValueArgument(0).isVariableOf(variable)) unsupportedWhenBranch()
                        val classObject = condition.getValueArgument(1) as? IrGetObjectValue ?: unsupportedWhenBranch()
                        classObject.type
                    }
                    is IrConst<*> -> {
                        if (condition.value != true) unsupportedWhenBranch()
                        ctx.rootType
                    }
                    else -> unsupportedWhenBranch()
                }

                val method = methods[type] ?: branchForTheTypeNotFound(type)
                methods.remove(type)
                val function = newClass.addFunction {
                    originalDeclaration = method
                    name = method.name
                    returnType = ctx.returningType
                }
                function.overriddenSymbols += method.symbol
                function.dispatchReceiverParameter = buildReceiverParameter(
                    parent = function,
                    origin = IrDeclarationOrigin.INSTANCE_RECEIVER,
                    type = newClass.typeWith(),
                )

                val param = function.addValueParameter {
                    this.type = method.valueParameters[0].type
                    this.name = funcParam.name
                }

                function.body = visitBlockByWhenBranch(
                    irFactory.createExpressionBody(branch.result),
                    lambda.function to function,
                    funcParam.symbol to param.symbol,
                    //variable to variable,
                )
            }
            println("creatingBlock: ")
            println(creatingBlock.dump())

            return ctx.createCall(valueArgument, creatingBlock)
            // TODO: replace this one
        }
        // */


        return super.visitCall(expression)
    }

    private fun visitBlockByWhenBranch(
        createExpressionBody: IrExpressionBody,
        parentReplace: Pair<IrSimpleFunction, IrSimpleFunction>,
        variableReplace: Pair<IrValueSymbol, IrValueSymbol>,
    ): IrBody {
        try {
            parentReplacements[parentReplace.first] = parentReplace.second
            variableReplacements[variableReplace.first] = variableReplace.second
            createExpressionBody.transform(this, null)
        } finally {
            parentReplacements.remove(parentReplace.first)
            variableReplacements.remove(variableReplace.first)
        }
        return createExpressionBody
    }

    private fun IrExpression?.isVariableOf(variable: IrValueSymbol): Boolean =
        this is IrGetValue && this.symbol == variable

    private fun unsupportedWhenBranch(): Nothing {
        error("unsupported when branch. requires 'is Type' or 'ObjectType', and 'else'.")
    }
    private fun branchForTheTypeNotFound(type: IrType): Nothing {
        error("visit method for ${type.fqName} not found.")
    }
    private fun unsupportedWhenExpr(): Nothing {
        error("unsupported when. requires when (param) {}")
    }
}

class VisitableData(
    val rootType: IrType,
    val visitorType: IrType,
    val hasCustomDataParam: Boolean,
    val invertTypeParamsOfVisitor: Boolean,
    val invertTypeParamsOfAccept: Boolean,
    val visitorConstructor: IrConstructor,
    val methodsByType: Map<IrType, IrSimpleFunction>,
    val acceptMethod: IrSimpleFunction,
) {
    companion object {
        fun computeData(ofType: IrType): VisitableData? {
            val (rootType, hasVisitor) = ofType.getHasVisitorValue() ?: return null
            val (methodsByType, visitorConstructor) = hasVisitor.visitorType.checkVisitorType(rootType, hasVisitor)
            val irClass = rootType.classOrNull?.owner ?: return null
            println("irClass: ")
            println(irClass.dump())
            val acceptMethod = irClass.functions
                .filter { isAcceptMethod(it, hasVisitor) }
                .singleOrNull() ?: return null
            println("acceptMethod: $acceptMethod")
            return VisitableData(
                rootType = rootType,
                visitorType = hasVisitor.visitorType,
                hasCustomDataParam = hasVisitor.hasCustomDataParam,
                invertTypeParamsOfVisitor = hasVisitor.invertTypeParamsOfVisitor,
                invertTypeParamsOfAccept = hasVisitor.invertTypeParamsOfAccept,
                visitorConstructor = visitorConstructor,
                methodsByType = methodsByType,
                acceptMethod = acceptMethod,
            )
        }

        private fun isAcceptMethod(func: IrSimpleFunction, hasVisitor: HasVisitorValue): Boolean {
            if (func.name.identifier != hasVisitor.acceptName) return false
            if (hasVisitor.hasCustomDataParam) {
                if (func.typeParameters.size != 2) return false
                if (func.valueParameters.size != 2) return false
                val (typeR, typeD) = func.typeParameters.invertTwoIfTrue(hasVisitor.invertTypeParamsOfAccept)
                if (!typeR.isAnyVariable()) return false
                if (!typeD.isAnyVariable()) return false
                if (func.returnType.classifierOrNull != typeR.symbol) return false
                val (visitorArg, dataArg) = func.valueParameters
                if (!visitorArg.type.isVisitorType(hasVisitor, typeR, typeD)) return false
                if (dataArg.type.classifierOrNull != typeD.symbol) return false
            } else {
                if (func.typeParameters.size != 1) return false
                if (func.valueParameters.size != 1) return false
                val (typeR) = func.typeParameters
                if (!typeR.isAnyVariable()) return false
                val (visitorArg) = func.valueParameters
                if (!visitorArg.type.isVisitorType(hasVisitor, typeR, null)) return false
                if (func.returnType.classifierOrNull != typeR.symbol) return false
            }
            return true
        }

        private val IrType.irClassSymbolOrFail get() = classifierOrFail.cast<IrClassSymbol>()

        private data class VisitorTypeInfo (
            val methodsByType: Map<IrType, IrSimpleFunction>,
            val constructor: IrConstructor,
        )

        private fun IrType.checkVisitorType(rootType: IrType, hasVisitor: HasVisitorValue) =
            irClassSymbolOrFail.owner.checkVisitorType(rootType, hasVisitor)

        private fun IrClass.checkVisitorType(rootType: IrType, hasVisitor: HasVisitorValue): VisitorTypeInfo {
            check(kind == ClassKind.CLASS) { "visitor is not a abstract class" }
            check(modality == Modality.ABSTRACT) { "visitor is not a abstract class" }

            // check constructor
            val constructor = constructors.firstOrNull() {
                if (it.valueParameters.isNotEmpty()) return@firstOrNull false
                if (it.typeParameters.isNotEmpty()) return@firstOrNull false
                true
            } ?: error("no constructor with no arguments found.")

            // check type param and functions
            val visitChecker: (IrSimpleFunction, IrType) -> Boolean
            if (hasVisitor.hasCustomDataParam) {
                check(typeParameters.size == 2)
                val (typeR, typeD) = typeParameters.invertTwoIfTrue(hasVisitor.invertTypeParamsOfVisitor)
                check(typeR.isAnyVariable())
                check(typeD.isAnyVariable())
                visitChecker = fun (func, type): Boolean {
                    if (func.typeParameters.isNotEmpty()) return false
                    if (func.valueParameters.size != 2) return false
                    if (func.returnType.classifierOrNull != typeR.symbol) return false
                    val (value, data) = func.valueParameters
                    if (value.type != type) return false
                    if (data.type.classifierOrNull != typeD.symbol) return false
                    return true
                }
            } else {
                check(typeParameters.size == 1)
                val (typeR) = typeParameters
                check(typeR.isAnyVariable())
                visitChecker = fun (func, type): Boolean {
                    if (func.typeParameters.isNotEmpty()) return false
                    if (func.valueParameters.size != 1) return false
                    if (func.returnType.classifierOrNull != typeR.symbol) return false
                    val (value) = func.valueParameters
                    if (value.type != type) return false
                    return true
                }
            }

            val methodsByType = hashMapOf<IrType, IrSimpleFunction>()

            val simpleTypeOfThisClass = IrSimpleTypeBuilder().also { it.classifier = rootType.irClassSymbolOrFail }.buildSimpleType()

            var rootName: String? = rootType.getVisitMethodName(this)
            val typesByVisitName = hasVisitor.subclasses.groupByTo(hashMapOf()) { it.getVisitMethodName(this) }

            // check functions
            for (declaration in declarations) {
                when (declaration) {
                    is IrSimpleFunction -> {
                        var used = false
                        if (declaration.name.identifier == rootName) {
                            if (visitChecker(declaration, simpleTypeOfThisClass)) {
                                check(!used)
                                check(declaration.modality == Modality.OPEN || declaration.modality == Modality.ABSTRACT)
                                rootName = null
                                used = true
                                methodsByType[simpleTypeOfThisClass] = declaration
                            }
                        }
                        typesByVisitName[declaration.name.identifier]?.let { types ->
                            val it = types.iterator()
                            while (it.hasNext()) {
                                val type = it.next()
                                if (visitChecker(declaration, type)) {
                                    check(!used)
                                    check(declaration.modality == Modality.OPEN)
                                    it.remove()
                                    used = true
                                    methodsByType[type] = declaration
                                }
                            }
                        }
                        if (!used)
                            check(declaration.modality != Modality.ABSTRACT) { "it's abstract: ${declaration.name}" }
                    }
                    is IrOverridableMember -> {
                        check(declaration.modality != Modality.ABSTRACT) { "it's abstract: ${declaration.name}" }
                    }
                }
            }

            check(typesByVisitName.values.all { it.isEmpty() })
            return VisitorTypeInfo(
                methodsByType = methodsByType,
                constructor = constructor,
            )
        }

        private fun IrType.getVisitMethodName(forClass: IrClass): String {
            return irClassSymbolOrFail.owner.getVisitMethodName(forClass)
        }

        private fun IrClass.getVisitMethodName(forClass: IrClass): String {
            val hasAccept = annotations
                .firstOrNull() { it.type.isClassType(Symbols.hasAccept.toUnsafe()) }
                ?.let { HasAcceptValue.fromIrConstructorCall(it) }
                ?: error("HasAccept annotation not found at ${forClass.fqNameWhenAvailable}")
            check(hasAccept.rootClass.classifierOrNull != forClass.symbol) { "invalid HasAccept rootClass" }
            return hasAccept.visitName
        }

        private fun IrType.isVisitorType(hasVisitor: HasVisitorValue, typeR: IrTypeParameter, typeD: IrTypeParameter?): Boolean {
            if (this !is IrSimpleType) return false
            if (classifier != hasVisitor.visitorType.classifierOrFail) return false
            if (typeD != null) {
                if (arguments.size != 2) return false
                val (first, second) = listOf(typeR, typeD).invertTwoIfTrue(hasVisitor.invertTypeParamsOfVisitor)
                if (arguments[0].typeOrNull?.classifierOrNull != first.symbol) return false
                if (arguments[1].typeOrNull?.classifierOrNull != second.symbol) return false
            } else {
                if (arguments.size != 1) return false
                if (arguments[0].typeOrNull?.classifierOrNull != typeR.symbol) return false
            }
            return true
        }

        private fun IrType.getHasVisitorValue(): Pair<IrType, HasVisitorValue>? {
            return getHasVisitorValue(this)
        }

        @JvmName("getHasVisitorValueInternal")
        private fun getHasVisitorValue(self: IrType): Pair<IrType, HasVisitorValue>? {
            if (self.classOrNull?.owner?.kind != ClassKind.CLASS) return null
            @Suppress("NAME_SHADOWING")
            var self = self
            while (true) {

                val owner = (self as? IrSimpleType)
                    ?.classifier
                    ?.let { it as? IrClassSymbol }
                    ?.owner
                    ?: return null
                val annotation = owner.annotations.firstOrNull { ctorCall ->
                    ctorCall.type.isClassType(Symbols.hasVisitor.toUnsafe())
                }
                if (annotation != null)
                    return self to HasVisitorValue.fromIrConstructorCall(annotation)

                self = self.superTypes().firstOrNull { it.classOrNull?.owner?.kind == ClassKind.CLASS } ?: return null
            }
        }
    }
}
