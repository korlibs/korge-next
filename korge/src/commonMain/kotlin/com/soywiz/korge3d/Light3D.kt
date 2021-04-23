package com.soywiz.korge3d

import com.soywiz.korim.color.*
import com.soywiz.korma.geom.*

@Korge3DExperimental
fun Container3D.light(
	color: RGBA = Colors.WHITE,
	constantAttenuation: Float = 1f,
	linearAttenuation: Float = 0f,
	quadraticAttenuation: Float = 0.00111109f,
	callback: Light3D.() -> Unit = {}
) = Light3D(color, constantAttenuation, linearAttenuation, quadraticAttenuation).addTo(this, callback)

@Korge3DExperimental
open class Light3D(
	var color: RGBA = Colors.WHITE,
	var constantAttenuation: Float = 1f,
	var linearAttenuation: Float = 0f,
	var quadraticAttenuation: Float = 0.00111109f
) : View3D() {
	internal val colorVec = Vector3D()
	internal val attenuationVec = Vector3D()

	fun setTo(
		color: RGBA = Colors.WHITE,
		constantAttenuation: Float = 1f,
		linearAttenuation: Float = 0f,
		quadraticAttenuation: Float = 0.00111109f
	): Light3D {
		this.color = color
		this.constantAttenuation = constantAttenuation
		this.linearAttenuation = linearAttenuation
		this.quadraticAttenuation = quadraticAttenuation
        return this
	}

	override fun render(ctx: RenderContext3D) {
	}
}
