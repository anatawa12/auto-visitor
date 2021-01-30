package two

abstract class TwoChildClass {
    abstract fun <R, D> accept(visitor: TwoChildClassVisitor<R, D>, data: D): R
}

class TwoChildClassChild0 : TwoChildClass() {
    override fun <R, D> accept(visitor: TwoChildClassVisitor<R, D>, data: D): R = visitor.visitChild0(this, data)
}

class TwoChildClassChild1 : TwoChildClass() {
    override fun <R, D> accept(visitor: TwoChildClassVisitor<R, D>, data: D): R = visitor.visitChild1(this, data)
}

abstract class TwoChildClassVisitor<out R, in D> {
    abstract fun visitRoot(value: TwoChildClass, data: D): R
    open fun visitChild0(value: TwoChildClassChild0, data: D): R = visitRoot(value, data)
    open fun visitChild1(value: TwoChildClassChild1, data: D): R = visitRoot(value, data)
}
