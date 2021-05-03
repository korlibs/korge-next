package com.soywiz.kds

import kotlin.js.*

@Suppress("UNCHECKED_CAST")
actual inline fun <T> Any?.fastCastTo(): T = this as T

//actual typealias FastArrayList<E> = ArrayList<E>

actual fun FastCharArray.concatToString(startIndex: Int, endIndex: Int): String {
    val len = endIndex - startIndex
    val sb = StringBuilder(len)
    for (n in 0 until len) sb.append(this.data[startIndex + n].toChar())
    return sb.toString()
}

actual fun String.getFast(index: Int): FastChar {
    return this[index].code.toFastChar()
}
