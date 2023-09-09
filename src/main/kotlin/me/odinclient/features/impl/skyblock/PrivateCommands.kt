package me.odinclient.features.impl.skyblock

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.odinclient.OdinClient
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.utils.ServerUtils
import me.odinclient.utils.Utils.floor
import me.odinclient.utils.Utils.noControlCodes
import me.odinclient.utils.skyblock.ChatUtils
import me.odinclient.utils.skyblock.ChatUtils.isInBlacklist
import me.odinclient.utils.skyblock.PlayerUtils
import net.minecraft.event.ClickEvent
import net.minecraft.util.ChatComponentText
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.floor

object PrivateCommands : Module(
    name = "Private Commands",
    category = Category.SKYBLOCK,
    description = "Party commands! Use /blacklist to blacklist players from using this module. !help for help."
) {

    private var help: Boolean by BooleanSetting(name = "help", default = true)
    private var coords: Boolean by BooleanSetting(name = "coords", default = true)
    private var odin: Boolean by BooleanSetting(name = "odin", default = true)
    private var boop: Boolean by BooleanSetting(name = "boop", default = true)
    private var cf: Boolean by BooleanSetting(name = "cf", default = true)
    private var eightball: Boolean by BooleanSetting(name = "eightball", default = true)
    private var dice: Boolean by BooleanSetting(name = "dice", default = true)
    private var cat: Boolean by BooleanSetting(name = "cat", default = true)
    private var ping: Boolean by BooleanSetting(name = "ping", default = true)
    private var inv: Boolean by BooleanSetting(name = "inv", default = true)
    private var gm: Boolean by BooleanSetting(name = "gm", default = true)
    private var gn: Boolean by BooleanSetting(name = "gn", default = true)
    private val invite: Boolean by BooleanSetting(name = "invite", default = true)

    @OptIn(DelicateCoroutinesApi::class)
    @SubscribeEvent
    fun private(event: ClientChatReceivedEvent) {
        val message = event.message.unformattedText.noControlCodes
        val match = Regex("From (\\[.+])? ?(.+): !(.+)").find(message) ?: return

        val ign = match.groups[2]?.value
        val msg = match.groups[3]?.value?.lowercase()
        GlobalScope.launch {
            delay(150)
            privateCmdsOptions(msg!!, ign!!)
        }
    }

    private fun privateCmdsOptions(message: String,name: String) {
        if (isInBlacklist(name)) return
        when (message.split(" ")[0]) {
            "help" -> if (help) ChatUtils.privateMessage("Commands: inv, coords, odin, boop, cf, 8ball, dice, cat ,ping",name)
            "coords" -> if (coords) ChatUtils.privateMessage(
                "x: ${PlayerUtils.posX.floor()}, y: ${PlayerUtils.posY.floor()}, z: ${PlayerUtils.posZ.floor()}",
                name
            )
            "odin" -> if (odin) ChatUtils.privateMessage("OdinClient! https://discord.gg/2nCbC9hkxT",name)
            "boop" -> if (boop) ChatUtils.sendChatMessage("/boop $name")
            "cf" -> if (cf) ChatUtils.privateMessage(ChatUtils.flipCoin(),name)
            "8ball" -> if (eightball) ChatUtils.privateMessage(ChatUtils.eightBall(),name)
            "dice" -> if (dice) ChatUtils.privateMessage(ChatUtils.rollDice(),name)
            "cat" -> if (cat) ChatUtils.privateMessage(ChatUtils.catPics(),name)
            "ping" -> if (ping) ChatUtils.privateMessage("Current Ping: ${floor(ServerUtils.averagePing)}ms",name)
            "inv" -> if (inv) ChatUtils.sendCommand("party invite $name")
            "gm" -> if (gm) ChatUtils.privateMessage("Good Morning $name!",name)
            "gn" -> if (gn) ChatUtils.privateMessage("Good Night $name.",name)
            "invite" -> if (invite) {
                OdinClient.mc.thePlayer.playSound("note.pling", 100f, 1f)
                OdinClient.mc.thePlayer.addChatMessage(
                    ChatComponentText("§3Odin§bClient §8»§r Click on this message to invite $name to your party!")
                        .setChatStyle(ChatUtils.createClickStyle(ClickEvent.Action.RUN_COMMAND,"/party invite $name"))
                )
            }
        }
    }

}
