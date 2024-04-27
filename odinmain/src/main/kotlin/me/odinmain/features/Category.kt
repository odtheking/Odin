package me.odinmain.features

import me.odinmain.features.impl.render.ClickGUIModule

enum class Category {
    DUNGEON, FLOOR7, RENDER, SKYBLOCK, KUUDRA;

    val x: Float
        get() = ClickGUIModule.panelX[this]!!.value

    val y: Float
        get() = ClickGUIModule.panelY[this]!!.value

    val extended: Boolean
        get() = ClickGUIModule.panelExtended[this]!!.value
}