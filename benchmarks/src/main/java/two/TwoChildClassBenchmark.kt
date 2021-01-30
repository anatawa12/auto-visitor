package two

import kotlinx.benchmark.*
import seven.*
import kotlin.random.Random

@State(Scope.Benchmark)
class TwoChildClassBenchmark {
    lateinit var valueClass: TwoChildClass

    @Setup
    fun setup() {
        valueClass = when (Random.nextInt(2)) {
            0 -> TwoChildClassChild0()
            1 -> TwoChildClassChild1()
            else -> error("never here")
        }
    }

    @Benchmark
    fun whenExpr(): String {
        return when (valueClass) {
            is TwoChildClassChild0 -> "TwoChildClassChild0 got"
            is TwoChildClassChild1 -> "TwoChildClassChild1 got"
            else -> "otherClass"
        }
    }

    @Benchmark
    fun visitor(): String {
        return valueClass.accept(object : TwoChildClassVisitor<String, Nothing?>() {
            override fun visitRoot(value: TwoChildClass, data: Nothing?): String = "otherClass"
            override fun visitChild0(value: TwoChildClassChild0, data: Nothing?) = "TwoChildClassChild0 got"
            override fun visitChild1(value: TwoChildClassChild1, data: Nothing?) = "TwoChildClassChild1 got"
        }, null)
    }
}
