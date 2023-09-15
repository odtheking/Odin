package me.odinclient.features.impl.render

import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.clock.Clock
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object NoCursorReset : Module(
    "No Cursor Reset",
    description = "Makes your cursor stop resetting between guis.",
    category = Category.RENDER
) {
    private val clock = Clock(150)
    private var wasNotNull = false

    @SubscribeEvent
    fun onGuiOpen(e: GuiOpenEvent) {
        val oldGuiScreen = mc.currentScreen
        if (e.gui is GuiChest && (oldGuiScreen is GuiContainer || oldGuiScreen == null)) {
            clock.update()
        }
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        wasNotNull = mc.currentScreen != null
    }

    fun shouldHookMouse(): Boolean {
        return !clock.hasTimePassed() && wasNotNull && enabled
    }
}