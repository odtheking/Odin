package me.odinclient.features.impl.dungeon

import me.odinmain.events.impl.GuiEvent
import me.odinmain.events.impl.PacketEvent
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.name
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.inDungeons
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object CloseChest : Module(
    name = "Close Chest",
    description = "Allows you to instantly close chests with any key or automatically."
) {
    private val mode by SelectorSetting("Mode", "Auto", arrayListOf("Auto", "Any Key"), description = "The mode to use, auto will automatically close the chest, any key will make any key input close the chest.")

    @SubscribeEvent
    fun onOpenWindow(event: PacketEvent.Receive) {
        val packet = event.packet as? S2DPacketOpenWindow ?: return
        if (!inDungeons || !packet.windowTitle.unformattedText.noControlCodes.equalsOneOf("Chest", "Large Chest") || mode != 0) return
        mc.netHandler.networkManager.sendPacket(C0DPacketCloseWindow(packet.windowId))
        event.isCanceled = true
    }

    @SubscribeEvent
    fun onInput(event: GuiEvent.KeyPress) {
        if (!inDungeons || mode != 1) return
        val gui = (event.gui as? GuiChest)?.inventorySlots as? ContainerChest ?: return
        if (gui.name.noControlCodes.equalsOneOf("Chest", "Large Chest")) mc.thePlayer?.closeScreen()
    }

    @SubscribeEvent
    fun onMouse(event: GuiEvent.MouseClick) {
        if (!inDungeons || mode != 1) return
        val gui = (event.gui as? GuiChest)?.inventorySlots as? ContainerChest ?: return
        if (gui.name.noControlCodes.equalsOneOf("Chest", "Large Chest")) mc.thePlayer?.closeScreen()
    }
}