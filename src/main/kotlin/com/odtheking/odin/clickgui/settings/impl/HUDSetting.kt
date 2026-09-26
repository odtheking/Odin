package com.odtheking.odin.clickgui.settings.impl

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.HudManager
import com.odtheking.odin.clickgui.settings.RenderableSetting
import com.odtheking.odin.clickgui.settings.Saving
import com.odtheking.odin.clickgui.widget.Toggle
import com.odtheking.odin.clickgui.widget.drawIcon
import com.odtheking.odin.clickgui.widget.drawToggle
import com.odtheking.odin.clickgui.widget.isOver
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.ui.animations.Fade
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.resources.Identifier

class HUDSetting(
    name: String,
    hud: HudElement,
    private val toggleable: Boolean = false,
    desc: String,
    val module: Module,
) : RenderableSetting<HudElement>(name, desc), Saving {

    constructor(
        name: String,
        x: Int,
        y: Int,
        scale: Float,
        toggleable: Boolean,
        description: String,
        module: Module,
        draw: GuiGraphicsExtractor.(Boolean) -> Pair<Int, Int>
    ) : this(name, HudElement(x, y, scale, !toggleable, draw), toggleable, description, module)

    override val default: HudElement = hud
    override var value: HudElement = default

    val isEnabled: Boolean get() = module.enabled && value.enabled
    val hud get() = value

    private val toggleAnimation = Fade(TOGGLE_DURATION)
    private val iconHover = Fade(HOVER_DURATION)

    private val iconX get() = x + width - ICON - RIGHT_PAD
    private val iconY get() = y + (height - ICON) / 2

    private val switchX get() = iconX - Toggle.WIDTH - GAP

    private fun overIcon(mouseX: Int, mouseY: Int): Boolean = isOver(mouseX, mouseY, iconX, iconY, ICON, ICON)

    override fun render(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        drawLabel(graphics)

        graphics.drawIcon(MOVEMENT, iconX, iconY, ICON, iconHover.progress(overIcon(mouseX, mouseY)))

        if (!toggleable) return
        graphics.drawToggle(switchX, y + height / 2, toggleAnimation.progress(value.enabled), hover)
    }

    override fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        val mouseX = event.x().toInt()
        val mouseY = event.y().toInt()

        when {
            overIcon(mouseX, mouseY) -> mc.setScreenAndShow(HudManager)
            toggleable && mouseX >= switchX -> value.enabled = !value.enabled
        }
    }

    override fun write(gson: Gson): JsonElement = value.write()

    override fun read(element: JsonElement, gson: Gson) = value.read(element, toggleable)

    private companion object {
        val MOVEMENT: Identifier = Identifier.fromNamespaceAndPath("odin", "textures/movementicon.png")
        const val TOGGLE_DURATION = 200L

        const val HOVER_DURATION = 150L

        const val ICON = Toggle.HEIGHT
        const val RIGHT_PAD = 6
        const val GAP = 6
    }
}