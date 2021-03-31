import com.anatawa12.autoVisitor.*;

import kotlin.reflect.KClass;

@HasVisitor(
    visitorType = Sealed.Visitor::class,
    acceptName = "accept",
    subclasses = [
        Sealed.Sealed1.Value3::class,
        Sealed.Sealed1.Value4::class,
    ],
    hasCustomDataParam = true,
)
@GenerateAccept
sealed class Sealed {
    sealed class Sealed1 : Sealed() {
        class Value3 : Sealed1() {
        }

        object Value4 : Sealed1() {
        }
    }

    @GenerateVisitor(Sealed::class)
    abstract class Visitor<out R, in D> {
    }
}
