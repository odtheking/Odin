package com.github.stivais.ui.impl

import com.github.stivais.ui.UI
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.constrain
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.constraints.size
import com.github.stivais.ui.elements.scope.BlockScope
import com.github.stivais.ui.renderer.Renderer
import com.github.stivais.ui.utils.draggable
import me.odinmain.utils.skyblock.modMessage

fun basic(renderer: Renderer) = UI(renderer) {
    column {
        repeat(5) {
            block(
                constraints = size(100.px, 100.px),
                color = Color.RED
            ) {
                outline(color = Color.BLACK)
                text(
                    text = "${it + 1}"
                )
                onClick(0) { _ ->
                    modMessage("Clicked block $it")
                    sibling()?.cast<BlockScope>()?.outline(Color.WHITE)
                    false
                }
                draggable()
            }
        }
        block(constrain(0.px, 0.px, 50.px, 50.px), Color.RED)
    }
}