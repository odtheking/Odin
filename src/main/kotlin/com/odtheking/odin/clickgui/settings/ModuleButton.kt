package com.odtheking.odin.clickgui.settings

import com.odtheking.odin.clickgui.ClickGUI
import com.odtheking.odin.clickgui.ClickGUI.gray26
import com.odtheking.odin.clickgui.Panel
import com.odtheking.odin.features.Module
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Color.Companion.brighter
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.ui.HoverHandler
import com.odtheking.odin.utils.ui.animations.ColorAnimation
import com.odtheking.odin.utils.ui.animations.EaseInOutAnimation
import com.odtheking.odin.utils.ui.mouseX
import com.odtheking.odin.utils.ui.mouseY
import com.odtheking.odin.utils.ui.rendering.NVGRenderer
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.MouseButtonEvent
import kotlin.math.floor

/**
 * Renders all the modules.
 *
 * @author Stivais, Aton
 *
 * see [RenderableSetting]
 */
class ModuleButton(val module: Module, val panel: Panel) {

    // don't like this.
    val representableSettings = module.settings.values.mapNotNull { setting -> setting as? RenderableSetting }

    private val colorAnim = ColorAnimation(150)

    private val color: Color
        get() =
            colorAnim.get(ClickGUIModule.clickGUIColor, gray26, module.enabled).brighter(1 + hover.percent() / 500f)

    private val nameWidth = NVGRenderer.textWidth(module.name, 18f, NVGRenderer.defaultFont)
    private val hoverHandler = HoverHandler(750)
    private val extendAnim = EaseInOutAnimation(250)
    private val hover = HoverHandler(250)
    var extended = false

    fun draw(x: Float, y: Float, lastModule: Boolean = false): Float {
        hoverHandler.handle(x, y, Panel.WIDTH, Panel.HEIGHT - 1, true)
        hover.handle(x, y, Panel.WIDTH, Panel.HEIGHT - 1, true)

        if (hoverHandler.percent() >= 100 && y >= panel.panelSetting.y + Panel.HEIGHT)
            ClickGUI.setDescription(module.description, x + Panel.WIDTH + 10f, y, hoverHandler)

        if (!ClickGUIModule.roundedPanelBottom && lastModule) {
            NVGRenderer.rect(x, y, Panel.WIDTH, Panel.HEIGHT - 10f, color.rgba)
            NVGRenderer.drawHalfRoundedRect(x, y + Panel.HEIGHT - 10f, Panel.WIDTH, 10f, color.rgba, 5f, false)
        } else {
            NVGRenderer.rect(x, y, Panel.WIDTH, Panel.HEIGHT, color.rgba)
        }
        NVGRenderer.text(module.name, x + Panel.WIDTH / 2 - nameWidth / 2, y + Panel.HEIGHT / 2 - 9f, 18f, Colors.WHITE.rgba, NVGRenderer.defaultFont)

        if (representableSettings.isEmpty()) return Panel.HEIGHT

        val totalHeight = Panel.HEIGHT + floor(extendAnim.get(0f, getSettingHeight(), !extended))
        var drawY = Panel.HEIGHT

        if (extendAnim.isAnimating()) NVGRenderer.pushScissor(x, y, Panel.WIDTH, totalHeight)

        if (extendAnim.isAnimating() || extended) {
            for (setting in representableSettings) {
                if (setting.isVisible) drawY += setting.render(x, y + drawY, mouseX / ClickGUIModule.getStandardGuiScale(), mouseY / ClickGUIModule.getStandardGuiScale())
            }
        }

        if (extendAnim.isAnimating()) NVGRenderer.popScissor()
        return totalHeight
    }

    fun mouseClicked(mouseX: Float, mouseY: Float, click: MouseButtonEvent): Boolean {
        if (hover.isHovered) {
            if (click.button() == 0) {
                colorAnim.start()
                module.toggle()
                return true
            } else if (click.button() == 1) {
                if (module.settings.isNotEmpty()) {
                    extendAnim.start()
                    extended = !extended
                }
                return true
            }
        } else if (extended) {
            for (setting in representableSettings) {
                if (setting.isVisible && setting.mouseClicked(mouseX, mouseY, click)) return true
            }
        }
        return false
    }

    fun mouseReleased(click: MouseButtonEvent) {
        if (!extended) return
        for (setting in representableSettings) {
            if (setting.isVisible) setting.mouseReleased(click)
        }
    }

    fun keyTyped(input: CharacterEvent): Boolean {
        if (!extended) return false
        for (setting in representableSettings) {
            if (setting.isVisible && setting.keyTyped(input)) return true
        }
        return false
    }

    fun keyPressed(input: net.minecraft.client.input.KeyEvent): Boolean {
        if (!extended) return false
        for (setting in representableSettings) {
            if (setting.isVisible && setting.keyPressed(input)) return true
        }
        return false
    }

    private fun getSettingHeight(): Float =
        representableSettings.filter { it.isVisible }.sumOf { it.getHeight().toDouble() }.toFloat()
}