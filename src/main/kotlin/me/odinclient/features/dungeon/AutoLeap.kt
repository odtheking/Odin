package me.odinclient.features.dungeon

import me.odinclient.OdinClient.Companion.config
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.general.BlackList
import me.odinclient.utils.Utils.noControlCodes
import me.odinclient.utils.skyblock.ChatUtils
import me.odinclient.utils.skyblock.PlayerUtils
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object AutoLeap {

    private var opened = false
    private var target: String? = null

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        opened = false
        target = null
    }

    @SubscribeEvent
    fun onClientChatReceived(event: ClientChatReceivedEvent) {
        if (!config.autoLeap || !DungeonUtils.inDungeons) return
        val message = event.message.unformattedText.noControlCodes
        val playerName = Regex("^Party > ?(?:\\[.+\\])? (.{0,16}): !tp ?(?:.+)?").find(message)?.groups?.get(1)?.value?.lowercase() ?: return
        if (playerName == mc.thePlayer.name || BlackList.isInBlacklist(playerName)) return
        PlayerUtils.useItem("leap")
        opened = true
        target = playerName

    }

    @SubscribeEvent
    fun autoLeap(event: GuiOpenEvent) {
        if (!opened || !config.autoLeap || !DungeonUtils.inDungeons) return
        PlayerUtils.clickItemInContainer("Spirit Leap", target!!, event )
        ChatUtils.modMessage("§rLeaped to $target")
        opened = false
        target = null
    }
}