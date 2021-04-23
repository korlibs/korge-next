package com.soywiz.korma.geom

import kotlin.math.*

inline fun quaternionLength(x: Float, y: Float, z: Float, w: Float) = sqrt(quaternionLengthSquared(x, y, z, w))
inline fun quaternionLengthSquared(x: Float, y: Float, z: Float, w: Float) = (x * x) + (y * y) + (z * z) + (w * w)

// https://en.wikipedia.org/wiki/Conversion_between_quaternions_and_Euler_angles
data class Quaternion(
    var x: Float = 0f,
    var y: Float = 0f,
    var z: Float = 0f,
    var w: Float = 1f
) {
    companion object {
        fun dotProduct(l: Quaternion, r: Quaternion): Float = l.x * r.x + l.y * r.y + l.z * r.z + l.w * r.w

        fun toEuler(q: Quaternion, out: EulerRotation = EulerRotation()): EulerRotation = toEuler(q.x, q.y, q.z, q.w, out)

        fun toEuler(x: Float, y: Float, z: Float, w: Float, out: EulerRotation = EulerRotation()): EulerRotation {
            val sinrCosp = +2f * (w * x + y * z)
            val cosrCosp = +1f - 2f * (x * x + y * y)
            val roll = atan2(sinrCosp, cosrCosp)
            val sinp = +2f * (w * y - z * x)
            val pitch = when {
                abs(sinp) >= 1f -> if (sinp > 0f) PI.toFloat() / 2f else -PI.toFloat() / 2f
                else -> asin(sinp)
            }
            val sinyCosp = +2f * (w * z + x * y)
            val cosyCosp = +1f - 2f * (y * y + z * z)
            val yaw = atan2(sinyCosp, cosyCosp)
            out.setTo(roll.radians, pitch.radians, yaw.radians)
            return out
        }
    }

    fun copy() = Quaternion(x, y, z, w)

    inline val length: Float get() = sqrt(quaternionLengthSquared(x, y, z, w))
    inline val lengthSqr: Float get() = quaternionLengthSquared(x, y, z, w)

    operator fun get(index: Int): Float = when (index) {
        0 -> x
        1 -> y
        2 -> z
        3 -> w
        else -> Float.NaN
    }
    inline fun setToFunc(callback: (Int) -> Float) = setTo(callback(0), callback(1), callback(2), callback(3))

    fun setTo(x: Float, y: Float, z: Float, w: Float): Quaternion {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
        return this
    }

    fun setTo(euler: EulerRotation) = setTo(EulerRotation.toQuaternion(euler, this))
    fun setTo(other: Quaternion) = setTo(other.x, other.y, other.z, other.w)

    fun setEuler(x: Angle, y: Angle, z: Angle): Quaternion = EulerRotation.toQuaternion(x, y, z, this)
    fun setEuler(euler: EulerRotation): Quaternion = EulerRotation.toQuaternion(euler, this)

    operator fun unaryMinus(): Quaternion = Quaternion(-x, -y, -z, -w)
    operator fun plus(other: Quaternion): Quaternion = Quaternion(x + other.x, y + other.y, z + other.z, w + other.w)
    operator fun minus(other: Quaternion): Quaternion = Quaternion(x - other.x, y - other.y, z - other.z, w - other.w)
    operator fun times(scale: Float): Quaternion = Quaternion(x * scale, y * scale, z * scale, w * scale)

    fun negate() = this.setTo(-x, -y, -z, -w)

    inline fun setToFunc(l: Quaternion, r: Quaternion, func: (l: Float, r: Float) -> Float) = setTo(
        func(l.x, r.x),
        func(l.y, r.y),
        func(l.z, r.z),
        func(l.w, r.w)
    )

    fun setToSlerp(left: Quaternion, right: Quaternion, t: Float, tleft: Quaternion = Quaternion(), tright: Quaternion = Quaternion()) {
        tleft.setTo(left)
        tleft.normalize()
        tright.setTo(right)
        tright.normalize()

        var dot = dotProduct(tleft, right)

        if (dot < 0.0f) {
            tright.negate()
            dot = -dot
        }

        if (dot > 0.99995f) {
            setToFunc(tleft, tright) { l, r -> l + t * (r - l) }
            return
        } else {
            val angle0 = acos(dot)
            val angle1 = angle0 * t

            val s1 = sin(angle1) / sin(angle0)
            val s0 = cos(angle1) - dot * s1

            setToFunc(tleft, tright) { l, r -> (s0 * l) + (s1 * r) }
        }
    }

    fun setToNlerp(left: Quaternion, right: Quaternion, t: Float): Quaternion {
        val sign = if (dotProduct(left, right) < 0f) -1f else +1f
        setToFunc { (1f - t) * left[it] + t * right[it] * sign }
        normalize()
        return this
    }

    fun setToInterpolated(left: Quaternion, right: Quaternion, t: Float) = setToSlerp(left, right, t)

    fun setFromRotationMatrix(m: Matrix3D): Unit = let { q ->
        m.apply {
            val t = v00 + v11 + v22
            when {
                t > 0 -> {
                    val s = 0.5f / sqrt(t + 1f)
                    q.setTo(((v21 - v12) * s), ((v02 - v20) * s), ((v10 - v01) * s), (0.25f / s))
                }
                v00 > v11 && v00 > v22 -> {
                    val s = 2f * sqrt(1f + v00 - v11 - v22)
                    q.setTo((0.25f * s), ((v01 + v10) / s), ((v02 + v20) / s), ((v21 - v12) / s))
                }
                v11 > v22 -> {
                    val s = 2f * sqrt(1f + v11 - v00 - v22)
                    q.setTo(((v01 + v10) / s), (0.25f * s), ((v12 + v21) / s), ((v02 - v20) / s))
                }
                else -> {
                    val s = 2f * sqrt(1f + v22 - v00 - v11)
                    q.setTo(((v02 + v20) / s), ((v12 + v21) / s), (0.25f * s), ((v10 - v01) / s))
                }
            }
        }
    }

    fun normalize(v: Quaternion = this): Float {
        val originalLength = length
        setTo(v.x / originalLength, v.y / originalLength, v.z / originalLength, v.w / originalLength)
        return originalLength
    }

    /**
     * Normalize, except when the length is 0
     */
    fun normalizeSafely(v: Quaternion = this): Float {
        val originalLength = length
        if (originalLength != 0f) {
            setTo(v.x / originalLength, v.y / originalLength, v.z / originalLength, v.w / originalLength)
        }
        return originalLength
    }

    fun toMatrix(): Matrix3D = multiplyMatrix3D(
        // Left
        w, z, -y, x,
        -z, w, x, y,
        y, -x, w, z,
        -x, -y, -z, w,
        // Right
        w, z, -y, -x,
        -z, w, x, -y,
        y, -x, w, -z,
        x, y, z, w,
    )
}

operator fun Float.times(scale: Quaternion): Quaternion = scale.times(this)
