package com.odtheking.odin.clickgui.vanilla.settings.impl

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.settings.ClickGUI.gray38
import com.odtheking.odin.clickgui.settings.Saving
import com.odtheking.odin.clickgui.vanilla.Panel
import com.odtheking.odin.clickgui.vanilla.VanillaRenderableSetting
import com.odtheking.odin.clickgui.vanilla.VanillaTextInput
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.roundedFill
import com.odtheking.odin.utils.render.roundedOutline
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent

class StringSetting(
    name: String,
    override val default: String = "",
    private var length: Int = 32,
    desc: String
) : VanillaRenderableSetting<String>(name, desc), Saving {

    override var value: String = default
        set(value) {
            field = if (value.length <= length) value else return
        }

    private val input = VanillaTextInput(length, length, { value }, { value = it })

    override fun render(graphics: GuiGraphics, x: Int, y: Int, mouseX: Int, mouseY: Int): Int {
        super.render(graphics, x, y, mouseX, mouseY)
        val h = getHeight()

        graphics.drawString(mc.font, name, x + 6, y + h / 2 - 13, Colors.WHITE.rgba, false)

        val boxX = x + 6
        val boxY = y + h - 20
        val boxW = width - 12
        val boxH = 20
        graphics.roundedFill(boxX, boxY, boxX + boxW, boxY + boxH, gray38.rgba, 4)
        graphics.roundedOutline(boxX, boxY, boxX + boxW, boxY + boxH, ClickGUIModule.clickGUIColor.rgba, 1.5f, 4)

        setInputCoords(x, y, h)
        input.draw(graphics)

        return h
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, click: MouseButtonEvent): Boolean {
        if (click.button() != 0) return false
        setInputCoords(lastX, lastY, getHeight())
        return input.mouseClicked(mouseX, mouseY, click)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, click: MouseButtonEvent) {
        input.mouseReleased()
    }

    override fun keyTyped(input: CharacterEvent): Boolean  = this.input.keyTyped(input)
    override fun keyPressed(input: KeyEvent): Boolean      = this.input.keyPressed(input)

    override fun getHeight(): Int = Panel.HEIGHT + 18

    override fun write(gson: Gson): JsonElement = JsonPrimitive(value)

    override fun read(element: JsonElement, gson: Gson) {
        element.asString?.let { value = it }
    }

    private fun setInputCoords(x: Int, y: Int, h: Int) {
        input.x      = x + 6
        input.y      = y + h - 18
        input.width  = width - 16
        input.height = 16
    }
}