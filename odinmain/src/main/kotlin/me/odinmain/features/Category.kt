package me.odinmain.features

import me.odinmain.features.impl.render.ClickGUIModule

enum class Category {
    DUNGEON, FLOOR7, RENDER, SKYBLOCK, NETHER;

    var x: Float
        get() = ClickGUIModule.panelX[this]!!.value
        set(value) {
            ClickGUIModule.panelX[this]!!.value = value
        }

    var y: Float
        get() = ClickGUIModule.panelY[this]!!.value
        set(value) {
            ClickGUIModule.panelY[this]!!.value = value
        }

    var extended: Boolean
        get() = ClickGUIModule.panelExtended[this]!!.value
        set(value) {
            ClickGUIModule.panelExtended[this]!!.value = value
        }
}