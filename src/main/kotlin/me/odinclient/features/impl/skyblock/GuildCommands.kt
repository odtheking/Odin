package me.odinclient.features.impl.skyblock

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.odinclient.events.impl.ChatPacketEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.utils.ServerUtils
import me.odinclient.utils.Utils.floor
import me.odinclient.utils.Utils.noControlCodes
import me.odinclient.utils.skyblock.ChatUtils
import me.odinclient.utils.skyblock.ChatUtils.isInBlacklist
import me.odinclient.utils.skyblock.PlayerUtils
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.floor

object GuildCommands : Module(
    name = "Guild Commands",
    category = Category.SKYBLOCK,
    description = "Guild commands! Use /blacklist to blacklist players from using this module. !help for help."
) {
    private val guildGM: Boolean by BooleanSetting("Guild GM")

    private var help: Boolean by BooleanSetting(name = "help", default = true)
    private var coords: Boolean by BooleanSetting(name = "coords", default = true)
    private var odin: Boolean by BooleanSetting(name = "odin", default = true)
    private var boop: Boolean by BooleanSetting(name = "boop", default = true)
    private var cf: Boolean by BooleanSetting(name = "cf", default = true)
    private var eightball: Boolean by BooleanSetting(name = "eightball", default = true)
    private var dice: Boolean by BooleanSetting(name = "dice", default = true)
    private var cat: Boolean by BooleanSetting(name = "cat", default = true)
    private var ping: Boolean by BooleanSetting(name = "ping", default = true)
    private var gm: Boolean by BooleanSetting(name = "gm", default = true)
    private var gn: Boolean by BooleanSetting(name = "gn", default = true)

    @OptIn(DelicateCoroutinesApi::class)
    @SubscribeEvent
    fun guild(event: ChatPacketEvent) {
        val match = Regex("Guild > (\\[.+])? ?(.+) ?(\\[.+])?: ?(.+)").find(event.message) ?: return

        val ign = match.groups[2]?.value?.split(" ")?.get(0) // Get rid of guild rank by splitting the string and getting the first word
        val msg = match.groups[4]?.value?.lowercase()
        GlobalScope.launch {
            delay(150)
            guildCmdsOptions(msg!!, ign!!)
            if (guildGM && mc.thePlayer.name !== ign) ChatUtils.autoGM(msg, ign)
        }

    }

    private fun guildCmdsOptions(message: String,name: String) {
        if (isInBlacklist(name)) return
        if (!message.startsWith("!")) return
        when (message.split(" ")[0].drop(1)) {
            "help" -> if (help) ChatUtils.guildMessage("Commands: coords, odin, boop, cf, 8ball, dice, cat, ping")
            "coords" -> if (coords) ChatUtils.guildMessage(
                "x: ${PlayerUtils.posX.floor()}, y: ${PlayerUtils.posY.floor()}, z: ${PlayerUtils.posZ.floor()}"
            )
            "odin" -> if (odin) ChatUtils.guildMessage("OdinClient! https://discord.gg/2nCbC9hkxT")
            "boop" -> if (boop) ChatUtils.sendChatMessage("/boop $name")
            "cf" -> if (cf) ChatUtils.guildMessage(ChatUtils.flipCoin())
            "8ball" -> if (eightball) ChatUtils.guildMessage(ChatUtils.eightBall())
            "dice" -> if (dice) ChatUtils.guildMessage(ChatUtils.rollDice())
            "cat" -> if (cat) ChatUtils.guildMessage("https://i.imgur.com/${ChatUtils.catPics()}.png")
            "ping" -> if (ping) ChatUtils.guildMessage("Current Ping: ${floor(ServerUtils.averagePing)}ms")

        }
    }

}