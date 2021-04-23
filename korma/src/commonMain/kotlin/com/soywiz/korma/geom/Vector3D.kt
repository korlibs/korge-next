package com.soywiz.korma.geom

import com.soywiz.korma.internal.*
import com.soywiz.korma.interpolation.interpolate
import com.soywiz.korma.math.*
import kotlin.math.*

inline fun vectorLength(x: Float, y: Float, z: Float) = sqrt(vectorLengthSquared(x, y, z))
inline fun vectorLengthSquared(x: Float, y: Float, z: Float) = (x * x) + (y * y) + (z * z)

class Vector3D constructor(
    val data: FloatArray
) {
    var x: Float get() = data[0]; set(value) = run { data[0] = value }
    var y: Float get() = data[1]; set(value) = run { data[1] = value }
    var z: Float get() = data[2]; set(value) = run { data[2] = value }

    val lengthSquared: Float get() = (x * x) + (y * y) + (z * z)
    val length: Float get() = sqrt(lengthSquared)

    constructor(x: Float, y: Float, z: Float): this(floatArrayOf(x, y, z))
    constructor(func: (index: Int) -> Float): this(func(0), func(1), func(2))
    constructor(): this(0.0f, 0.0f, 0.0f)

    fun copyInto(other: Vector3D): Vector3D {
        data.copyInto(other.data)
        return other
    }

    fun copy() = Vector3D(x, y, z)

    inline fun setTo(func: (index: Int) -> Float): Vector3D = setTo(func(0), func(1), func(2))

    fun setTo(x: Float, y: Float, z: Float): Vector3D {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    inline fun setTo(l: Vector3D, r: Vector3D, func: (l: Float, r: Float) -> Float) = setTo(
        func(l.x, r.x),
        func(l.y, r.y),
        func(l.z, r.z)
    )

    fun setToInterpolated(left: Vector3D, right: Vector3D, t: Float) = setTo(
        t.interpolate(left.x, right.x),
        t.interpolate(left.x, right.x),
        t.interpolate(left.x, right.x)
    )

    fun scale(scale: Float) = setTo(x * scale, y * scale, z * scale)

    fun transform(mat: Matrix3D) = mat.transform(this, this)

    /**
     * Normalizes this object and returns the length before normalization.
     */
    fun normalize(): Float {
        val thisLength = length
        this /= thisLength
        return thisLength
    }


    /**
     * Returns a normalized copy of this instance.
     */
    fun normalized(): Vector3D {
        return this / length
    }

    /**
     * Normalizes this object and returns the length before normalization.
     * If the vector length is 0, the vector is set to (0, 0, 1.0f).
     */
    fun normalizeSafely(): Float {
        val thisLength = length
        if (thisLength != 0.0f) {
            this *= (1.0f / thisLength)
        } else {
            z = 1.0f
        }
        return thisLength
    }

    fun dot(v2: Vector3D): Float = (x * v2.x) + (y * v2.y) + (z * v2.y)

    fun setToSub(l: Vector3D, r: Vector3D) = setTo(l.x - r.x, l.y - r.y, l.z - r.z)

    fun setToAdd(l: Vector3D, r: Vector3D) = setTo(l.x + r.x, l.y + r.y, l.z + r.z)

    fun setToCross(l: Vector3D, r: Vector3D) = setTo(
        (l.y * r.z - l.z * r.y),
        (l.z * r.x - l.x * r.z),
        (l.x * r.y - l.y * r.x)
    )

    // region data class / JVM

    override fun equals(other: Any?): Boolean = (other is Vector3D) && almostEquals(this.x, other.x) && almostEquals(this.y, other.y) && almostEquals(this.z, other.z)
    override fun hashCode(): Int = data.contentHashCode()
    override fun toString(): String = "(${x.niceStr}, ${y.niceStr}, ${z.niceStr})"

    // endregion

    // region Operator overloading

    /**
     * Get data value by index (only 0, 1, and 2 are valid indices)
     */
    operator fun get(index: Int): Float = data[index]

    /**
     * Set data value by index (only 0, 1, and 2 are valid indices)
     */
    operator fun set(index: Int, value: Float) = run { data[index] = value }

    operator fun minus(other: Vector3D): Vector3D = Vector3D(x - other.x, y - other.y, z - other.z)

    operator fun minusAssign(other: Vector3D) {
        x -= other.x
        y -= other.y
        z -= other.z
    }

    operator fun plus(other: Vector3D): Vector3D = Vector3D(x + other.x, y + other.y, z + other.z)

    operator fun plusAssign(other: Vector3D) {
        x += other.x
        y += other.y
        z += other.z
    }

    /**
     * Dot product.
     */
    operator fun times(other: Float): Vector3D = Vector3D(x * other, y * other, z * other)

    /**
     * Dot product.
     */
    operator fun timesAssign(other: Float) {
        x *= other
        y *= other
        z *= other
    }

    /**
     * Cross product
     */
    operator fun times(other: Vector3D): Vector3D = Vector3D(
        (y * other.z - z * other.y),
        (z * other.x - x * other.z),
        (x * other.y - y * other.x)
    )

    /**
     * Cross product
     */
    operator fun timesAssign(other: Vector3D) {
        x = (y * other.z - z * other.y)
        y = (z * other.x - x * other.z)
        z = (x * other.y - y * other.x)
    }

    /**
     * Inverse dot product.
     */
    operator fun div(other: Float): Vector3D = Vector3D(x / other, y / other, z / other)

    /**
     * Inverse dot product.
     */
    operator fun divAssign(other: Float) {
        x /= other
        y /= other
        z /= other
    }

    // endregion
}

inline class IntVector3(val v: Vector3D) {
    val x: Int get() = v.x.toInt()
    val y: Int get() = v.y.toInt()
    val z: Int get() = v.z.toInt()
}

fun Vector3D.toIntvector() = IntVector3(this)

typealias Position3D = Vector3D
typealias Scale3D = Vector3D
