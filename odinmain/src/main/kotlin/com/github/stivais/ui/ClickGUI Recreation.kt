package com.github.stivais.ui

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.at
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.constraints.size
import com.github.stivais.ui.elements.block
import com.github.stivais.ui.elements.column
import com.github.stivais.ui.elements.text
import com.github.stivais.ui.utils.draggable
import com.github.stivais.ui.utils.radii
import me.odinmain.features.Category
import me.odinmain.utils.capitalizeFirst

fun clickGUI() = UI {
    for (panel in Category.entries) {
        column(at(x = panel.x.px, y = panel.y.px)) {
            block(
                constraints = size(240.px, 40.px),
                color = Color.RGB(26, 26, 26),
                radius = radii(tl = 5, tr = 5)
            ) {
                text(
                    text = panel.name.capitalizeFirst(),
                    size = 20.px
                )
                draggable(target = parent!!)
            }
            block(
                constraints = size(240.px, 10.px),
                color = Color.RGB(26, 26, 26),
                radius = radii(br = 5, bl = 5)
            )
        }
    }
}