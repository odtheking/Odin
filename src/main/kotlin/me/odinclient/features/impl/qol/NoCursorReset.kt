package me.odinclient.features.impl.qol

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object NoCursorReset : Module(
    "No Cursor Reset",
    category = Category.QOL
) {
    private var lastContainerOpen = 0L
    private var hasBeenNullFor = 0

    @SubscribeEvent
    fun onGuiOpen(e: GuiOpenEvent) {
        val oldGuiScreen = mc.currentScreen
        if (e.gui is GuiChest && (oldGuiScreen is GuiContainer || oldGuiScreen == null))
            lastContainerOpen = System.currentTimeMillis()
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (mc.currentScreen != null) hasBeenNullFor = 0
        else hasBeenNullFor++
    }

    fun shouldHookMouse() = System.currentTimeMillis() - lastContainerOpen < 100 && hasBeenNullFor == 0 && this.enabled
}