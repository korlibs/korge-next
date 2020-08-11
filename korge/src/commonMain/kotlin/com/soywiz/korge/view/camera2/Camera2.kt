package com.soywiz.korge.view.camera2

import com.soywiz.klock.hr.*
import com.soywiz.korge.view.*
import com.soywiz.korio.async.*
import com.soywiz.korma.geom.*
import com.soywiz.korma.geom.Point
import com.soywiz.korma.geom.interpolate
import com.soywiz.korma.interpolation.*
import kotlin.math.*

inline fun Container.cameraContainer2(
    width: Double,
    height: Double,
    clip: Boolean = true,
    noinline block: @ViewDslMarker CameraContainer2.() -> Unit = {},
    content: @ViewDslMarker Container.() -> Unit = {}
) = CameraContainer2(width, height, clip, block).addTo(this).also { content(it.content) }

class CameraContainer2(
    width: Double = 100.0,
    height: Double = 100.0,
    clip: Boolean = true,
    block: @ViewDslMarker CameraContainer2.() -> Unit = {}
) : FixedSizeContainer(width, height, clip), View.Reference {

    private val contentContainer = Container()

    val content: Container = object : Container(), Reference {
        override fun getLocalBoundsInternal(out: Rectangle) {
            out.setTo(0, 0, this@CameraContainer2.width, this@CameraContainer2.height)
        }
    }

    private val sourceCamera = Camera2(x = width / 2.0, y = height / 2.0, anchorX = 0.5, anchorY = 0.5)
    private val currentCamera = sourceCamera.copy()
    private val targetCamera = sourceCamera.copy()

    override var width: Double = width
        set(value) {
            field = value
            sync()
        }
    override var height: Double = height
        set(value) {
            field = value
            sync()
        }

    var cameraX: Double
        set(value) {
            currentCamera.x = value
            manualSet()
        }
        get() = currentCamera.x
    var cameraY: Double
        set(value) {
            currentCamera.y = value
            manualSet()
        }
        get() = currentCamera.y
    var cameraZoom: Double
        set(value) {
            currentCamera.zoom = value
            manualSet()
        }
        get() = currentCamera.zoom
    var cameraAngle: Angle
        set(value) {
            currentCamera.angle = value
            manualSet()
        }
        get() = currentCamera.angle
    var cameraAnchorX: Double
        set(value) {
            currentCamera.anchorX = value
            manualSet()
        }
        get() = currentCamera.anchorX
    var cameraAnchorY: Double
        set(value) {
            currentCamera.anchorY = value
            manualSet()
        }
        get() = currentCamera.anchorY

    private fun manualSet() {
        elapsedTime = transitionTime
        sync()
    }

    val onCompletedTransition = Signal<Unit>()

    fun getCurrentCamera(out: Camera2 = Camera2()): Camera2 = out.copyFrom(currentCamera)
    fun getDefaultCamera(out: Camera2 = Camera2()): Camera2 = out.setTo(x = width / 2.0, y = height / 2.0, anchorX = 0.5, anchorY = 0.5)

    fun getCameraRect(rect: Rectangle, scaleMode: ScaleMode = ScaleMode.SHOW_ALL, out: Camera2 = Camera2()): Camera2 {
        val size = Rectangle(0.0, 0.0, width, height).place(rect.size, Anchor.TOP_LEFT, scale = scaleMode).size
        val scaleX = size.width / rect.width
        val scaleY = size.height / rect.height
        return out.setTo(
            rect.x + rect.width * cameraAnchorX,
            rect.y + rect.height * cameraAnchorY,
            zoom = min(scaleX, scaleY),
            angle = 0.degrees,
            anchorX = cameraAnchorX,
            anchorY = cameraAnchorY
        )
    }

    fun getCameraToFit(rect: Rectangle, out: Camera2 = Camera2()): Camera2 = getCameraRect(rect, ScaleMode.SHOW_ALL, out)
    fun getCameraToCover(rect: Rectangle, out: Camera2 = Camera2()): Camera2 = getCameraRect(rect, ScaleMode.COVER, out)

    private var transitionTime = 1.hrSeconds
    private var elapsedTime = 0.hrSeconds
    //var easing = Easing.EASE_OUT
    private var easing = Easing.LINEAR

    private var following: View? = null

    fun follow(view: View?, setImmediately: Boolean = false) {
        following = view
        if (setImmediately) {
            val point = getFollowingXY(tempPoint)
            cameraX = point.x
            cameraY = point.y
            sourceCamera.x = cameraX
            sourceCamera.y = cameraY
        }
    }

    fun unfollow() {
        following = null
    }

    fun updateCamera(block: Camera2.() -> Unit) {
        block(currentCamera)
    }

    fun setCurrentCamera(camera: Camera2) {
        elapsedTime = transitionTime
        following = null
        sourceCamera.copyFrom(camera)
        currentCamera.copyFrom(camera)
        targetCamera.copyFrom(camera)
        sync()
    }

    fun setTargetCamera(camera: Camera2, time: HRTimeSpan = 1.hrSeconds, easing: Easing = Easing.LINEAR) {
        elapsedTime = 0.hrSeconds
        this.transitionTime = time
        this.easing = easing
        following = null
        sourceCamera.copyFrom(currentCamera)
        targetCamera.copyFrom(camera)
    }

    suspend fun tweenCamera(camera: Camera2, time: HRTimeSpan = 1.hrSeconds, easing: Easing = Easing.LINEAR) {
        setTargetCamera(camera, time, easing)
        onCompletedTransition.waitOne()
    }

    fun getFollowingXY(out: Point = Point()): Point {
        val followGlobalX = following!!.globalX
        val followGlobalY = following!!.globalY
        val localToContentX = content!!.globalToLocalX(followGlobalX, followGlobalY)
        val localToContentY = content!!.globalToLocalY(followGlobalX, followGlobalY)
        return out.setTo(localToContentX, localToContentY)
    }

    private val tempPoint = Point()
    init {
        block(this)
        contentContainer.addTo(this)
        content.addTo(contentContainer)
        addHrUpdater {
            when {
                following != null -> {
                    val point = getFollowingXY(tempPoint)
                    cameraX = 0.1.interpolate(currentCamera.x, point.x)
                    cameraY = 0.1.interpolate(currentCamera.y, point.y)
                    sourceCamera.x = cameraX
                    sourceCamera.y = cameraY
                    //cameraX = 0.0
                    //cameraY = 0.0
                    //println("$cameraX, $cameraY - ${following?.x}, ${following?.y}")
                    sync()
                }
                elapsedTime < transitionTime -> {
                    elapsedTime += it
                    val ratio = (elapsedTime / transitionTime).coerceIn(0.0, 1.0)
                    currentCamera.setToInterpolated(easing(ratio), sourceCamera, targetCamera)
                    /*
                    val ratioCamera = easing(ratio)
                    val ratioZoom = easing(ratio)
                    currentCamera.zoom = ratioZoom.interpolate(sourceCamera.zoom, targetCamera.zoom)
                    currentCamera.x = ratioCamera.interpolate(sourceCamera.x, targetCamera.x)
                    currentCamera.y = ratioCamera.interpolate(sourceCamera.y, targetCamera.y)
                    currentCamera.angle = ratioCamera.interpolate(sourceCamera.angle, targetCamera.angle)
                    currentCamera.anchorX = ratioCamera.interpolate(sourceCamera.anchorX, targetCamera.anchorX)
                    currentCamera.anchorY = ratioCamera.interpolate(sourceCamera.anchorY, targetCamera.anchorY)
                     */
                    sync()
                    if (ratio >= 1.0) {
                        onCompletedTransition()
                    }
                }
            }
        }
    }

    fun sync() {
        //val realScaleX = (content.unscaledWidth / width) * cameraZoom
        //val realScaleY = (content.unscaledHeight / height) * cameraZoom
        val realScaleX = cameraZoom
        val realScaleY = cameraZoom

        val contentContainerX = width * cameraAnchorX
        val contentContainerY = height * cameraAnchorY

        content.x = -cameraX
        content.y = -cameraY
        contentContainer.x = contentContainerX
        contentContainer.y = contentContainerY
        contentContainer.rotation = cameraAngle
        contentContainer.scaleX = realScaleX
        contentContainer.scaleY = realScaleY
    }

    fun setZoomAt(anchor: Point, zoom: Double) {
        setAnchorPosKeepingPos(anchor.x, anchor.y)
        cameraZoom = zoom
    }

    fun setZoomAt(anchorX: Double, anchorY: Double, zoom: Double) {
        setAnchorPosKeepingPos(anchorX, anchorY)
        cameraZoom = zoom
    }

    fun setAnchorPosKeepingPos(anchor: Point) {
        setAnchorPosKeepingPos(anchor.x, anchor.y)
    }

    fun setAnchorPosKeepingPos(anchorX: Double, anchorY: Double) {
        setAnchorRatioKeepingPos(anchorX / width, anchorY / height)
    }
    fun setAnchorRatioKeepingPos(ratioX: Double, ratioY: Double) {
        currentCamera.setAnchorRatioKeepingPos(ratioX, ratioY, width, height)
        sync()
    }
}

