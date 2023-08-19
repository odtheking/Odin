package me.odinclient.features.impl.skyblock

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.OdinClient.Companion.scope
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.utils.Utils.noControlCodes
import me.odinclient.utils.skyblock.ChatUtils
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object GuildCommands : Module(
    name = "Guild Commands",
    category = Category.SKYBLOCK,
    description = ""
) {
    private val guildGM: Boolean by BooleanSetting("Guild GM")

    @SubscribeEvent
    fun guild(event: ClientChatReceivedEvent) {
        val message = event.message.unformattedText.noControlCodes
        val match = Regex("Guild > (\\[.+])? ?(.+) ?(\\[.+])?: ?(.+)").find(message) ?: return

        val ign = match.groups[2]?.value?.split(" ")?.get(0) // Get rid of guild rank by splitting the string and getting the first word
        val msg = match.groups[4]?.value?.lowercase()
        scope.launch {
            delay(150)
            ChatUtils.guildCmdsOptions(msg!!, ign!!)
            if (guildGM && mc.thePlayer.name !== ign) ChatUtils.autoGM(msg, ign)
        }

    }
}