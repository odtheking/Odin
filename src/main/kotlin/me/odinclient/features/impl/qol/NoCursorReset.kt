package me.odinclient.features.impl.qol

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.clock.Clock
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object NoCursorReset : Module(
    "No Cursor Reset",
    category = Category.QOL
) {
    private val clock = Clock(100)
    private val hasBeenNull get() = mc.currentScreen != null // test this cuz I cant test stuff

    @SubscribeEvent
    fun onGuiOpen(e: GuiOpenEvent) {
        val oldGuiScreen = mc.currentScreen
        if (e.gui is GuiChest && (oldGuiScreen is GuiContainer || oldGuiScreen == null)) {
            clock.update()
        }
    }

    fun shouldHookMouse(): Boolean {
        return clock.hasTimePassed() && !hasBeenNull && enabled
    }
}