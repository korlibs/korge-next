package com.soywiz.korma.geom

import kotlin.jvm.*

fun Matrix3D.copyFrom(that: Matrix): Matrix3D = that.toMatrix3D(this)

fun Matrix.toMatrix3D(out: Matrix3D = Matrix3D()): Matrix3D = out.setRows(
    a.toFloat(), c.toFloat(), 0f, tx.toFloat(),
    b.toFloat(), d.toFloat(), 0f, ty.toFloat(),
    0f, 0f, 1f, 0f,
    0f, 0f, 0f, 1f
)

@JvmName("multiplyNullable")
fun Matrix.multiply(l: Matrix?, r: Matrix?): Matrix {
    when {
        l != null && r != null -> multiply(l, r)
        l != null -> copyFrom(l)
        r != null -> copyFrom(r)
        else -> identity()
    }
    return this
}
