package me.odinclient.features.impl.dungeon

import me.odinclient.events.impl.ReceivePacketEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.DualSetting
import me.odinclient.utils.Utils.equalsOneOf
import me.odinclient.utils.Utils.name
import me.odinclient.utils.skyblock.ChatUtils.modMessage
import me.odinclient.utils.skyblock.dungeon.DungeonUtils.inDungeons
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.ContainerChest
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent

object CloseChest : Module(
    "Cancel Chest Open",
    category = Category.DUNGEON,
    description = "Cancels the opening of chests in dungeons.",
    tag = TagType.NEW
) {
    private val mode: Boolean by DualSetting("Mode", "Auto", "Any Key", description = "The mode to use, auto will automatically close the chest, any key will make any key input close the chest.")

    @SubscribeEvent
    fun onOpenWindow(event: ReceivePacketEvent) {
        if (!inDungeons || event.packet !is S2DPacketOpenWindow || !event.packet.windowTitle.unformattedText.equalsOneOf("Chest", "Large Chest") || mode) return
        mc.netHandler.networkManager.sendPacket(C0DPacketCloseWindow(event.packet.windowId))
        event.isCanceled = true
    }

    @SubscribeEvent
    fun onInput(event: GuiScreenEvent.KeyboardInputEvent) {
        if (!inDungeons || !mode || event.gui !is GuiChest) return
        if (((event.gui as? GuiChest)?.inventorySlots as? ContainerChest)?.name.equalsOneOf("Chest", "Large Chest")) {
            mc.thePlayer.closeScreen()
        }
    }

    @SubscribeEvent
    fun onMouse(event: GuiScreenEvent.MouseInputEvent) {
        if (!inDungeons || !mode || event.gui !is GuiChest) return
        if (((event.gui as? GuiChest)?.inventorySlots as? ContainerChest)?.name.equalsOneOf("Chest", "Large Chest")) {
            mc.thePlayer.closeScreen()
        }
    }
}