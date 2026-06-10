package com.odtheking.odin.clickgui.vanilla.settings.impl

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.HudManager
import com.odtheking.odin.clickgui.settings.ClickGUI.gray38
import com.odtheking.odin.clickgui.settings.Saving
import com.odtheking.odin.clickgui.vanilla.VanillaRenderableSetting
import com.odtheking.odin.features.Module
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.Color.Companion.darker
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.roundedFill
import com.odtheking.odin.utils.render.roundedOutline
import com.odtheking.odin.utils.ui.isAreaHovered
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.input.MouseButtonEvent

class HUDSetting(
    name: String,
    hud: HudElement,
    private val toggleable: Boolean = false,
    description: String,
    val module: Module,
) : VanillaRenderableSetting<HudElement>(name, description), Saving {

    constructor(
        name: String,
        x: Int,
        y: Int,
        scale: Float,
        toggleable: Boolean,
        description: String,
        module: Module,
        draw: GuiGraphics.(Boolean) -> Pair<Int, Int>
    ) : this(name, HudElement(x, y, scale, !toggleable, draw), toggleable, description, module)

    override val default: HudElement = hud
    override var value: HudElement = default

    val isEnabled: Boolean get() = module.enabled && value.enabled

    override fun render(graphics: GuiGraphics, x: Int, y: Int, mouseX: Int, mouseY: Int): Int {
        super.render(graphics, x, y, mouseX, mouseY)
        val height = getHeight()
        graphics.drawString(mc.font, name, x + 6, y + height / 2 - 8, Colors.WHITE.rgba, false)

        val hudX = x + width - 30
        val hudY = y + height / 2 - 12
        val hudHovered = mouseX in hudX..hudX + 24 && mouseY in hudY..hudY + 24
        graphics.roundedFill(hudX, hudY, hudX + 24, hudY + 24, if (hudHovered) gray38.darker(0.9f).rgba else gray38.rgba, 5)
        graphics.drawString(mc.font, "HUD", hudX + 3, hudY + 8, Colors.WHITE.rgba, false)

        if (toggleable) {
            val toggleX = x + width - 70
            val toggleY = y + height / 2 - 10
            val toggleHovered = mouseX in toggleX..toggleX + 34 && mouseY in toggleY..toggleY + 20
            graphics.roundedFill(toggleX, toggleY, toggleX + 34, toggleY + 20, if (toggleHovered) gray38.darker(0.9f).rgba else gray38.rgba, 9)
            if (value.enabled) graphics.roundedFill(toggleX, toggleY, toggleX + 34, toggleY + 20, ClickGUIModule.clickGUIColor.rgba, 9)
            graphics.roundedOutline(toggleX, toggleY, toggleX + 34, toggleY + 20, ClickGUIModule.clickGUIColor.rgba, 2f, 9)
            val knobX = if (value.enabled) toggleX + 26 else toggleX + 8
            graphics.fill(knobX - 5, y + height / 2 - 5, knobX + 5, y + height / 2 + 5, Colors.WHITE.rgba)
        }

        return height
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, click: MouseButtonEvent): Boolean {
        if (click.button() != 0) return false
        return if (isHovered) {
            mc.setScreen(HudManager)
            true
        } else if (toggleable && isAreaHovered(lastX + width - 70f, lastY + getHeight() / 2f - 10f, 34f, 20f, true)) {
            value.enabled = !value.enabled
            true
        } else false
    }

    override val isHovered: Boolean
        get() = isAreaHovered(lastX + width - 30, lastY + getHeight() / 2 - 12, 24, 24)

    override fun write(gson: Gson): JsonElement = JsonObject().apply {
        addProperty("x", value.x)
        addProperty("y", value.y)
        addProperty("scale", value.scale)
        addProperty("enabled", value.enabled)
    }

    override fun read(element: JsonElement, gson: Gson) {
        if (element !is JsonObject) return
        value.x = element.get("x")?.asInt ?: value.x
        value.y = element.get("y")?.asInt ?: value.y
        value.scale = element.get("scale")?.asFloat ?: value.scale
        value.enabled = if (toggleable) element.get("enabled")?.asBoolean ?: value.enabled else true
    }
}