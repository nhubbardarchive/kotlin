package foo

//
//inline fun foo(): Char? {
//    return 'K'
//}

//fun foo(c: Any): Boolean {
//    return c == 'c'
//}
//
//
//fun bar(c: Char): Any {
//    return c
//}

//fun eq(a: Any?, b: Any?): Boolean {
//    return a == b
//}

//fun <T> Appendable.appendElement2(element: T, transform: ((T) -> CharSequence)?) {
//    when {
//        transform != null -> append(transform(element))
//        element is CharSequence? -> append(element)
//        element is Char -> append(element)
//        else -> append(element.toString())
//    }
//}
//
//public fun <T, A : Appendable> Iterable<T>.joinTo2(buffer: A, separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", transform: ((T) -> CharSequence)? = null): A {
//    buffer.append(prefix)
//    var count = 0
//    for (element in this) {
//        if (++count > 1) buffer.append(separator)
//        if (limit < 0 || count <= limit) {
//            buffer.appendElement2(element, transform)
//        } else break
//    }
//    if (limit >= 0 && count > limit) buffer.append(truncated)
//    buffer.append(postfix)
//    return buffer
//}
//
///**
// * Creates a string from all the elements separated using [separator] and using the given [prefix] and [postfix] if supplied.
// *
// * If the collection could be huge, you can specify a non-negative value of [limit], in which case only the first [limit]
// * elements will be appended, followed by the [truncated] string (which defaults to "...").
// */
//public fun <T> Iterable<T>.joinToString2(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", transform: ((T) -> CharSequence)? = null): String {
//    return joinTo2(StringBuilder(), separator, prefix, postfix, limit, truncated, transform).toString()
//}
//
//
//
//fun <T> foo(t: T): Boolean {
//    return t is Char
//}

//
//public fun Char.equals2(other: Char, ignoreCase: Boolean = false): Boolean {
//    if (this === other) return true
//    if (!ignoreCase) return false
//
//    if (this.toUpperCase() === other.toUpperCase()) return true
//    if (this.toLowerCase() === other.toLowerCase()) return true
//    return false
//}
//
//fun foo(c: Char): Any {
//    return c
//}
//
//
//open class A<T> {
//    open fun foo(c: T): Any = 1
//}
//
//class B : A<Char>() {
//    override fun foo(c: Char): Char { return 'a' }
//}


//fun <T> id(t: T) = t

//class CharIterable : Iterable<Char> {
//    override fun iterator(): Iterator<Char> {
//        return CharIterator()
//    }
//}
//
//class CharIterator : Iterator<Char> {
//    val s = "abc"
//    var i = 0
//
//    override fun hasNext(): Boolean {
//        return i < s.length
//    }
//
//    override fun next(): Char {
//        return s[i++]
//    }
//}
//
//fun <T> addAll(it: Iterable<T>): List<T> {
//    val result = ArrayList<T>()
//    for (q in it) {
//        result.add(q)
//    }
//    return result
//}

//fun <T> expect(t: T, f: () -> T) {
//    if (t != f()) throw Error()
//}
//
//open class A <T, F> {
//    open val foo: T get() = null!!
//    open fun bar(a: T): T = a
//    open fun baz(a: F): F = a
//}
//
//interface C {
//    val foo: Int get() = 0
//    fun bar(a: Int): Int = a
//    fun baz(a: Char): Char = a
//}
//
//class B : A<Int, Char>(), C {
//    override val foo: Int
//        get() = 0
//
//    override fun bar(a: Int): Int = a
//    override fun baz(a: Char): Char = a
//}

//fun test(a: A<Char>) {
//    a.baz('a')
//}

//fun main(args: Array<String>) {
//}


//fun foo(c: Char): Char {
//    return c
//}
//
//interface A {
//    fun foo(t: Char): Any
//}
//
//open class C {
//    open fun foo(c: Char): Char {
//        return c
//    }
//}
//
//class B: A, C() {
////    override fun foo(c: Char): Char {
////        return c as Char
////    }
//}
//
//class CC(val s : CharSequence) : CharSequence by s, E {
//}
//
//interface E {
//    open operator fun get(index: Int): Char
//}
//
//interface U {
//    val a: Any
//}
//
//open class I {
//    open val a: Char = 'a'
//}
//
//class O: U, I() {}
//
//
//open class Base {
//    open fun getChar(): Char {
//        return 's'
//    }
//}
//
//class Derived: Base() {
//    override fun getChar(): Char {
//        return 'a'
//    }
//}
//
//
//class P {
//    val f: Char = 'a'
//}
//
//inline fun test(c: Int?): Int? {
//    if (c == null) return null
//    return c
//}


//fun ff(s: String, o: Any?): String {
//    return s + o
//}

//fun <T> T.foo(): Char {
//    if (this is Char) {
//        return this
//    }
//    else {
//        return '!'
//    }
//}
//
//fun Char.baz(): Char {
//    return this
//}
//
//public fun <N : Any> doTest(
//        sequence: Iterable<N>,
//        expectedFirst: N,
//        expectedLast: N,
//        expectedIncrement: Number,
//        expectedElements: List<N>
//) {
//    val first: Any
//    val last: Any
//    val increment: Number
//    when (sequence) {
//        is IntProgression -> {
//            first = sequence.first
//            last = sequence.last
//            increment = sequence.step
//        }
//        is LongProgression -> {
//            first = sequence.first
//            last = sequence.last
//            increment = sequence.step
//        }
//        is CharProgression -> {
//            first = sequence.first
//            last = sequence.last
//            increment = sequence.step
//        }
//        else -> throw IllegalArgumentException("Unsupported sequence type: $sequence")
//    }
//
//    assertEquals(expectedFirst, first)
//    assertEquals(expectedLast, last)
//    assertEquals(expectedIncrement, increment)
//
//    if (expectedElements.isEmpty())
//        assertTrue(sequence.none())
//    else
//        assertEquals(expectedElements, sequence.toList())
//}

