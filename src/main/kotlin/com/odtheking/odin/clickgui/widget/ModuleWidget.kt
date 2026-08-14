package com.odtheking.odin.clickgui.widget

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.GuiTheme
import com.odtheking.odin.clickgui.blend
import com.odtheking.odin.clickgui.settings.RenderableSetting
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color.Companion.brighter
import com.odtheking.odin.utils.Color.Companion.darker
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.pushScissor
import com.odtheking.odin.utils.ui.animations.Easing
import com.odtheking.odin.utils.ui.animations.Fade
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component

class ModuleWidget(val module: Module) : OdinContainerWidget(0, 0, GuiTheme.ROW_WIDTH, GuiTheme.ROW_HEIGHT, Component.literal(module.name)) {
    private val header = ModuleHeaderWidget(this, module)
    private val settings = module.settings.values.filterIsInstance<RenderableSetting<*>>()

    private val elements: List<GuiEventListener> = listOf(header) + settings
    private val headerOnly: List<GuiEventListener> = listOf(header)

    private val expand = Fade(EXPAND_DURATION, Easing.EASE_IN_OUT)
    private val filter = Fade(FILTER_DURATION, Easing.EASE_IN_OUT, initial = true)

    private var revealed = 0
    var expanded = false
        private set

    private var shown = 1f

    val isFiltering: Boolean get() = filter.isAnimating

    override fun children(): List<GuiEventListener> = if (expanded) elements else headerOnly

    fun matches(search: String): Boolean {
        shown = filter.progress(module.name.contains(search, true))
        return shown > 0f
    }

    fun toggleExpanded() {
        if (settings.isNotEmpty()) expanded = !expanded
    }

    fun release() = settings.forEach { it.release() }

    fun measure() {
        val progress = expand.progress(expanded)

        var content = 0
        for (widget in settings) {
            widget.visible = progress > 0f && widget.isVisible
            if (!widget.visible) continue
            widget.measure()
            content += widget.height
        }

        revealed = (progress * content).toInt()
        height = ((GuiTheme.ROW_HEIGHT + revealed) * shown).toInt()
    }

    fun place() {
        header.x = x
        header.y = y

        var offset = GuiTheme.ROW_HEIGHT
        for (widget in settings) {
            if (!widget.visible) continue
            widget.x = x
            widget.y = y + offset
            widget.place()
            offset += widget.height
        }
    }

    override fun render(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        val squeezed = height < GuiTheme.ROW_HEIGHT + revealed
        if (squeezed) graphics.pushScissor(x, y, x + width, y + height)

        header.extractRenderState(graphics, mouseX, mouseY, 0f)

        if (revealed > 0) {
            val top = y + GuiTheme.ROW_HEIGHT
            graphics.pushScissor(x, top, x + width, top + revealed)
            for (widget in settings) {
                if (widget.visible) widget.extractRenderState(graphics, mouseX, mouseY, 0f)
            }
            graphics.disableScissor()
        }

        if (squeezed) graphics.disableScissor()
    }

    private companion object {
        const val EXPAND_DURATION = 250L
        const val FILTER_DURATION = 150L
    }
}

private class ModuleHeaderWidget(private val group: ModuleWidget, private val module: Module) : OdinWidget(
    0, 0, GuiTheme.ROW_WIDTH, GuiTheme.ROW_HEIGHT, Component.literal(module.name)
) {
    override val clickButtons = BOTH_BUTTONS
    override val description = module.description

    private val enabledFade = Fade(ENABLED_DURATION)

    override fun render(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        val toggled = enabledFade.progress(module.enabled)
        var color = blend(GuiTheme.background.rgba, GuiTheme.accent.rgba, toggled)

        val hovered = hover
        if (hovered > 0f)
            color = blend(color, blend(GuiTheme.background.brighter(1.4f).rgba, GuiTheme.accent.darker(0.8f).rgba, toggled), hovered)

        graphics.fill(x, y, x + width, y + height, color)
        graphics.centeredText(mc.font, module.name, x + width / 2, GuiTheme.textY(y, height), Colors.WHITE.rgba)
    }

    override fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        if (event.button() == RIGHT) group.toggleExpanded() else module.toggle()
    }

    private companion object {
        const val ENABLED_DURATION = 150L
    }
}
