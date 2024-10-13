package me.odinmain.features.impl.floor7

import com.github.stivais.ui.color.multiplyAlpha
import me.odinmain.features.impl.floor7.WitherDragons.lineThickness
import me.odinmain.utils.render.Renderer

object DragonBoxes {
    fun renderBoxes() {
        WitherDragonsEnum.entries.forEach {
            if (it.state != WitherDragonState.DEAD)
                Renderer.drawBox(it.boxesDimensions, it.color.multiplyAlpha(0.5f), lineThickness, depth = false, fillAlpha = 0)
        }
    }
}
