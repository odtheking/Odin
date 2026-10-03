package com.odtheking.odin.features.impl.dungeon.dungeonwaypoints

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.OdinScreen
import com.odtheking.odin.utils.ui.compose.UiHost
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component

class TextPromptScreen(val promptTitle: String) : OdinScreen(Component.literal(promptTitle)) {
    private var callback: (String) -> Unit = {}

    override val ui = UiHost {
        PromptDialog(promptTitle, { callback(it) }, { mc.gui.setScreen(null) }, scrim = false)
    }

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick)
        ui.render(graphics, width, height, mouseX, mouseY)
    }

    override fun removed() {
        super.removed()
        ui.dispose()
    }

    fun setCallback(callback: (String) -> Unit): TextPromptScreen {
        this.callback = callback
        return this
    }
}