package com.odtheking.odin.clickgui

import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Color.Companion.brighter
import com.odtheking.odin.utils.Colors
import net.minecraft.util.ARGB

object GuiTheme {

    const val ROW_WIDTH = 160
    const val ROW_HEIGHT = 21
    const val RADIUS = 5f
    const val CAP = 5
    const val PADDING = 6

    const val PANEL_BLUR = 12f

    fun textY(y: Int, height: Int): Int = y + (height - 8) / 2

    val background: Color get() = Colors.gray26
    val surface: Color get() = Colors.gray38
    val accent: Color get() = ClickGUIModule.clickGUIColor

    val shadow: Color = Color(0, 0, 0, 0.4f)
}

internal fun blend(from: Int, to: Int, progress: Float): Int = ARGB.srgbLerp(progress, from, to)
internal fun Color.hoverTint(hover: Float, factor: Float = 1.3f): Int =
    if (hover <= 0f) rgba else blend(rgba, brighter(factor).rgba, hover)