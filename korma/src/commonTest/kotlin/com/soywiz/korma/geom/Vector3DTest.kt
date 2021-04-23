package com.soywiz.korma.geom

import kotlin.test.*

class Vector3DTest {
    @Test
    fun testNormalized() {
        val v = Vector3D(2f, 0f, 0f)
        // Normalized doesn't changes the original vector
        assertEquals(Vector3D(1f, 0f, 0f), v.normalized())
        assertEquals(Vector3D(2f, 0f, 0f), v)
    }

    @Test
    fun testNormalize() {
        val v = Vector3D(2f, 0f, 0f)
        // Normalized changes the original vector and returns its pre-normalized length
        assertEquals(2f, v.normalize())
        assertEquals(Vector3D(1f, 0f, 0f), v)
    }

    @Test
    fun testCrossProduct() {
        val xInt = Vector3D().setToCross(Vector3D(1f, 0f, 0f), Vector3D(0f, 1f, 0f))
        assertEquals(Vector3D(0f, 0f, 1f), xInt)
        val xDouble = Vector3D().setToCross(Vector3D(1f, 0f, 0f), Vector3D(0f, 1f, 0f))
        assertEquals(Vector3D(0f, 0f, 1f), xDouble)
    }

    @Test
    fun testDotProduct() {
        val dot = Vector3D(0.5f, 1f, 0f).dot(Vector3D(3f, 1f, 1f))
        assertEquals(2.5f, dot)
    }

    @Test
    fun testBasicMath() {
        val v = Vector3D(0f,0f,0f)
        v.setToAdd(v, Vector3D(1f,0f,0f))
        assertEquals(Vector3D(1f, 0f, 0f), v)
        v.scale(5f)
        assertEquals(Vector3D(5f, 0f ,0f), v)
        v.setToSub(v, Vector3D(2f, 1f, 0f))
        assertEquals(Vector3D(3f, -1f, 0f), v)
    }

    @Test
    fun testCopy() {
        val original = Vector3D(1.0f, 2.0f, 3.0f)
        val copy = original.copy()
        assertEquals(original.x, copy.x)
        assertEquals(original.y, copy.y)
        assertEquals(original.z, copy.z)
    }

    @Test
    fun testMultiplicationOperatorWithFloat() {
        val vector = Vector3D(1.0f, 2.0f, 3.0f)
        val number = 2.0f

        val result = vector * number
        assertEquals(result.x, vector.x * number)
        assertEquals(result.y, vector.y * number)
        assertEquals(result.z, vector.z * number)
    }

    @Test
    fun testSelfAssignMultiplicationWithOperatorFloat() {
        val vector = Vector3D(1.0f, 2.0f, 3.0f)
        val original = vector.copy()
        val number = 2.0f

        vector *= number
        assertEquals(vector.x, original.x * number)
        assertEquals(vector.y, original.y * number)
        assertEquals(vector.z, original.z * number)
    }

    @Test
    fun testDivisionOperatorWithFloat() {
        val vector = Vector3D(1.0f, 2.0f, 3.0f)
        val number = 2.0f

        val result = vector / number
        assertEquals(result.x, vector.x / number)
        assertEquals(result.y, vector.y / number)
        assertEquals(result.z, vector.z / number)
    }

    @Test
    fun testSelfAssignDivisionOperatorWithFloat() {
        val vector = Vector3D(1.0f, 2.0f, 3.0f)
        val original = vector.copy()
        val number = 2.0f

        vector /= number
        assertEquals(vector.x, original.x / number)
        assertEquals(vector.y, original.y / number)
        assertEquals(vector.z, original.z / number)
    }

    @Test
    fun testAdditionOperatorWithVector() {
        val first = Vector3D(1.0f, 2.0f, 3.0f)
        val second = Vector3D(4.0f, 5.0f, 6.0f)

        val result = first + second
        assertEquals(result.x, first.x + second.x)
        assertEquals(result.y, first.y + second.y)
        assertEquals(result.z, first.z + second.z)
    }

    @Test
    fun testSelfAssignAdditionOperatorWithVector() {
        val first = Vector3D(1.0f, 2.0f, 3.0f)
        val firstOriginal = first.copy()
        val second = Vector3D(4.0f, 5.0f, 6.0f)

        first += second
        assertEquals(first.x, firstOriginal.x + second.x)
        assertEquals(first.y, firstOriginal.y + second.y)
        assertEquals(first.z, firstOriginal.z + second.z)
    }

    @Test
    fun testSubtractionOperatorWithVector() {
        val first = Vector3D(1.0f, 2.0f, 3.0f)
        val second = Vector3D(4.0f, 5.0f, 6.0f)

        val result = first - second
        assertEquals(result.x, first.x - second.x)
        assertEquals(result.y, first.y - second.y)
        assertEquals(result.z, first.z - second.z)
    }

    @Test
    fun testSelfAssignSubtractionOperatorWithVector() {
        val first = Vector3D(1.0f, 2.0f, 3.0f)
        val firstOriginal = first.copy()
        val second = Vector3D(4.0f, 5.0f, 6.0f)

        first -= second
        assertEquals(first.x, firstOriginal.x - second.x)
        assertEquals(first.y, firstOriginal.y - second.y)
        assertEquals(first.z, firstOriginal.z - second.z)
    }
}
