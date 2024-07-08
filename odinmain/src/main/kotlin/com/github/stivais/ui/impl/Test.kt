package com.github.stivais.ui.impl

import com.github.stivais.ui.UI
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.constrain
import com.github.stivais.ui.constraints.plus
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.constraints.size
import com.github.stivais.ui.constraints.sizes.Bounding
import com.github.stivais.ui.elements.impl.TextInput
import com.github.stivais.ui.elements.scope.BlockScope
import com.github.stivais.ui.elements.scope.ElementDSL
import com.github.stivais.ui.renderer.Renderer
import me.odinmain.utils.skyblock.modMessage

fun basic(renderer: Renderer) = UI(renderer) {

    fun ElementDSL.test(size: Float): ElementDSL {
        return block(size(size.px, size.px), Color.RED) {
            outline(Color.BLACK)
            onMouseEnter {
                modMessage("entered $size")
                true
            }
            onMouseExit {
                modMessage("exited $size")
                true
            }
        }
    }

    test(60f).test(40f).test(20f)
}