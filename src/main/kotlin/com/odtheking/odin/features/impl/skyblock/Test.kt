package com.odtheking.odin.features.impl.skyblock

import com.odtheking.odin.clickgui.vanilla.settings.impl.*
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors

object Test: Module("Test", description = "Test") {

    val test1 by BooleanSetting("Test1", desc = "Test1")
    val test2 by ActionSetting("Test2", desc = "Test2") {
        println("Clicked")
    }

    val test3 by ColorSetting("Test3", Colors.WHITE, true, desc = "Test3")
    val test4 by DropdownSetting("Test4", desc = "Test4")
    val test5 by HUDSetting("Test5", 10, 10, 1f, true, "Test5", this) { enabled ->
        if (enabled) {
            drawString(mc.font, "Test HUD", 0, 0, Colors.WHITE.rgba)
        }
        Pair(100, 20)
    }

    val test6 by KeybindSetting("Test6", 0, desc = "Test6")
    val test7 by NumberSetting("Test7", 0, 10, 100, 1, desc = "Test7")
    val test8 by SelectorSetting("Test8", "a", listOf("a", "b", "c"), desc = "Test8")
    val test9 by StringSetting("Test9", desc = "Test9")


}