package com.github.stivais.ui.impl

import com.github.stivais.commodore.utils.SyntaxException
import com.github.stivais.ui.UIScreen.Companion.open
import com.github.stivais.ui.renderer.CookedRenderer
import com.github.stivais.ui.renderer.NVGRenderer
import me.odinmain.commands.commodore

val `ui command` = commodore("ui") {
    runs { type: String, `use old renderer`: Boolean? ->
        val renderer = if (`use old renderer` == true) CookedRenderer else NVGRenderer
        val ui = when (type) {
            "basic" -> basic(renderer)
            "clickgui" -> clickGUI(renderer)
            else -> throw SyntaxException()
        }
        open(ui)
    }.suggests(
        "type" to listOf("basic", "clickgui"),
        "use old renderer" to listOf("true", "false") // i have no idea why it doesn't automatically do
    )
}