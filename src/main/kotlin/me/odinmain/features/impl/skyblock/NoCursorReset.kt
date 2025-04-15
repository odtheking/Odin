package me.odinmain.features.impl.skyblock

import me.odinmain.features.Module
import me.odinmain.utils.clock.Clock
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object NoCursorReset : Module(
    name = "No Cursor Reset",
    desc = "Makes your cursor stop resetting between guis.",
) {
    private val clock = Clock(150)
    private var wasNotNull = false

    @SubscribeEvent
    fun onGuiOpen(event: GuiOpenEvent) {
        val oldGuiScreen = mc.currentScreen
        if (event.gui is GuiChest && (oldGuiScreen is GuiContainer || oldGuiScreen == null)) clock.update()
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        wasNotNull = mc.currentScreen != null
    }

    @JvmStatic
    fun shouldHookMouse(): Boolean {
        return !clock.hasTimePassed() && wasNotNull && enabled
    }
}