package me.odinmain.features.impl.skyblock

import kotlinx.coroutines.launch
import me.odinmain.OdinMain
import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ListSetting
import me.odinmain.utils.ServerUtils
import me.odinmain.utils.floor
import me.odinmain.utils.imgurID
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.*
import net.minecraft.event.ClickEvent
import net.minecraft.util.ChatComponentText
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.floor
import kotlin.random.Random

object ChatCommands : Module(
    name = "Chat commands",
    category = Category.SKYBLOCK,
    description = "type !help in the corresponding channel for cmd list. Use /blacklist.",
) {
    private var party: Boolean by BooleanSetting(name = "Party commands", default = true, description = "Toggles chat commands in party chat")
    private var guild: Boolean by BooleanSetting(name = "Guild commands", default = true, description = "Toggles chat commands in guild chat")
    private var private: Boolean by BooleanSetting(name = "Private commands", default = true, description = "Toggles chat commands in private chat")
    private var showSettings: Boolean by BooleanSetting(name = "Show Settings", default = false, description = "Shows the settings for chat commands")

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
    private val queDungeons: Boolean by BooleanSetting(name = "Queue dungeons cmds", default = true).withDependency { showSettings }
    private val queKuudra: Boolean by BooleanSetting(name = "Queue kuudra cmds", default = true).withDependency { showSettings }

    private var dtPlayer: String? = null
    var disableRequeue: Boolean? = false
    private val dtReason = mutableListOf<Pair<String, String>>()
    val blacklist: MutableList<String> by ListSetting("Blacklist", mutableListOf())

    private fun getCatPic(): String {
        return try {
            "https://i.imgur.com/${imgurID("https://api.thecatapi.com/v1/images/search")}.png"

        } catch (e: Exception) {
            "imgurID Failed ${e.message}"
        }
    }

    private val partyRegex = Regex("Party > (\\[.+])? ?(.+): (.+)")
    private val guildRegex = Regex("Guild > (\\[.+])? ?(.+) ?(\\[.+])?: ?(.+)")
    private val fromRegex = Regex("From (\\[.+])? ?(.+): (.+)")

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
            else -> return // Handle unknown channels, or adjust as needed
        }

        runIn(6) {
            handleChatCommands(msg!!, ign, channel)
        }
    }

    private fun handleChatCommands(message: String, name: String, channel: String) {

        val helpMessage = when (channel) {
            "party" -> {
                val commandsMap = mapOf("coords" to coords, "odin" to odin, "boop" to boop, "cf" to cf, "8ball" to eightball, "dice" to dice, "cat" to cat, "racism" to racism, "tps" to tps, "warp" to warp, "warptransfer" to warptransfer, "allinvite" to allinvite, "pt" to pt, "dt" to dt, "m" to queDungeons, "f" to queDungeons)
                val enabledCommands = commandsMap.filterValues { it }.keys.joinToString(", ")
                "Commands: $enabledCommands"
            }
            "guild" -> {
                val commandsMap = mapOf("coords" to coords, "odin" to odin, "boop" to boop, "cf" to cf, "8ball" to eightball, "dice" to dice, "cat" to cat, "racism" to racism, "ping" to ping, "tps" to tps)
                val enabledCommands = commandsMap.filterValues { it }.keys.joinToString(", ")
                "Commands: $enabledCommands"
            }
            "private" -> {
                val commandsMap = mapOf("coords" to coords, "odin" to odin, "boop" to boop, "cf" to cf, "8ball" to eightball, "dice" to dice, "cat" to cat, "racism" to racism, "ping" to ping, "tps" to tps, "inv" to inv, "invite" to invite)
                val enabledCommands = commandsMap.filterValues { it }.keys.joinToString(", ")
                "Commands: $enabledCommands"
            }
            else -> ""
        }

        if (!message.startsWith("!")) return
        when (message.split(" ")[0].drop(1)) {
            "help" -> channelMessage(helpMessage, name, channel)
            "coords" -> if (coords) channelMessage("x: ${PlayerUtils.posX.toInt()}, y: ${PlayerUtils.posY.toInt()}, z: ${PlayerUtils.posZ.toInt()}", name, channel)
            "odin" -> if (odin) channelMessage("Odin! https://discord.gg/2nCbC9hkxT", name, channel)
            "boop" -> {
                if (boop) {
                    val boopAble = message.substringAfter("boop ")
                    sendChatMessage("/boop $boopAble") }
            }
            "cf" -> if (cf) channelMessage(flipCoin(), name, channel)
            "8ball" -> if (eightball) channelMessage(eightBall(), name, channel)
            "dice" -> if (dice) channelMessage(rollDice(), name, channel)
            "cat" -> if (cat) {
                modMessage("§aFetching cat picture...")
                OdinMain.scope.launch {
                    channelMessage(getCatPic(), name, channel)
                }
            }
            "racism" -> if (racism) channelMessage("$name is ${Random.nextInt(1, 101)}% racist. Racism is not allowed!", name, channel)
            "ping" -> if (ping) channelMessage("Current Ping: ${floor(ServerUtils.averagePing).toInt()}ms", name, channel)
            "tps" -> if (tps) channelMessage("Current TPS: ${floor(ServerUtils.averageTps.floor())}", name, channel)

            // Party cmds only

            "warp" -> if (warp && channel == "party") sendCommand("p warp")
            "warptransfer" -> { if (warptransfer)
                sendCommand("p warp")
                runIn(12) {
                    sendCommand("p transfer $name")
                }
            }
            "allinvite" -> if (allinvite && channel == "party") sendCommand("p settings allinvite")
            "pt" -> if (pt && channel == "party") sendCommand("p transfer $name")

            "dt" -> if (dt && channel == "party") {
                var reason = "No reason given"
                if (message.substringAfter("dt ") != message && !message.substringAfter("dt ").contains("!dt"))
                    reason = message.substringAfter("dt ")
                if (dtReason.any { it.first == name }) return modMessage("§cThat player already has a reminder!")
                dtReason.add(Pair(name, reason))
                modMessage("§aReminder set for the end of the run!")
                dtPlayer = name
                disableRequeue = true
            }

            "m" -> {
                if (!queDungeons) return
                val floor = message.substringAfter("m ")
                if (message.substringAfter("m ") == message) return modMessage("§cPlease specify a floor.")
                if (floor.toIntOrNull() == null) return modMessage("§cPlease specify a valid floor.")
                modMessage("§aEntering master mode floor: $floor")
                sendCommand("od m$floor", true)
            }

            "f" -> {
                if (!queDungeons) return
                val floor = message.substringAfter("f ")
                if (message.substringAfter("f ") == message) return modMessage("§cPlease specify a floor.")
                if (floor.toIntOrNull() == null) return modMessage("§cPlease specify a valid floor.")
                modMessage("§aEntering floor: $floor")
                sendCommand("od f$floor", true)
            }

            "t" -> {
                if(!queKuudra) return
                val tier = message.substringAfter("t ")
                if (message.substringAfter("t ") == message) return modMessage("§cPlease specify a tier.")
                if (tier.toIntOrNull() == null) return modMessage("§cPlease specify a valid tier.")
                modMessage("§aEntering kuudra run: $tier")
                sendCommand("od t$tier", true)
            }

            // Private cmds only

            "inv" -> if (inv && channel == "private") inviteCommand(name)
            "invite" -> if (invite && channel == "private") inviteCommand(name)
        }
    }

    private fun inviteCommand(name: String) {
        mc.thePlayer.playSound("note.pling", 100f, 1f)
        mc.thePlayer.addChatMessage(
            ChatComponentText("§3Odin§bClient §8»§r Click on this message to invite $name to your party!")
                .setChatStyle(createClickStyle(ClickEvent.Action.RUN_COMMAND,"/party invite $name"))
        )
    }

    @SubscribeEvent
    fun dt(event: ChatPacketEvent) {
        if (!event.message.contains("EXTRA STATS") || dtPlayer == null) return

        runIn(30) {
            PlayerUtils.alert("§cPlayers need DT")
            partyMessage("Players need DT: ${dtReason.joinToString(separator = ", ") { (name, reason) ->
                "$name: $reason" }}")
            dtPlayer = null
            dtReason.clear()
        }
    }

    private fun isInBlacklist(name: String) : Boolean = blacklist.contains(name.lowercase())
}