package com.odtheking.odin.clickgui.vanilla.settings.impl

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.settings.ClickGUI.gray38
import com.odtheking.odin.clickgui.settings.Saving
import com.odtheking.odin.clickgui.vanilla.Panel
import com.odtheking.odin.clickgui.vanilla.VanillaRenderableSetting
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.Color.Companion.brighter
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.roundedFill
import com.odtheking.odin.utils.render.roundedOutline
import com.odtheking.odin.utils.ui.animations.EaseInOutAnimation
import com.odtheking.odin.utils.ui.animations.LinearAnimation
import com.odtheking.odin.utils.ui.isAreaHovered
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.input.MouseButtonEvent

class SelectorSetting(
    name: String,
    default: String,
    private var options: List<String>,
    desc: String
) : VanillaRenderableSetting<Int>(name, desc), Saving {

    override val default: Int = optionIndex(default)

    override var value: Int
        get() = index
        set(value) { index = value }

    private var index: Int = optionIndex(default)
        set(value) {
            field = if (value > options.size - 1) 0 else if (value < 0) options.size - 1 else value
        }

    private var selected: String
        get() = options[index]
        set(value) { index = optionIndex(value) }

    private val expandAnim = EaseInOutAnimation(200)
    private var extended = false

    private val pillHoverAnim = LinearAnimation<Float>(150)
    private var pillWasHovered = false

    override fun render(graphics: GuiGraphics, x: Int, y: Int, mouseX: Int, mouseY: Int): Int {
        super.render(graphics, x, y, mouseX, mouseY)

        val selW    = mc.font.width(selected)
        val pillX   = x + width - 20 - selW
        val pillY   = y + Panel.HEIGHT / 2 - 8
        val pillW   = selW + 12
        val pillH   = 17

        val pillHovered = mouseX in pillX..pillX + pillW && mouseY in pillY..pillY + pillH
        if (pillHovered != pillWasHovered) { pillHoverAnim.start(); pillWasHovered = pillHovered }
        val hoverPct = pillHoverAnim.get(0f, 1f, !pillHovered)
        val pillColor = gray38.brighter(1f + hoverPct / 5f)

        graphics.roundedFill(pillX, pillY, pillX + pillW, pillY + pillH, pillColor.rgba, 5, ClickGUIModule.clickGUIColor.rgba, 1.5f)

        graphics.drawString(mc.font, name, x + 6, y + Panel.HEIGHT / 2 - 4, Colors.WHITE.rgba, false)
        graphics.drawString(mc.font, selected, x + width - 14 - selW, y + Panel.HEIGHT / 2 - 4, Colors.WHITE.rgba, false)

        if (!extended && !expandAnim.isAnimating()) return Panel.HEIGHT

        val animH = getHeight()
        val clipH = animH - Panel.HEIGHT
        if (clipH <= 0) return Panel.HEIGHT
        graphics.enableScissor(x, y + Panel.HEIGHT, x + width, y + Panel.HEIGHT + clipH)

        val cX = x + 4
        val cY = y + 22
        val cW = width - 8
        val cH = options.size * 16
        graphics.roundedFill(cX, cY, cX + cW, cY + cH, gray38.rgba, 5)

        for (i in options.indices) {
            val optionY = cY + 1 + i * 16

            if (i != options.size - 1)
                graphics.fill(cX + 14, optionY + 15, cX + cW - 14, optionY + 16, Colors.MINECRAFT_DARK_GRAY.rgba)

            graphics.drawCenteredString(mc.font, options[i], x + width / 2, optionY + 4, Colors.WHITE.rgba)

            if (isOptionHovered(i))
                graphics.roundedOutline(cX, optionY - 1, cX + cW, optionY + 16, ClickGUIModule.clickGUIColor.rgba, 1.5f, 4)
        }

        graphics.disableScissor()
        return animH
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, click: MouseButtonEvent): Boolean {
        if (click.button() == 1 && isHovered) {
            index++
            return true
        }

        if (click.button() != 0) return false

        if (isHovered) {
            expandAnim.start()
            extended = !extended
            return true
        }

        if (!extended) return false

        for (i in options.indices) {
            if (isOptionHovered(i)) {
                expandAnim.start()
                selected = options[i]
                extended = false
                return true
            }
        }
        return false
    }

    override val isHovered: Boolean
        get() = isAreaHovered(lastX, lastY, width, Panel.HEIGHT)

    override fun getHeight(): Int {
        val collapsed = Panel.HEIGHT.toFloat()
        val fullOpen  = (options.size * 16 + 26).toFloat()
        return expandAnim.get(collapsed, fullOpen, !extended).toInt()
    }

    private fun isOptionHovered(i: Int): Boolean =
        isAreaHovered(lastX + 4, lastY + 26 + i * 16, width - 8, 16)

    private fun optionIndex(string: String): Int =
        options.map { it.lowercase() }.indexOf(string.lowercase()).coerceIn(0, options.size - 1)

    override fun write(gson: Gson): JsonElement = JsonPrimitive(selected)

    override fun read(element: JsonElement, gson: Gson) {
        element.asString?.let { selected = it }
    }
}