data class Camera2(
    var x: Double = 0.0,
    var y: Double = 0.0,
    var zoom: Double = 1.0,
    var angle: Angle = 0.degrees,
    var anchorX: Double = 0.5,
    var anchorY: Double = 0.5
) : MutableInterpolable<Camera2> {
    fun setTo(
        x: Double = 0.0,
        y: Double = 0.0,
        zoom: Double = 1.0,
        angle: Angle = 0.degrees,
        anchorX: Double = 0.5,
        anchorY: Double = 0.5
    ): Camera2 = this.apply {
        this.x = x
        this.y = y
        this.zoom = zoom
        this.angle = angle
        this.anchorX = anchorX
        this.anchorY = anchorY
    }

    fun setAnchorRatioKeepingPos(anchorX: Double, anchorY: Double, width: Double, height: Double) {
        val sx = width / zoom
        val sy = height / zoom
        val oldPaX = this.anchorX * sx
        val oldPaY = this.anchorY * sy
        val newPaX = anchorX * sx
        val newPaY = anchorY * sy
        this.x += newPaX - oldPaX
        this.y += newPaY - oldPaY
        this.anchorX = anchorX
        this.anchorY = anchorY
        //println("ANCHOR: $anchorX, $anchorY")
    }

    fun copyFrom(source: Camera2) = source.apply { this@Camera2.setTo(x, y, zoom, angle, anchorX, anchorY) }

    // @TODO: Easing must be adjusted from the zoom change
    // @TODO: This is not exact. We have to preserve final pixel-level speed while changing the zoom
    fun posEasing(zoomLeft: Double, zoomRight: Double, lx: Double, rx: Double, it: Double): Double {
        val zoomChange = zoomRight - zoomLeft
        return if (zoomChange <= 0.0) {
            it.pow(sqrt(-zoomChange).toInt().toDouble())
        } else {
            val inv = it - 1.0
            inv.pow(sqrt(zoomChange).toInt().toDouble()) + 1
        }
    }

    override fun setToInterpolated(ratio: Double, l: Camera2, r: Camera2): Camera2 {
        // Adjust based on the zoom changes
        val posRatio = posEasing(l.zoom, r.zoom, l.x, r.x, ratio)

        return setTo(
            posRatio.interpolate(l.x, r.x),
            posRatio.interpolate(l.y, r.y),
            ratio.interpolate(l.zoom, r.zoom),
            ratio.interpolate(l.angle, r.angle), // @TODO: Fix KorMA angle interpolator
            ratio.interpolate(l.anchorX, r.anchorX),
            ratio.interpolate(l.anchorY, r.anchorY)
        )
    }
}
