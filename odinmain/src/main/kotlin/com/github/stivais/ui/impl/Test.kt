package com.github.stivais.ui.impl

import com.github.stivais.ui.UI
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.constraints.size
import com.github.stivais.ui.elements.block
import com.github.stivais.ui.elements.column
import com.github.stivais.ui.elements.text
import com.github.stivais.ui.events.onClick
import com.github.stivais.ui.renderer.Renderer
import me.odinmain.utils.skyblock.modMessage

fun basic(renderer: Renderer) = UI(renderer) {
    column {
        repeat(5) {
            block(
                constraints = size(100.px, 100.px),
                color = Color.RGB(255, 0, 0)
            ) {
                text(text = "$it")
                onClick(0) {
                    modMessage("Clicked block $it")
                    true
                }
            }
        }
    }
}