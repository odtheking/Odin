package com.github.stivais.ui.elements

import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.color.brighter
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.constraints.sizes.Copying
import com.github.stivais.ui.elements.impl.*
import com.github.stivais.ui.events.onClick
import com.github.stivais.ui.events.onMouseEnterExit
import com.github.stivais.ui.events.onMouseMove
import com.github.stivais.ui.events.onRelease
import com.github.stivais.ui.utils.animate
import com.github.stivais.ui.utils.radii
import com.github.stivais.ui.utils.seconds

fun Element.column(constraints: Constraints? = null, block: Column.() -> Unit = {}): Column {
    val column = Column(constraints)
    addElement(column)
    column.block()
    return column
}

// todo: improve outline color
fun Element.block(
    constraints: Constraints? = null,
    color: Color,
    radius: FloatArray? = null,
    block: Block.() -> Unit = {}
): Block {
    val block = if (radius != null) RoundedBlock(constraints, color, radius) else Block(constraints, color)
    addElement(block)
    block.block()
    return block
}

fun Element.text(
    text: String,
    at: Constraints? = null,
    size: Measurement = 50.percent,
    color: Color = Color.WHITE,
    block: Text.() -> Unit = {}
): Text {
    val text = Text(text, color, at, size)
    addElement(text)
    text.block()
    return text
}

fun Element.group(constraints: Constraints? = null, block: Group.() -> Unit = {}): Group {
    val column = Group(constraints)
    addElement(column)
    column.block()
    return column
}

fun Element.button(
    constraints: Constraints? = null,
    offColor: Color,
    onColor: Color,
    on: Boolean = false,
    radii: FloatArray? = null,
    dsl: Block.() -> Unit = {}
): Block {
    val buttonColor = Color.Animated(offColor, onColor, on)
    val hoverColor = Color.Animated(buttonColor, Color { buttonColor.rgba.brighter() })
    return block(
        constraints = constraints,
        color = hoverColor,
        radius = radii
    ) {
        onMouseEnterExit {
            hoverColor.animate(0.25.seconds)
            true
        }
        onClick(0) {
            buttonColor.animate(0.15.seconds)
            false
        }
        dsl()
    }
}

fun Element.slider(
    constraints: Constraints?,
    value: Double,
    min: Double,
    max: Double,
    onChange: (percent: Float) -> Unit
): Block {
    var dragging = false
    return block(constraints, Color.RGB(-0xefeff0), radii(3)) {
        val sliderAnim = Animatable.Raw(0f)
        // color is temp i cba to put in params yet
        val color = Color.Animated(Color.RGB(50, 150, 220), Color.RGB(75, 175, 245))
        afterInitialization {
            sliderAnim.to(((value - min) / (max - min) * width).toFloat())
        }
        block(constrain(0.px, 0.px, sliderAnim, Copying), color = color, radii(all = 3f))

        onClick(0) {
            val pos = (ui.eventManager!!.mouseX - x).coerceIn(0f, width)
            sliderAnim.animate(to = pos, 0.75.seconds, Animations.EaseOutQuint)
            onChange(pos / width)
            dragging = true
            true
        }
        onMouseMove {
            if (dragging) {
                val pos = (ui.eventManager!!.mouseX - x).coerceIn(0f, width)
                sliderAnim.to(pos)
                onChange(pos / width)
            }
            true
        }
        onRelease(0) {
            dragging = false
        }
        onMouseEnterExit {
            color.animate(0.25.seconds)
            true
        }
    }
}
