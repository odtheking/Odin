package me.odinmain.features.impl.floor7

import me.odinmain.features.impl.floor7.WitherDragons.lineThickness
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.render.Renderer

object DragonBoxes {
    fun renderBoxes() {
        WitherDragonsEnum.entries.forEach {
            if (it.entity?.isEntityAlive == true)
                Renderer.drawBox(it.boxesDimensions, it.color.withAlpha(0.5f), lineThickness, depth =  false, fillAlpha = 0)
        }
    }
}
