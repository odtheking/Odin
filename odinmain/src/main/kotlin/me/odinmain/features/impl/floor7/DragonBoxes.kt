package me.odinmain.features.impl.floor7

import me.odinmain.features.impl.floor7.WitherDragons.lineThickness
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.render.world.RenderUtils

object DragonBoxes {
    fun renderBoxes() {
        WitherDragonsEnum.entries.forEach {
            if (it.entity?.isEntityAlive == true)
                RenderUtils.drawBoxOutline(it.boxesDimensions, it.color.withAlpha(0.5f), lineThickness, phase = false)
        }
    }
}
