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
    open fun visit(value: Sealed.Value1): R = throw null!!
    open fun visit(value: Sealed.Value2): R = throw null!!
    // visit for Sealed is missing
//    abstract fun visit(value: Sealed): R
}
