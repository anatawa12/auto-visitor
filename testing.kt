import com.anatawa12.autoVisitor.*;

import kotlin.reflect.KClass;

fun main() {
    println("i:Sealed.Value1:")
    block(Sealed.Value1())
    println("i:Sealed.Value2:")
    block(Sealed.Value2())
    println("i:Sealed.Sealed1.Value3:")
    block(Sealed.Sealed1.Value3())

    println("o:Sealed.Sealed1.Value4")
    block(Sealed.Sealed1.Value4)
    println("o:Sealed.Sealed1.Value3")
    block(Sealed.Sealed1.Value3)
}

fun block(block1: Sealed) {
    autoVisitor(block1) { block ->
        when (block) {
            is Sealed.Value1 -> println("Value1: ${block.dome()}")
//            is Sealed.Value2 -> println("Value2: ${object : Iterable<String> {
//                override fun iterator(): Iterator<String> = object : Iterator<String> {
//                    override fun hasNext(): Boolean = true
//                    override fun next(): String = block1.toString()
//                }
//                override fun equals(other: Any?): Boolean {
//                    return this === other
//                }
//            }}")
            is Sealed.Sealed1 -> println("Sealed1")
            is Sealed.Sealed1.Value3 -> println("Value3")
            Sealed.Sealed1.Value4 -> println("Value4")
            Sealed.Sealed1.Value3 -> println("Value4")
            else -> Unit
        }
    }
//    print()
}

@HasVisitor(
    visitorType = Sealed.Visitor::class,
    acceptName = "accept",
    subclasses = [
        Sealed.Value1::class,
        Sealed.Value2::class,
        Sealed.Sealed1.Value3.Companion::class,
        Sealed.Sealed1::class,
        Sealed.Sealed1.Value3::class,
        Sealed.Sealed1.Value4::class,
    ],
    hasCustomDataParam = true,
)
sealed class Sealed {
    abstract fun <R, D> accept(visitor: Sealed.Visitor<R, D>, data: D): R

    class Value1 : Sealed() {
        override fun <R, D> accept(visitor: Sealed.Visitor<R, D>, data: D): R = visitor.visitValue1(this, data)
        fun dome() {}
    }
    class Value2 : Sealed() {
        override fun <R, D> accept(visitor: Sealed.Visitor<R, D>, data: D): R = visitor.visitValue2(this, data)
    }
    sealed class Sealed1 : Sealed() {
        class Value3 : Sealed1() {
            override fun <R, D> accept(visitor: Sealed.Visitor<R, D>, data: D): R = visitor.visitValue3(this, data)
            @HasAccept(visitName = "visitValue3Companion", rootClass = Sealed::class)
            companion object : Sealed() {
                override fun <R, D> accept(visitor: Sealed.Visitor<R, D>, data: D): R = visitor.visitValue3Companion(this, data)
            }
        }
        object Value4 : Sealed1() {
            override fun <R, D> accept(visitor: Sealed.Visitor<R, D>, data: D): R = visitor.visitValue4(this, data)
        }
    }

    @GenerateVisitor(Sealed::class)
    abstract class Visitor<out R, in D> {
    }
}
