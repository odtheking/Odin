package me.odinclient.ui.clickgui.elements.menu

import cc.polyfrost.oneconfig.renderer.font.Fonts
import cc.polyfrost.oneconfig.utils.dsl.*
import me.odinclient.features.settings.impl.DummySetting
import me.odinclient.ui.clickgui.elements.Element
import me.odinclient.ui.clickgui.elements.ElementType
import me.odinclient.ui.clickgui.elements.ModuleButton
import me.odinclient.ui.clickgui.util.ColorUtil

class ElementDescription(parent: ModuleButton, private val description: String) : Element<DummySetting>(parent, DummySetting("Description"), ElementType.DESCRIPTION) {


    override fun render(vg: VG): Float {
        var h = 0f

        nanoVG(vg.instance) {
            drawRect(x, y, width, height + 20f, ColorUtil.elementBackground)

            val bounds = nanoVGHelper.getWrappedStringBounds(this.instance, description, width - 10f, 12f, Fonts.REGULAR)
            h = bounds[0]

            drawWrappedString(description, x + 5f, y + 4f, width - 10f, -1, 12f, 1f, Fonts.REGULAR)
        }
        return h
    }
}