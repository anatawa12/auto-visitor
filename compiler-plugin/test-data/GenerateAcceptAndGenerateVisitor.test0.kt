import com.anatawa12.autoVisitor.*;

import kotlin.reflect.KClass;

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
@GenerateAccept
sealed class Sealed {
    class Value1 : Sealed() {
        fun dome() {}
    }

    class Value2 : Sealed() {
    }

    sealed class Sealed1 : Sealed() {
        class Value3 : Sealed1() {
            @HasAccept(visitName = "visitValue3Companion", rootClass = Sealed::class)
            companion object : Sealed() {
            }
        }

        object Value4 : Sealed1() {
        }
    }

    @GenerateVisitor(Sealed::class)
    abstract class Visitor<out R, in D> {
    }
}
