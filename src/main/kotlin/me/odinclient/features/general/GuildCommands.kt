package me.odinclient.features.general

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.odinclient.OdinClient.Companion.config
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.utils.skyblock.ChatUtils
import net.minecraft.util.StringUtils.stripControlCodes
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object GuildCommands {
    @OptIn(DelicateCoroutinesApi::class)
    @SubscribeEvent
    fun guild(event: ClientChatReceivedEvent) {
        if (!config.guildCommands) return

        val message = stripControlCodes(event.message.unformattedText)
        val match = Regex("Guild > (\\[.+])? ?(.+) ?(\\[.+])?: ?(.+)").find(message) ?: return

        val ign = match.groups[2]?.value?.split(" ")?.get(0) // Get rid of guild rank by splitting the string and getting the first word
        val msg = match.groups[4]?.value?.lowercase()
        GlobalScope.launch {
            delay(150)
            ChatUtils.guildCmdsOptions(msg!!, ign!!)
            if (config.guildGM && mc.thePlayer.name !== ign) ChatUtils.autoGM(msg, ign)
        }

    }
}