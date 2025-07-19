package me.odinmain.clickgui.settings

import me.odinmain.clickgui.ClickGUI
import me.odinmain.clickgui.ClickGUI.gray26
import me.odinmain.clickgui.Panel
import me.odinmain.features.Module
import me.odinmain.features.impl.render.ClickGUIModule
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Color.Companion.brighter
import me.odinmain.utils.render.Colors
import me.odinmain.utils.ui.HoverHandler
import me.odinmain.utils.ui.animations.ColorAnimation
import me.odinmain.utils.ui.animations.EaseInOutAnimation
import me.odinmain.utils.ui.mouseX
import me.odinmain.utils.ui.mouseY
import me.odinmain.utils.ui.rendering.NVGRenderer
import kotlin.math.floor

/**
 * Renders all the modules.
 *
 * @author Stivais, Aton
 *
 * see [RenderableSetting]
 */
class ModuleButton(val module: Module, private val panel: Panel) {

    val representableSettings = module.settings.filterIsInstance<RenderableSetting<*>>()

    private val colorAnim = ColorAnimation(150)

    private val color: Color get() =
        colorAnim.get(ClickGUIModule.clickGUIColor, gray26, module.enabled).brighter(1 + hover.percent() / 500f)

    private val nameWidth = NVGRenderer.textWidth(module.name, 18f, NVGRenderer.defaultFont)
    private val hoverHandler = HoverHandler(750, 200)
    private val extendAnim = EaseInOutAnimation(250)
    private val hover = HoverHandler(250)
    var extended = false

    fun draw(x: Float, y: Float): Float {
        hoverHandler.handle(x, y, Panel.WIDTH, Panel.HEIGHT - 1)
        hover.handle(x, y, Panel.WIDTH, Panel.HEIGHT - 1)

        if (hoverHandler.percent() > 0 && y >= panel.panelSetting.y + Panel.HEIGHT)
            ClickGUI.setDescription(module.description, x + Panel.WIDTH + 10f, y, hoverHandler)

        NVGRenderer.rect(x, y, Panel.WIDTH, Panel.HEIGHT, color.rgba)
        NVGRenderer.text(module.name, x + Panel.WIDTH / 2 - nameWidth / 2, y + Panel.HEIGHT / 2 - 9f, 18f, Colors.WHITE.rgba, NVGRenderer.defaultFont)

        if (module.settings.isEmpty()) return Panel.HEIGHT

        val totalHeight = Panel.HEIGHT + floor(extendAnim.get(0f, getSettingHeight(), !extended))
        var drawY = Panel.HEIGHT

        if (extendAnim.isAnimating()) NVGRenderer.pushScissor(x, y, Panel.WIDTH, totalHeight)

        if (extendAnim.isAnimating() || extended) {
            for (setting in module.settings) {
                if (setting is RenderableSetting<*> && setting.isVisible) drawY += setting.render(x, y + drawY, mouseX, mouseY)
            }
        }

        if (extendAnim.isAnimating()) NVGRenderer.popScissor()
        return totalHeight
    }

    fun mouseClicked(mouseX: Float, mouseY: Float, button: Int): Boolean {
        if (hover.hasStarted) {
            if (button == 0) {
                colorAnim.start()
                module.toggle()
                return true
            } else if (button == 1) {
                if (module.settings.isNotEmpty()) {
                    extendAnim.start()
                    extended = !extended
                }
                return true
            }
        } else if (extended) {
            for (setting in representableSettings) {
                if (setting.isVisible && setting.mouseClicked(mouseX, mouseY, button)) return true
            }
        }
        return false
    }

    fun mouseReleased(state: Int) {
        if (!extended) return
        for (setting in representableSettings) {
            if (setting.isVisible) setting.mouseReleased(state)
        }
    }

    fun keyTyped(typedChar: Char): Boolean {
        if (!extended) return false
        for (setting in representableSettings) {
            if (setting.isVisible && setting.keyTyped(typedChar)) return true
        }
        return false
    }

    fun keyPressed(keyCode: Int): Boolean {
        if (!extended) return false
        for (setting in representableSettings) {
            if (setting.isVisible && setting.keyPressed(keyCode)) return true
        }
        return false
    }

    private fun getSettingHeight(): Float =
        representableSettings.filter { it.isVisible }.sumOf { it.getHeight().toDouble() }.toFloat()
}