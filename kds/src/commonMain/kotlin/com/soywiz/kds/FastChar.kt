package com.soywiz.kds

// @TODO: This is required because Char and CharArray is slow on JS
// @TODO: https://youtrack.jetbrains.com/issue/KT-46443

inline class FastChar(val code: Int) {

}

expect fun String.getFast(index: Int): FastChar
inline fun Int.toFastChar() = FastChar(this)
inline fun Char.toFastChar() = this.code.toFastChar()
inline val Char.fast get() = FastChar(code)

inline class FastCharArray internal constructor(val data: IntArray) {
    companion object {
        operator fun invoke(size: Int) = FastCharArray(IntArray(size))
    }

    val size: Int get() = data.size
    operator fun get(index: Int): FastChar = FastChar(data[index])
    operator fun set(index: Int, value: FastChar) { data[index] = value.code and 0xFFFF }
}

expect fun FastCharArray.concatToString(startIndex: Int = 0, endIndex: Int = this.size): String
fun String.toFastCharArray(startIndex: Int, endIndex: Int): FastCharArray {
    val len = endIndex - startIndex
    val out = FastCharArray(len)
    for (n in 0 until len) out[n] = this.getFast(n)
    return out
}

