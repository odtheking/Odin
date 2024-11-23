package me.odinclient.features.impl.dungeon

import me.odinmain.events.impl.PacketEvent
import me.odinmain.features.Category
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
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Mouse

object CloseChest : Module(
    name = "Close Chest",
    category = Category.DUNGEON,
    description = "Allows you to close the chest with any key or automatically."
) {
    private val mode by SelectorSetting("Mode", "Auto", arrayListOf("Auto", "Any Key"), description = "The mode to use, auto will automatically close the chest, any key will make any key input close the chest.")

    @SubscribeEvent
    fun onOpenWindow(event: PacketEvent.Receive) {
        val packet = event.packet as? S2DPacketOpenWindow ?: return
        if (!inDungeons || !packet.windowTitle.unformattedText.noControlCodes.equalsOneOf("Chest", "Large Chest") || mode == 0) return
        mc.netHandler.networkManager.sendPacket(C0DPacketCloseWindow(packet.windowId))
        event.isCanceled = true
    }

    @SubscribeEvent
    fun onInput(event: GuiScreenEvent.KeyboardInputEvent.Pre) {
        val gui = event.gui as? GuiChest ?: return
        if (!inDungeons || mode != 1) return
        if ((gui.inventorySlots as? ContainerChest)?.name?.noControlCodes?.equalsOneOf("Chest", "Large Chest") == true)
            mc.thePlayer?.closeScreen()
    }

    @SubscribeEvent
    fun onMouse(event: GuiScreenEvent.MouseInputEvent.Pre) {
        val gui = event.gui as? GuiChest ?: return
        if (!inDungeons || mode != 1 || !Mouse.getEventButtonState()) return
        if ((gui.inventorySlots as? ContainerChest)?.name?.noControlCodes?.equalsOneOf("Chest", "Large Chest") == true)
            mc.thePlayer?.closeScreen()
    }
}