fun assertArrayNotSameButEquals(expected: CharArray, actual: CharArray, message: String = "") {
    assertTrue(expected !== actual && expected contentEquals actual, message)
}

fun box(): String {

    fun foo(l: Any?): String {
        return "" + l
    }

    assertEquals("9223372036854775807", foo(Long.MAX_VALUE))

//    assertArrayNotSameButEquals(charArrayOf('A', 'B'), charArrayOf('A', 'B', 'C').copyOf(2))
//    assertArrayNotSameButEquals(charArrayOf('A', 'B', '\u0000'), charArrayOf('A', 'B').copyOf(3))

//    val da = charArrayOf('d')
//    da[0] = 'k'
//
////    doTest(CharRange.EMPTY, 1.toChar(), 0.toChar(), 1, listOf())
////
//    val charList = listOf('a', 'b')
//
//    val charArray: CharArray = CharArray(2)
//    var index = 0
//    for (element in charList)
//        charArray[index++] = element

//    val charArray: CharArray = charList.toCharArray()
//    assertEquals(charList, charArray.asList())
//
//
//    val arr = arrayOf("aa", 1, null, charArrayOf('d'))
//    assertEquals("[aa, 1, null, [d]]", arr.contentDeepToString())


//    val d: Base = Derived()
//    val l: List<Char> = listOf(d.getChar())
//
//    assertTrue(l.first() is Char)

//    val a = B() as A
//    if (a.foo('a') !is Char) return "fail"

//    val f = String::plus

//    val k = 'K' as Any

//    val o = 'O'.foo()
//    val k = 'K'.baz()
//    return "$o$k"

//    var s = "O"
//    s += 'K'
//
//    return s;//"O" + "K"


//    val q1 = arrayOf(1, 2, 3)
//    val q2 = IntArray(10)
//    val q3 = CharArray(10)
//
//
//
//    val r = test(97)
//
//    P()
//
//    val d: Base = Derived()
//    val l: List<Char> = listOf(d.getChar())
//    assertTrue(l.first() is Char)
//
//
//    val s = "safadsfas"
//    var q = ""
//    for (i in s.indices) {
//        q += s[i]
//    }
//
//    if (foo('a') != 'a') return "fail"

//    B().apply {
//        foo
//        val a = foo
//        bar(1)
//        val b = bar(1)
//        baz('a')
//        val c = baz('a')
//    }


//    val a: A<Char> = B()
//    if (a.foo('a') !is Char) return "fail"

//    val q = id('a')
//
//    val l = 'f'.let { ('a'..it) + ' ' }
//    assertEquals("abcdef ".toList(), l)

//    val q : () -> Any = { 'A' }
//
//    expect('c') {
//        'c'
////        "ab".reduceIndexed { index, acc, e ->
////            assertEquals(1, index)
////            assertEquals('a', acc)
////            assertEquals('b', e)
////            e + (e - acc)
////        }
//    }
//    assertEquals("abc".toList(), addAll(CharIterable()))

//    val data = StringBuilder("abcd")
//    val result = data.flatMap { ('a'..it) + ' ' }
//    assertEquals("a ab abc abcd ".toList(), result)

//    val a: Any = 'a'
//    val c: Char = a as Char
//
//    if (c != 'a') return "fail"

//    'a'.let { it.equals('a') }

//    val charAtIndex = 'a'
//    val f = arrayOf('b', 'a', 'c').indexOfFirst { it.equals2(charAtIndex, true) }
//
//    if (f != 1) return "fail"
//


//    if (!'a'.equals2('A', true)) return "fail"

//    val string = createStringBuilder("bcedef")
//    assertEquals(2, string.indexOf('e'))
//    withOneCharSequenceArg("bcedef") { string ->
//        assertEquals(2, string.indexOf('e'))
//    }

//    if (("asdfasdf" as CharSequence).indexOf('a') < 0) return "fail"

//    if (!foo('a')) return "fail0"
//
//    val data = "abcd".toList()
//    val q = ArrayList<Char>()
//    q.add('c')
//    val ee = q[0]

//    val result = data.joinToString2("_", "(", ")")
//
//    if (result != "(a_b_c_d)") return "fail"
//
//    if (!eq('a', "abc".firstOrNull())) return "fail"
//
//    if (!'a'.equals('a')) return "fail"
//
//    if ('a'.compareTo('b') >= 0) return "fail"

//
//    val aa = bar('f')
//
//    val a7: Any = 'A'.plus(1)
//
//    if (a7 is Char && a7 == 'B') return "OK"
//    else return "fail"
//
//    if (a7!! != 'B') return "fail 7"
//
//    if (!foo('c')) return "fail is"
//
//    val q : Any = 's'
//
//    if (q != 's') return "fail assignment"
//
//    if ('O'.toString() + "K" != "OK") return "fail toString"
//
//    if (('O' as Any) != 'O') return "fail eq"

//
//    if (!' '.isWhitespace()) return "fail white"
//
//    var q = "O${foo()}"
//    if (q != "OK") return "fail122"
//
//    val s = StringBuilder()
//    s.append("a")
//    s.append("b").append("c")
//    s.append('d').append("e")
//
//    if (s.toString() != "abcde") return s.toString()
    return "OK"
}