package me.odinmain.features.impl.skyblock

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.odinmain.config.MiscConfig
import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.ServerUtils
import me.odinmain.utils.floor
import me.odinmain.utils.imgurID
import me.odinmain.utils.skyblock.ChatUtils
import me.odinmain.utils.skyblock.PlayerUtils
import net.minecraft.event.ClickEvent
import net.minecraft.util.ChatComponentText
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.floor
import kotlin.random.Random

object ChatCommands : Module(
    name = "Chat commands",
    category = Category.SKYBLOCK,
    description = "type !help in the corresponding channel for cmd list. Use /blacklist.",
    tag = TagType.NEW
) {
    private var party: Boolean by BooleanSetting(name = "Party cmds", default = true)
    private var guild: Boolean by BooleanSetting(name = "Guild cmds", default = true)
    private var private: Boolean by BooleanSetting(name = "Private cmds", default = true)
    private var showSettings: Boolean by BooleanSetting(name = "Show Settings", default = false)

    private var warp: Boolean by BooleanSetting(name = "Warp", default = true).withDependency { showSettings }
    private var warptransfer: Boolean by BooleanSetting(name = "Warp & pt (warptransfer)", default = true).withDependency { showSettings }
    private var coords: Boolean by BooleanSetting(name = "Coords (coords)", default = true).withDependency { showSettings }
    private var allinvite: Boolean by BooleanSetting(name = "Allinvite", default = true).withDependency { showSettings }
    private var odin: Boolean by BooleanSetting(name = "Odin", default = true).withDependency { showSettings }
    private var boop: Boolean by BooleanSetting(name = "Boop", default = true).withDependency { showSettings }
    private var cf: Boolean by BooleanSetting(name = "Coinflip (cf)", default = true).withDependency { showSettings }
    private var eightball: Boolean by BooleanSetting(name = "Eightball", default = true).withDependency { showSettings }
    private var dice: Boolean by BooleanSetting(name = "Dice", default = true).withDependency { showSettings }
    private var cat: Boolean by BooleanSetting(name = "Cat", default = true).withDependency { showSettings }
    private var pt: Boolean by BooleanSetting(name = "Party transfer (pt)", default = true).withDependency { showSettings }
    private var ping: Boolean by BooleanSetting(name = "Ping", default = true).withDependency { showSettings }
    private var tps: Boolean by BooleanSetting(name = "tps", default = true).withDependency { showSettings }
    private var dt: Boolean by BooleanSetting(name = "Dt", default = true).withDependency { showSettings }
    private var inv: Boolean by BooleanSetting(name = "inv", default = true).withDependency { showSettings }
    private val invite: Boolean by BooleanSetting(name = "invite", default = true).withDependency { showSettings }
    private val racism: Boolean by BooleanSetting(name = "Racism", default = true).withDependency { showSettings }

    private var dtPlayer: String? = null
    var disableReque: Boolean? = false
    private var picture = getCatPic()

    private fun getCatPic(): String {
        return try {
            "https://i.imgur.com/${imgurID("https://api.thecatapi.com/v1/images/search")}.png"
        } catch (e: Exception) {
            "Failed to get a cat pic"
        }
    }

    private fun useCatPic(): String {
        val temp = picture
        picture = getCatPic()
        return temp
    }

    init {
        execute(3000) {
            if (picture == "Failed to get a cat pic") picture = getCatPic()
        }
    }

    private val partyRegex = Regex("Party > (\\[.+])? ?(.+): !(.+)")
    private val guildRegex = Regex("Guild > (\\[.+])? ?(.+) ?(\\[.+])?: !?(.+)")
    private val fromRegex = Regex("From (\\[.+])? ?(.+): !(.+)")

    @OptIn(DelicateCoroutinesApi::class)
    @SubscribeEvent
    fun chatCommands(event: ChatPacketEvent) {
        val message = event.message
        val channel = when {
            partyRegex.matches(message) -> "party"
            guildRegex.matches(message) -> "guild"
            fromRegex.matches(message) -> "private"
            else -> return
        }

        val ign = when (channel) {
            "party" -> partyRegex.matchEntire(message)?.groups?.get(2)?.value
            "guild" -> guildRegex.matchEntire(message)?.groups?.get(2)?.value
            "private" -> fromRegex.matchEntire(message)?.groups?.get(2)?.value
            else -> return
        } ?: return

        if (isInBlacklist(ign)) return

        val msg = when (channel) {
            "party" -> partyRegex.matchEntire(message)?.groups?.get(3)?.value?.lowercase()
            "guild" -> guildRegex.matchEntire(message)?.groups?.get(4)?.value?.lowercase()
            "private" -> fromRegex.matchEntire(message)?.groups?.get(3)?.value?.lowercase()
            else -> ""
        }

        when (channel) {
            "party" -> if (!party) return
            "guild" -> if (!guild) return
            "private" -> if (!private) return
            // Add more cases as needed for other channels
            else -> return // Handle unknown channels, or adjust as needed
        }

        GlobalScope.launch {
            delay(350)
            cmdsAll(msg!!, ign, channel)
        }

    }

    private suspend fun cmdsAll(message: String, name: String, channel: String) {

        val helpMessage = when (channel) {
            "party" -> "Commands: coords, odin, boop, cf, 8ball, dice, cat, racism, ping, tps, warp, warptransfer, allinvite, pt, dt"
            "guild" -> "Commands: coords, odin, boop, cf, 8ball, dice, cat, racism, ping, tps"
            "private" -> "Commands: coords, odin, boop, cf, 8ball, dice, cat, racism, ping, tps, inv, invite"
            else -> ""
        }

        when (message.split(" ")[0]) {
            "help" -> ChatUtils.channelMessage(helpMessage, name, channel)
            "coords" -> if (coords) ChatUtils.channelMessage("x: ${PlayerUtils.getFlooredPlayerCoords().x}, y: ${PlayerUtils.getFlooredPlayerCoords().y}, z: ${PlayerUtils.getFlooredPlayerCoords().z}", name, channel)
            "odin" -> if (odin) ChatUtils.channelMessage("Odin! https://discord.gg/2nCbC9hkxT", name, channel)
            "boop" -> {
                if (boop) {
                    val boopAble = message.substringAfter("boop ")
                    ChatUtils.sendChatMessage("/boop $boopAble") }
            }
            "cf" -> if (cf) ChatUtils.channelMessage(ChatUtils.flipCoin(), name, channel)
            "8ball" -> if (eightball) ChatUtils.channelMessage(ChatUtils.eightBall(), name, channel)
            "dice" -> if (dice) ChatUtils.channelMessage(ChatUtils.rollDice(), name, channel)
            "cat" -> if (cat) ChatUtils.channelMessage(useCatPic(), name, channel)
            "racism" -> if (racism) ChatUtils.channelMessage("$name is ${Random.nextInt(1, 101)}% racist. Racism is not allowed!", name, channel)
            "ping" -> if (ping) ChatUtils.channelMessage("Current Ping: ${floor(ServerUtils.averagePing).toInt()}ms", name, channel)
            "tps" -> if (tps) ChatUtils.channelMessage("Current TPS: ${floor(ServerUtils.averageTps.floor())}", name, channel)

            // Party cmds only

            "warp" -> if (warp && channel == "party") ChatUtils.sendCommand("p warp")
            "warptransfer" -> { if (warptransfer && channel == "party")
                ChatUtils.sendCommand("p warp")
                delay(500)
                ChatUtils.sendCommand("p transfer $name")
            }
            "allinvite" -> if (allinvite && channel == "party") ChatUtils.sendCommand("p settings allinvite")
            "pt" -> if (pt && channel == "party") ChatUtils.sendCommand("p transfer $name")

            "dt" -> if (dt && channel == "party") {
                ChatUtils.modMessage("Reminder set for the end of the run!")
                dtPlayer = name
                disableReque = true
            }

            // Private cmds only

            "inv" -> if (inv && channel == "private") ChatUtils.sendCommand("party invite $name")
            "invite" -> if (invite && channel == "private") {
                mc.thePlayer.playSound("note.pling", 100f, 1f)
                mc.thePlayer.addChatMessage(
                    ChatComponentText("§3Odin§bClient §8»§r Click on this message to invite $name to your party!")
                        .setChatStyle(ChatUtils.createClickStyle(ClickEvent.Action.RUN_COMMAND,"/party invite $name"))
                )
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SubscribeEvent
    fun dt(event: ChatPacketEvent) {
        if (!event.message.contains("EXTRA STATS") || dtPlayer == null) return

        GlobalScope.launch{
            delay(2500)
            PlayerUtils.alert("§c$dtPlayer needs downtime")
            ChatUtils.partyMessage("$dtPlayer needs downtime")
            dtPlayer = null
        }
    }

    fun isInBlacklist(name: String) : Boolean = MiscConfig.blacklist.contains(name.lowercase())


}