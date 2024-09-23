package me.odinclient.features.impl.dungeon

import me.odinmain.events.impl.GuiEvent
import me.odinmain.events.impl.PacketReceivedEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.DualSetting
import me.odinmain.utils.*
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.inDungeons
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object CloseChest : Module(
    name = "Close Chest",
    category = Category.DUNGEON,
    description = "Allows you to close the chest with any key or automatically."
) {
    private val mode: Boolean by DualSetting("Mode", "Auto", "Any Key", description = "The mode to use, auto will automatically close the chest, any key will make any key input close the chest.")

    @SubscribeEvent
    fun onOpenWindow(event: PacketReceivedEvent) {
        val packet = event.packet as? S2DPacketOpenWindow ?: return
        if (!inDungeons || !packet.windowTitle.unformattedText.noControlCodes.equalsOneOf("Chest", "Large Chest") || mode) return
        mc.netHandler.networkManager.sendPacket(C0DPacketCloseWindow(packet.windowId))
        event.isCanceled = true
    }

    @SubscribeEvent
    fun onInput(event: GuiEvent.GuiKeyPressEvent) {
        val gui = event.gui as? GuiChest ?: return
        if (!inDungeons || !mode) return
        if ((gui.inventorySlots as? ContainerChest)?.name?.noControlCodes?.equalsOneOf("Chest", "Large Chest") == true)
            mc.thePlayer?.closeScreen()
    }

    @SubscribeEvent
    fun onMouse(event: GuiEvent.GuiMouseClickEvent) {
        val gui = event.gui as? GuiChest ?: return
        if (!inDungeons || !mode) return
        if ((gui.inventorySlots as? ContainerChest)?.name?.noControlCodes?.equalsOneOf("Chest", "Large Chest") == true)
            mc.thePlayer?.closeScreen()
    }
}