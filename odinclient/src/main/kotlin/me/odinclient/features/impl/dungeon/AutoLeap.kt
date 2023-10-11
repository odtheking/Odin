package me.odinclient.features.impl.dungeon

import me.odinclient.utils.skyblock.PlayerUtils.clickItemInContainer
import me.odinclient.utils.skyblock.PlayerUtils.useItem
import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.skyblock.ChatCommands.isInBlacklist
import me.odinmain.utils.skyblock.ChatUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraftforge.client.event.GuiOpenEvent
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
        val message = event.message
        val playerName = Regex("^Party > ?(?:\\[.+])? (.{0,16}): !tp ?(?:.+)?").find(message)?.groups?.get(1)?.value?.lowercase() ?: return
        if (playerName == mc.thePlayer.name || isInBlacklist(playerName)) return
        useItem("leap")
        opened = true
        target = playerName

    }

    @SubscribeEvent
    fun onGuiOpen(event: GuiOpenEvent) {
        if (!opened || !DungeonUtils.inDungeons) return
        clickItemInContainer("Spirit Leap", target!!, event )
        ChatUtils.modMessage("§rLeaped to $target")
        opened = false
        target = null
    }
}