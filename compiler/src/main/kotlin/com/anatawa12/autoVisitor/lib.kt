package com.anatawa12.autoVisitor

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class GenerateVisitor(
    /**
     *
     * if starts with '.', it is name of child class.
     * if else, it is name of the class at same package.
     */
    val visitorName: String,
)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class HasVisitor(
    /**
     * The visitor abstract class.
     * If annotated with [GenerateVisitor], this will be ignored and computed automatically so You can set any class.
     */
    val visitorType: KClass<*>,
    /**
     * if true, the accept method will have 'value' argument and visit methods will have 'value' parameter.
     * if not, the accept method will have only 'object' argument and visit methods will not have 'value' parameter.
     */
    val hasCustomDataParam: Boolean,
    /**
     * The name of accept function.
     *
     * if [hasCustomDataParam] is true, the signature of accept function is shown like below:
     *
     * ```
     * fun <R, D> accept(Visitor<R, D> visitor, value: T): R
     * ```
     *
     * if [hasCustomDataParam] is false, the signature of accept function is shown like below:
     *
     * ```
     * fun <R> accept(Visitor<R> visitor): R
     * ```
     */
    val acceptName: String,
    /**
     * the subclasses.
     * if [GenerateVisitor] was marked and it's an sealed class,
     * you can make empty array and it's automatically computed.
     */
    val subclasses: Array<KClass<*>>,
    /**
     * if true, inverts sorting of type params of visitor.
     * In the default, the order of type params are '<R, D>' but if true, '<D, R>'.
     * This should not be true if [hasCustomDataParam] is false.
     */
    val invertTypeParamsOfVisitor: Boolean = false,
    /**
     * if true, inverts sorting of type params of visitor.
     * In the default, the order of type params are '<R, D>' but if true, '<D, R>'.
     * This should not be true if [hasCustomDataParam] is false.
     */
    val invertTypeParamsOfAccept: Boolean = false,
)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class HasAccept(
    /**
     * The name of visit method. defaults `visit<SimpleName>`.
     * automatically computed by plugin if empty.
     */
    val visitName: String,
    /**
     * The class which has declaration of 'accept' function.
     * automatically computed by plugin.
     */
    val rootClass: KClass<*>,
)


/**
 * calls 'accept' function manually.
 * you must call like shown below:
 *
 * ```kotlin
 * autoVisitor(some_expr) { variable ->
 *     when (variable) {
 *         is SomeClass -> {
 *             statements
 *         }
 *     }
 * }
 * ```
 */
@OptIn(ExperimentalContracts::class)
fun <T, R> autoVisitor(value: T, block: (T) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return block(value)
}
