package com.odtheking.odin.clickgui.settings.impl

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.odtheking.odin.clickgui.ClickGUI.gray38
import com.odtheking.odin.clickgui.settings.RenderableSetting
import com.odtheking.odin.clickgui.settings.Saving
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.Color.Companion.brighter
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.ui.animations.LinearAnimation
import com.odtheking.odin.utils.ui.isAreaHovered
import com.odtheking.odin.utils.ui.rendering.NVGRenderer
import net.minecraft.client.input.MouseButtonEvent

class BooleanSetting(
    name: String,
    override val default: Boolean = false,
    desc: String,
) : RenderableSetting<Boolean>(name, desc), Saving {

    override var value: Boolean = default
    var enabled: Boolean by this::value

    private val toggleAnimation = LinearAnimation<Float>(200)

    override fun render(x: Float, y: Float, mouseX: Float, mouseY: Float): Float {
        super.render(x, y, mouseX, mouseY)
        val height = getHeight()

        NVGRenderer.text(name, x + 6f, y + height / 2f - 8f, 16f, Colors.WHITE.rgba, NVGRenderer.defaultFont)

        NVGRenderer.rect(x + width - 40f, y + height / 2f - 10f, 34f, 20f, if (isHovered) gray38.brighter().rgba else gray38.rgba, 9f)

        if (enabled || toggleAnimation.isAnimating()) {
            val color = ClickGUIModule.clickGUIColor
            NVGRenderer.rect(
                x + width - 40f,
                y + height / 2f - 10f,
                toggleAnimation.get(34f, 9f, enabled),
                20f,
                if (isHovered) color.brighter().rgba else color.rgba,
                9f
            )
        }

        NVGRenderer.hollowRect(
            x + width - 40f,
            y + height / 2f - 10f,
            34f,
            20f,
            2f,
            ClickGUIModule.clickGUIColor.rgba,
            9f
        )
        NVGRenderer.circle(x + width - toggleAnimation.get(30f, 14f, !enabled), y + height / 2f, 6f, Colors.WHITE.rgba)

        return height
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: MouseButtonEvent): Boolean {
        return if (click.button() != 0 || !isHovered) false
        else {
            toggleAnimation.start()
            enabled = !enabled
            true
        }
    }

    override val isHovered: Boolean get() = isAreaHovered(lastX + width - 43f, lastY + getHeight() / 2f - 10f, 34f, 20f, true)

    override fun write(gson: Gson): JsonElement = JsonPrimitive(enabled)

    override fun read(element: JsonElement, gson: Gson) {
        enabled = element.asBoolean
    }
}