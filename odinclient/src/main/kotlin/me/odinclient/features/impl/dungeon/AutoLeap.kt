package me.odinclient.features.impl.dungeon

import me.odinclient.utils.skyblock.PlayerUtils.useItem
import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.events.impl.GuiLoadedEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.skyblock.ChatCommands.isInBlacklist
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.modMessage
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object AutoLeap : Module(
    name = "Auto Leap",
    description = "Automatically leaps to the player who sent !tp in party chat.",
    category = Category.DUNGEON
) {
    private var opened = false
    private var target: String? = null

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        opened = false
        target = null
    }

    @SubscribeEvent
    fun onChatMessage(event: ChatPacketEvent) {
        if (!DungeonUtils.inDungeons) return
        val playerName = Regex("^Party > ?(?:\\[.+])? (.{0,16}): !tp ?(?:.+)?").find(event.message)?.groups?.get(1)?.value?.lowercase() ?: return
        if (playerName == mc.thePlayer.name || isInBlacklist(playerName)) return
        useItem("leap")
        opened = true
        target = playerName
    }

    @SubscribeEvent
    fun onGuiOpen(event: GuiLoadedEvent) {
        if (!opened || !DungeonUtils.inDungeons) return
        val index = event.gui.inventorySlots.subList(11, 16)
            .indexOfFirst { it?.stack?.displayName?.noControlCodes == target }
            .takeIf { it != -1 } ?: return
        mc.playerController.windowClick(event.gui.windowId, 11 + index, 2, 3, mc.thePlayer)
        modMessage("§rLeaped to $target")
        opened = false
        target = null
    }
}