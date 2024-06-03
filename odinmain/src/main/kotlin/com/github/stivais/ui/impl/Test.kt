package com.github.stivais.ui.impl

import com.github.stivais.ui.UI
import com.github.stivais.ui.constraints.constrain
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.elements.impl.TextInput
import com.github.stivais.ui.renderer.Renderer

fun basic(renderer: Renderer) = UI(renderer) {

    TextInput(
        "HELLLO",
        constraints = constrain(920.px, 540.px, 200.px, 30.px)
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