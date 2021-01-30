package seven

abstract class SevenChildClass {
    abstract fun <R, D> accept(visitor: SevenChildClassVisitor<R, D>, data: D): R
}

class SevenChildClassChild0 : SevenChildClass() {
    override fun <R, D> accept(visitor: SevenChildClassVisitor<R, D>, data: D): R = visitor.visitChild0(this, data)
}

class SevenChildClassChild1 : SevenChildClass() {
    override fun <R, D> accept(visitor: SevenChildClassVisitor<R, D>, data: D): R = visitor.visitChild1(this, data)
}

class SevenChildClassChild2 : SevenChildClass() {
    override fun <R, D> accept(visitor: SevenChildClassVisitor<R, D>, data: D): R = visitor.visitChild2(this, data)
}

class SevenChildClassChild3 : SevenChildClass() {
    override fun <R, D> accept(visitor: SevenChildClassVisitor<R, D>, data: D): R = visitor.visitChild3(this, data)
}

class SevenChildClassChild4 : SevenChildClass() {
    override fun <R, D> accept(visitor: SevenChildClassVisitor<R, D>, data: D): R = visitor.visitChild4(this, data)
}

class SevenChildClassChild5 : SevenChildClass() {
    override fun <R, D> accept(visitor: SevenChildClassVisitor<R, D>, data: D): R = visitor.visitChild5(this, data)
}

class SevenChildClassChild6 : SevenChildClass() {
    override fun <R, D> accept(visitor: SevenChildClassVisitor<R, D>, data: D): R = visitor.visitChild6(this, data)
}

abstract class SevenChildClassVisitor<out R, in D> {
    abstract fun visitRoot(value: SevenChildClass, data: D): R
    open fun visitChild0(value: SevenChildClassChild0, data: D): R = visitRoot(value, data)
    open fun visitChild1(value: SevenChildClassChild1, data: D): R = visitRoot(value, data)
    open fun visitChild2(value: SevenChildClassChild2, data: D): R = visitRoot(value, data)
    open fun visitChild3(value: SevenChildClassChild3, data: D): R = visitRoot(value, data)
    open fun visitChild4(value: SevenChildClassChild4, data: D): R = visitRoot(value, data)
    open fun visitChild5(value: SevenChildClassChild5, data: D): R = visitRoot(value, data)
    open fun visitChild6(value: SevenChildClassChild6, data: D): R = visitRoot(value, data)
}
