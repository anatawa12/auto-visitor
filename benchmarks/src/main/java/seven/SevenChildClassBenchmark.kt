package seven

import kotlinx.benchmark.*
import kotlin.random.Random

@State(Scope.Benchmark)
class SevenChildClassBenchmark {
    lateinit var valueClass: SevenChildClass

    @Setup
    fun setup() {
        valueClass = when (Random.nextInt(7)) {
            0 -> SevenChildClassChild0()
            1 -> SevenChildClassChild1()
            2 -> SevenChildClassChild2()
            3 -> SevenChildClassChild3()
            4 -> SevenChildClassChild4()
            5 -> SevenChildClassChild5()
            6 -> SevenChildClassChild6()
            else -> error("never here")
        }
    }

    @Benchmark
    fun whenExpr(): String {
        return when (valueClass) {
            is SevenChildClassChild0 -> "SevenChildClassChild0 got"
            is SevenChildClassChild1 -> "SevenChildClassChild1 got"
            is SevenChildClassChild2 -> "SevenChildClassChild2 got"
            is SevenChildClassChild3 -> "SevenChildClassChild3 got"
            is SevenChildClassChild4 -> "SevenChildClassChild4 got"
            is SevenChildClassChild5 -> "SevenChildClassChild5 got"
            is SevenChildClassChild6 -> "SevenChildClassChild6 got"
            else -> "otherClass"
        }
    }

    @Benchmark
    fun visitor(): String {
        return valueClass.accept(object : SevenChildClassVisitor<String, Nothing?>() {
            override fun visitRoot(value: SevenChildClass, data: Nothing?): String = "otherClass"
            override fun visitChild0(value: SevenChildClassChild0, data: Nothing?) = "SevenChildClassChild0 got"
            override fun visitChild1(value: SevenChildClassChild1, data: Nothing?) = "SevenChildClassChild1 got"
            override fun visitChild2(value: SevenChildClassChild2, data: Nothing?) = "SevenChildClassChild2 got"
            override fun visitChild3(value: SevenChildClassChild3, data: Nothing?) = "SevenChildClassChild3 got"
            override fun visitChild4(value: SevenChildClassChild4, data: Nothing?) = "SevenChildClassChild4 got"
            override fun visitChild5(value: SevenChildClassChild5, data: Nothing?) = "SevenChildClassChild5 got"
            override fun visitChild6(value: SevenChildClassChild6, data: Nothing?) = "SevenChildClassChild6 got"
        }, null)
    }
}
