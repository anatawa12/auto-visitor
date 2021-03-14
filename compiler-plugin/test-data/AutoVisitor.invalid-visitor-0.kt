import com.anatawa12.autoVisitor.*;

import kotlin.reflect.KClass;

@HasVisitor(
    visitorType = Visitor::class,
    acceptName = "accept",
    subclasses = [
        Sealed.Value1::class,
        Sealed.Value2::class,
    ],
    hasCustomDataParam = false,
)
@HasAccept("visit", Sealed::class)
sealed class Sealed {
    abstract fun <R> accept(visitor: Visitor<R>): R

    @HasAccept("visit", Sealed::class)
    class Value1 : Sealed() {
        override fun <R> accept(visitor: Visitor<R>) = visitor.visit(this)
    }

    @HasAccept("visit", Sealed::class)
    class Value2 : Sealed() {
        override fun <R> accept(visitor: Visitor<R>) = visitor.visit(this)
    }
}

abstract class Visitor<out R> {
    open fun visit(value: Sealed.Value1): R = visit(value as Sealed)

    // visit for Value2 is missing
//    open fun visit(value: Sealed.Value2): R = visit(value as Sealed)
    abstract fun visit(value: Sealed): R
}

fun test(value: Sealed) = autoVisitor(value) { value1 ->
    when (value1) {
        is Sealed.Value1 -> println("Value1")
        is Sealed.Value2 -> println("Value2")
    }
}
