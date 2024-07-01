package me.odinclient.features.impl.dungeon

import me.odinmain.events.impl.GuiEvent
import me.odinmain.events.impl.PacketReceivedEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.DualSetting
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.name
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.inDungeons
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object CloseChest : Module(
    "Close Chest",
    category = Category.DUNGEON,
    description = "Options to close the chest automatically or with any key or automatically."
) {
    private val mode: Boolean by DualSetting("Mode", "Auto", "Any Key", description = "The mode to use, auto will automatically close the chest, any key will make any key input close the chest.")

    @SubscribeEvent
    fun onOpenWindow(event: PacketReceivedEvent) {
        if (!inDungeons || event.packet !is S2DPacketOpenWindow || !(event.packet as S2DPacketOpenWindow).windowTitle.unformattedText.equalsOneOf("Chest", "Large Chest", "§4Chest", "§4Large Chest") || mode) return
        mc.netHandler.networkManager.sendPacket(C0DPacketCloseWindow((event.packet as S2DPacketOpenWindow).windowId))
        event.isCanceled = true
    }

    @SubscribeEvent
    fun onInput(event: GuiEvent.GuiKeyPressEvent) {
        if (!inDungeons || !mode || event.gui !is GuiChest) return
        if (((event.gui as? GuiChest)?.inventorySlots as? ContainerChest)?.name.equalsOneOf("Chest", "Large Chest", "§4Chest", "§4Large Chest"))
            mc.thePlayer.closeScreen()
    }

    @SubscribeEvent
    fun onMouse(event: GuiEvent.GuiMouseClickEvent) {
        if (!inDungeons || !mode || event.gui !is GuiChest) return
        if (((event.gui as? GuiChest)?.inventorySlots as? ContainerChest)?.name.equalsOneOf("Chest", "Large Chest", "§4Chest", "§4Large Chest"))
            mc.thePlayer.closeScreen()
    }
}