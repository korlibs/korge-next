package com.soywiz.korge.debug

import com.soywiz.kmem.convertRange
import com.soywiz.korev.*
import com.soywiz.korio.async.*
import com.soywiz.korio.util.toStringDecimal
import com.soywiz.korte.Template
import com.soywiz.korui.*
import kotlin.math.absoluteValue
import kotlin.math.withSign
import kotlin.reflect.*

class NumberUiEditableValue(
    app: UiApplication,
    override val prop: ObservableProperty<Double>,
    var min: Double = -1.0,
    var max: Double = +1.0,
    var clampMin: Boolean = false,
    var clampMax: Boolean = false,
    var decimalPlaces: Int = 2
) : UiEditableValue(app), ObservablePropertyHolder<Double> {
    var evalContext: () -> Any? = { null }
    val initial = prop.value
    companion object {
        //val MAX_WIDTH = 300
        val MAX_WIDTH = 1000
    }

    init {
        prop.onChange {
            //println("prop.onChange: $it")
            if (current != it) {
                setValue(it, setProperty = false)
            }
        }
    }

    val contentText = UiLabel(app).also { it.text = "" }.also { it.visible = true }
    val contentTextField = UiTextField(app).also { it.text = contentText.text }.also { it.visible = false }
    var current: Double = Double.NaN

    override fun hideEditor() {
        contentText.visible = true
        contentTextField.visible = false
        if (contentTextField.text.isNotEmpty()) {
            val templateResult = runBlockingNoSuspensions { Template("{{ ${contentTextField.text} }}").invoke(evalContext()) }
            setValue(templateResult.toDoubleOrNull() ?: 0.0)
        }
    }

    override fun showEditor() {
        contentTextField.text = contentText.text
        contentText.visible = false
        contentTextField.visible = true
        contentTextField.select()
        contentTextField.focus()
    }

    fun setValue(value: Double, setProperty: Boolean = true) {
        var rvalue = value
        if (clampMin) rvalue = rvalue.coerceAtLeast(min)
        if (clampMax) rvalue = rvalue.coerceAtMost(max)
        val valueStr = rvalue.toStringDecimal(decimalPlaces)
        if (current != rvalue) {
            current = rvalue
            if (setProperty) {
                prop.value = rvalue
            }
            contentText.text = valueStr
            contentTextField.text = valueStr
        }
    }

    init {
        layout = UiFillLayout
        visible = true
        contentText.onClick {
            showEditor()
        }
        contentTextField.onKeyEvent { e ->
            if (e.typeDown && e.key == Key.RETURN) {
                hideEditor()
            }
            //println(e)
        }
        contentTextField.onFocus { e ->
            if (e.typeBlur) {
                hideEditor()
            }
            //println(e)
        }
        var startX = 0
        var startY = 0
        var startValue = current
        contentText.onMouseEvent { e ->
            if (e.typeDown) {
                startX = e.x
                startY = e.y
                startValue = current
                e.requestLock()
            }
            if (e.typeDrag) {
                val dx = (e.x - startX).toDouble()
                val dy = (e.y - startY).toDouble()
                val lenAbs = dx.absoluteValue.convertRange(0.0, MAX_WIDTH.toDouble(), 0.0, max - min)
                val len = lenAbs.withSign(dx)
                setValue(startValue + len)
            }
        }
        setValue(initial)
        contentText.cursor = UiStandardCursor.RESIZE_EAST
        addChild(contentText)
        addChild(contentTextField)
    }
}

fun KMutableProperty0<Double>.toEditableValue(
    app: UiApplication,
    name: String? = null,
    min: Double = -1.0,
    max: Double = +1.0,
    clamp: Boolean = true,
    clampMin: Boolean = clamp,
    clampMax: Boolean = clamp,
    decimalPlaces: Int = 2
): RowUiEditableValue {
    val prop = this
    val obs = ObservableProperty(name ?: this.name, internalSet = { prop.set(it) }, internalGet = {prop.get() })
    val number = NumberUiEditableValue(app, obs, min, max, clampMin, clampMax, decimalPlaces)
    return RowUiEditableValue(app, name ?: this.name, number)
}

fun Pair<KMutableProperty0<Double>, KMutableProperty0<Double>>.toEditableValue(
    app: UiApplication,
    name: String,
    min: Double = -1.0,
    max: Double = +1.0,
    clamp: Boolean = true,
    clampMin: Boolean = clamp,
    clampMax: Boolean = clamp,
    decimalPlaces: Int = 2
): RowUiEditableValue {
    val prop = this
    val obs1 = ObservableProperty(prop.first.name, internalSet = { prop.first.set(it) }, internalGet = { prop.first.get() })
    val obs2 = ObservableProperty(prop.second.name, internalSet = { prop.second.set(it) }, internalGet = { prop.second.get() })
    val number1 = NumberUiEditableValue(app, obs1, min, max, clampMin, clampMax, decimalPlaces)
    val number2 = NumberUiEditableValue(app, obs2, min, max, clampMin, clampMax, decimalPlaces)
    return RowUiEditableValue(app, name, TwoItemUiEditableValue(app, number1, number2))
}