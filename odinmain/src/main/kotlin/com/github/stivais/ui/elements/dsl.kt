package com.github.stivais.ui.elements

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.Measurement
import com.github.stivais.ui.constraints.copyParent
import com.github.stivais.ui.constraints.percent
import com.github.stivais.ui.elements.impl.*
import com.github.stivais.ui.events.onClick
import com.github.stivais.ui.events.onMouseEnterExit
import com.github.stivais.ui.utils.animate
import com.github.stivais.ui.utils.seconds

fun Element.button(
    constraints: Constraints? = null,
    offColor: Color = Color.RGB(38, 38, 38),
    onColor: Color = Color.RGB(50, 150, 220),
    on: Boolean = false,
    radii: FloatArray? = null,
    dsl: Block.() -> Unit
): Block {
    val mainColor = Color.Animated(offColor, onColor, on)
    val hoverColor = Color.Animated(Color.TRANSPARENT, Color.RGB(255, 255, 255, 0.05f))

    return block(constraints, mainColor, radii) {
        block(copyParent(), color = hoverColor, radius = radii) {
            onMouseEnterExit {
                hoverColor.animate(0.25.seconds)
                true
            }
        }
        onClick(0) {
            mainColor.animate(0.15.seconds)
            false
        }
        dsl()
    }
}

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