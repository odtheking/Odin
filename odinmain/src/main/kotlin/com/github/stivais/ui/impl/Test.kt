package com.github.stivais.ui.impl

import com.github.stivais.ui.UI
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.constrain
import com.github.stivais.ui.constraints.plus
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.constraints.size
import com.github.stivais.ui.constraints.sizes.Bounding
import com.github.stivais.ui.elements.impl.TextInput
import com.github.stivais.ui.renderer.Renderer

fun basic(renderer: Renderer) = UI(renderer) {

    TextInput(
        "0.0",
        "placeholder",
        constraints = size(h = 40.px),
        onlyNumbers =  true,
    ).add()
}