package me.odinclient.features.impl.general

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.odinclient.OdinClient
import me.odinclient.OdinClient.Companion.scope
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.Utils.noControlCodes
import me.odinclient.utils.skyblock.ChatUtils
import me.odinclient.utils.skyblock.ChatUtils.modMessage
import me.odinclient.utils.skyblock.PlayerUtils
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object PartyCommands : Module(
    name = "Party Commands",
    category = Category.GENERAL,
    description = "Party Commands! Use /blacklist to blacklist players from using this module. !help for help."
) {

    @SubscribeEvent
    fun party(event: ClientChatReceivedEvent) {
        val message = event.message.unformattedText.noControlCodes
        val match = Regex("Party > (\\[.+])? ?(.+): !(.+)").find(message) ?: return

        val ign = match.groups[2]?.value
        val msg = match.groups[3]?.value?.lowercase()

        scope.launch {
            delay(150)
            ChatUtils.partyCmdsOptions(msg!!, ign!!)
        }
    }
    @OptIn(DelicateCoroutinesApi::class)
    @SubscribeEvent
    fun dt(event: ClientChatReceivedEvent) {
        val message = event.message.unformattedText.noControlCodes

        if (!message.contains("EXTRA STATS") || ChatUtils.dtPlayer == null) return

        GlobalScope.launch{
            delay(2500)
            PlayerUtils.alert("§c${ChatUtils.dtPlayer} needs downtime")
            ChatUtils.partyMessage("${ChatUtils.dtPlayer} needs downtime")
            ChatUtils.dtPlayer = null
        }
    }

    @SubscribeEvent
    fun joinDungeon(event: ClientChatReceivedEvent) {
        val message = event.message.unformattedText.noControlCodes
        val match = Regex("(Party >) (\\[.+])? ?(.+): !(.+) (.+)").find(message) ?: return

        val msg = match.groups[3]?.value?.lowercase()
        val num = match.groups[4]?.value

        ChatUtils.joinDungeon(msg!!, num!!)
    }
}
