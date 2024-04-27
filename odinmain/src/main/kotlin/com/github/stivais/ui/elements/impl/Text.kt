package com.github.stivais.ui.elements.impl

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.Measurement
import com.github.stivais.ui.constraints.measurements.Pixel
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.utils.replaceUndefined
import me.odinmain.font.OdinFont
import kotlin.reflect.KProperty

class Text(
    text: String,
    textColor: Color,
    constraints: Constraints?,
    val size: Measurement
) : Element(constraints.replaceUndefined(w = 0.px, h = size)) {

    var text: String = text
        set(value) {
            if (field == value) return
            field = value
            (constraints.width as Pixel).pixels = OdinFont.getTextWidth(text, height)//renderer.textWidth(value, height)

        }

    init {
        this.color = textColor

        onInitialization {
            // needs size to be set
            parent?.position()
            val width = OdinFont.getTextWidth(text, height)
//            val width = renderer.textWidth(text, height)
            (this.constraints.width as Pixel).pixels = width
            this.width = width
        }
    }

    override fun draw() {
        OdinFont.text(text, x, y, me.odinmain.utils.render.Color(color!!.rgba), height)
//        renderer.text(text, x, y, color!!.rgba, height)
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return text
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        text = value
    }
}