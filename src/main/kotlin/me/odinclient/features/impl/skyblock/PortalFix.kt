package me.odinclient.features.impl.skyblock

import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.inventory.GuiContainer

object PortalFix : Module(
    name = "Portal Fix",
    description = "Lets you use chat while in portal.",
    category = Category.SKYBLOCK
) {

    private var onlyChat: Boolean by BooleanSetting("Only Chat", default = true, description = "Only lets you use the chat while in a portal")

    fun useChatInPortalMixin(gui: GuiScreen): Boolean
    {
        return if (PortalFix.enabled) {
            if (onlyChat) {
                gui !is GuiContainer || gui.doesGuiPauseGame()
            } else true
        } else gui.doesGuiPauseGame()
    }

}