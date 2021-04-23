package com.soywiz.korge3d

import com.soywiz.korge3d.internal.vector3DTemps
import com.soywiz.korma.geom.Matrix3D
import com.soywiz.korma.geom.Vector3D
import com.soywiz.korma.geom.scale

@Korge3DExperimental
fun Container3D.shape3D(width: Float=1f, height: Float=1f, depth: Float=1f, drawCommands: MeshBuilder3D.() -> Unit): Shape3D {
   return  Shape3D(width, height, depth, drawCommands).addTo(this)
}

/*
 * Note: To draw solid quads, you can use [Bitmaps.white] + [AgBitmapTextureManager] as texture and the [colorMul] as quad color.
 */
@Korge3DExperimental
class Shape3D(
    initWidth: Float, initHeight: Float, initDepth: Float,
    drawCommands: MeshBuilder3D.() -> Unit
) : ViewWithMesh3D(createMesh(drawCommands).copy()) {

    var width: Float = initWidth
    var height: Float = initHeight
    var depth: Float = initDepth

    override fun prepareExtraModelMatrix(mat: Matrix3D) {
        mat.identity().scale(width, height, depth)
    }

    companion object {

        fun createMesh(drawCommands: MeshBuilder3D.() -> Unit) = MeshBuilder3D {
            drawCommands()
        }
    }
}


@Korge3DExperimental
inline fun Container3D.cube(width: Int, height: Int, depth: Int, callback: Cube3D.() -> Unit = {}): Cube3D = cube(width.toFloat(), height.toFloat(), depth.toFloat(), callback)

@Korge3DExperimental
inline fun Container3D.cube(
    width: Float = 1f,
    height: Float = width,
    depth: Float = height,
    callback: Cube3D.() -> Unit = {}
): Cube3D = Cube3D(width, height, depth).addTo(this, callback)

@Korge3DExperimental
class Cube3D(var width: Float, var height: Float, var depth: Float) : ViewWithMesh3D(mesh.copy()) {
    override fun prepareExtraModelMatrix(mat: Matrix3D) {
        mat.identity().scale(width, height, depth)
    }

    var material: Material3D?
        get() = mesh.material
        set(value) {
            mesh.material = value
        }

    fun material(material: Material3D?): Cube3D {
        this.material = material
        return this
    }

    companion object {
        val mesh = MeshBuilder3D {
            vector3DTemps {
                fun face(pos: Vector3D) {
                    val dims = (0 until 3).filter { pos[it] == 0f }
                    val normal = Vector3D { if (pos[it] != 0f) 1f else 0f }
                    val dirs = Array(2) { dim ->
                        Vector3D { if (it == dims[dim]) .5f else 0f }
                    }
                    val dx = dirs[0]
                    val dy = dirs[1]

                    val i0 = addVertex(pos - dx - dy, normal, Vector3D(0f, 0f, 0f))
                    val i1 = addVertex(pos + dx - dy, normal, Vector3D(1f, 0f, 0f))
                    val i2 = addVertex(pos - dx + dy, normal, Vector3D(0f, 1f, 0f))

                    val i3 = addVertex(pos - dx + dy, normal, Vector3D(0f, 1f, 0f))
                    val i4 = addVertex(pos + dx - dy, normal, Vector3D(1f, 0f, 0f))
                    val i5 = addVertex(pos + dx + dy, normal, Vector3D(1f, 1f, 0f))

                    addIndices(i0, i1, i2, i3, i4, i5)
                }

                face(Vector3D(0f, +.5f, 0f))
                face(Vector3D(0f, -.5f, 0f))

                face(Vector3D(+.5f, 0f, 0f))
                face(Vector3D(-.5f, 0f, 0f))

                face(Vector3D(0f, 0f, +.5f))
                face(Vector3D(0f, 0f, -.5f))
            }
        }
    }
}
