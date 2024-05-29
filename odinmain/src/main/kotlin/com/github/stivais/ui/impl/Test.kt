package com.github.stivais.ui.impl

import com.github.stivais.ui.UI
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.constraints.size
import com.github.stivais.ui.elements.impl.Button
import com.github.stivais.ui.renderer.Renderer

fun basic(renderer: Renderer) = UI(renderer) {

    Button(
        constraints = size(100.px, 100.px),
        color = Color.BLACK
    ).add()

//    column {
//        repeat(5) {
//            block(
//                constraints = size(100.px, 10.percent),
//                color = Color.RED
//            ) {
//                outline(color = Color.BLACK)
//                text(
//                    text = "${it + 1}"
//                )
//                onClick(0) { _ ->
//                    modMessage("Clicked block $it")
//                    sibling()?.cast<BlockScope>()?.outline(Color.WHITE)
//                    false
//                }
//            }
//        }
//    }
}