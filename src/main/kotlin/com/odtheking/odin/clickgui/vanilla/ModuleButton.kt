package com.odtheking.odin.clickgui.vanilla

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.features.Module
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.Color.Companion.brighter
import com.odtheking.odin.utils.Color.Companion.darker
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.ui.HoverHandler
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent

class ModuleButton(val module: Module, private val panel: Panel) {

    val representableSettings = module.settings.values.filterIsInstance<VanillaRenderableSetting<*>>()
    private val hoverHandler = HoverHandler(600)
    private val settingHoverHandlers = hashMapOf<VanillaRenderableSetting<*>, HoverHandler>()
    var extended = false

    private var lastX = 0
    private var lastY = 0

    fun draw(graphics: GuiGraphics, x: Int, y: Int, mouseX: Int, mouseY: Int): Int {
        lastX = x
        lastY = y

        hoverHandler.handle(x, y, Panel.WIDTH, Panel.HEIGHT - 1)
        if (hoverHandler.percent() >= 100f && y >= panel.panelSetting.y + Panel.HEIGHT) {
            VanillaGUI.setDescription(module.description, x + Panel.WIDTH + 10, y, hoverHandler)
        }

        val baseColor = if (module.enabled) ClickGUIModule.clickGUIColor else VanillaGUI.gray26
        val renderColor = if (hoverHandler.isHovered) {
            if (module.enabled) baseColor.darker(0.8f) else baseColor.brighter(1.4f)
        } else {
            baseColor
        }

        graphics.fill(x, y, x + Panel.WIDTH, y + Panel.HEIGHT, renderColor.rgba)
        graphics.drawCenteredString(mc.font, module.name, x + Panel.WIDTH / 2, y + Panel.HEIGHT / 2 - 4, Colors.WHITE.rgba)

        if (representableSettings.isEmpty() || !extended) return Panel.HEIGHT

        var drawY = Panel.HEIGHT
        for (setting in representableSettings) {
            if (!setting.isVisible) continue

            drawY += setting.render(graphics, x, y + drawY, mouseX, mouseY)
            val settingY = y + drawY
            val settingHoverHandler = settingHoverHandlers.getOrPut(setting) { HoverHandler(750) }
            if (setting.description.isNotEmpty() && settingHoverHandler.percent() >= 100f) {
                VanillaGUI.setDescription(setting.description, x + Panel.WIDTH + 10, settingY, settingHoverHandler)
            }
        }

        return drawY
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, click: MouseButtonEvent): Boolean {
        if (hoverHandler.isHovered) {
            if (click.button() == 0) {
                module.toggle()
                return true
            }
            if (click.button() == 1) {
                if (representableSettings.isNotEmpty()) extended = !extended
                return true
            }
        } else if (extended) {
            for (setting in representableSettings) {
                if (!setting.isVisible) continue
                if (setting.mouseClicked(mouseX, mouseY, click)) return true
            }
        }
        return false
    }

    fun mouseReleased(mouseX: Int, mouseY: Int, click: MouseButtonEvent) {
        if (!extended) return
        for (setting in representableSettings) {
            if (!setting.isVisible) continue
            setting.mouseReleased(mouseX, mouseY, click)
        }
    }

    fun keyTyped(input: CharacterEvent): Boolean {
        if (!extended) return false
        for (setting in representableSettings) {
            if (!setting.isVisible) continue
            if (setting.keyTyped(input)) return true
        }
        return false
    }

    fun keyPressed(input: KeyEvent): Boolean {
        if (!extended) return false
        for (setting in representableSettings) {
            if (!setting.isVisible) continue
            if (setting.keyPressed(input)) return true
        }
        return false
    }
}