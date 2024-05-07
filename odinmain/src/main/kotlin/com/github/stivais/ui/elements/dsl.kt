package com.github.stivais.ui.elements

import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.constraints.sizes.Copying
import com.github.stivais.ui.elements.impl.*
import com.github.stivais.ui.events.onClick
import com.github.stivais.ui.events.onMouseMove
import com.github.stivais.ui.events.onRelease
import com.github.stivais.ui.renderer.GradientDirection
import com.github.stivais.ui.utils.animate
import com.github.stivais.ui.utils.hoverEffect
import com.github.stivais.ui.utils.radii
import com.github.stivais.ui.utils.seconds

inline fun Element.column(constraints: Constraints? = null, block: Column.() -> Unit = {}): Column {
    val column = Column(constraints)
    addElement(column)
    column.block()
    return column
}

inline fun Element.block(
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

inline fun Element.block(
    constraints: Constraints? = null,
    color1: Color,
    color2: Color,
    direction: GradientDirection,
    radius: Float = 0f,
    block: Block.() -> Unit = {}
): GradientBlock {
    val block = GradientBlock(constraints, color1, color2, radius, direction)
//        if (radius != null) RoundedBlock(constraints, color, color2, radius) else Block(constraints, color)
    addElement(block)
    block.block()
    return block
}

inline fun Element.block(
    constraints: Constraints? = null,
    colors: Pair<Color, Color>,
    direction: GradientDirection,
    radius: Float = 0f,
    crossinline block: Block.() -> Unit = {}
) = block(constraints, colors.first, colors.second, direction, radius, block)

inline fun Element.text(
    text: String,
    pos: Constraints? = null,
    size: Measurement = 50.percent,
    color: Color = Color.WHITE,
    block: Text.() -> Unit = {}
): Text {
    val text = Text(text, color, pos, size)
    addElement(text)
    text.block()
    return text
}

inline fun Element.group(constraints: Constraints? = null, block: Group.() -> Unit = {}): Group {
    val column = Group(constraints)
    addElement(column)
    column.block()
    return column
}

inline fun Element.button(
    constraints: Constraints? = null,
    color: Color.Animated,
    on: Boolean = false,
    radii: FloatArray? = null,
    crossinline dsl: Block.() -> Unit = {}
): Block {
    if (on) color.swap()
    return block(
        constraints = constraints,
        color = color,
        radius = radii
    ) {
        onClick {
            color.animate(0.15.seconds)
            false
        }
        hoverEffect(0.25.seconds)
        dsl()
    }
}

inline fun Element.button(
    constraints: Constraints? = null,
    offColor: Color,
    onColor: Color,
    on: Boolean = false,
    radii: FloatArray? = null,
    crossinline dsl: Block.() -> Unit = {}
): Block = button(constraints, Color.Animated(offColor, onColor), on, radii, dsl)

inline fun Element.slider(
    constraints: Constraints?,
    color: Color,
    value: Double,
    min: Double,
    max: Double,
    crossinline onChange: (percent: Float) -> Unit
): Block {
    var dragging = false
    val sliderPosition = Animatable.Raw(0f)

    return block(constraints, Color.RGB(26, 26, 26), radii(3)) {
        block(
            constraints = constrain(0.px, 0.px, sliderPosition, Copying),
            color = color,
            radius = radii(all = 4)
        )
        onClick {
            val pos = (ui.mx - x).coerceIn(0f, width)
            sliderPosition.animate(to = pos, 0.75.seconds, Animations.EaseOutQuint)
            onChange(pos / width)
            dragging = true
            true
        }
        onMouseMove {
            if (dragging) {
                val pos = (ui.mx - x).coerceIn(0f, width)
                sliderPosition.to(pos)
                update()
                onChange(pos / width)
            }
            true
        }
        onRelease(0) {
            dragging = false
        }
        afterInitialization {
            sliderPosition.to(((value - min) / (max - min) * width).toFloat())
        }
        hoverEffect(0.25.seconds)
    }
}
