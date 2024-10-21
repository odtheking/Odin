package me.odinmain.features.impl.skyblock

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.dungeon.DungeonRequeue.disableRequeue
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.*
import me.odinmain.utils.skyblock.*
import net.minecraft.event.ClickEvent
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.floor
import kotlin.random.Random

object ChatCommands : Module(
    name = "Chat Commands",
    category = Category.SKYBLOCK,
    description = "Type !help in the corresponding channel for cmd list. Use /blacklist.",
) {
    private var party by BooleanSetting(name = "Party commands", default = true, description = "Toggles chat commands in party chat.")
    private var guild by BooleanSetting(name = "Guild commands", default = true, description = "Toggles chat commands in guild chat.")
    private var private by BooleanSetting(name = "Private commands", default = true, description = "Toggles chat commands in private chat.")
    private val whitelistOnly by DualSetting("Whitelist Only", left = "blacklist", right = "Whitelist", default = false, description = "Whether the list should act like a whitelist or a blacklist.")
    private var showSettings by DropdownSetting(name = "Show Settings", default = false)

    private var warp by BooleanSetting(name = "Warp", default = true, description = "Executes the /party warp commnad.").withDependency { showSettings }
    private var warptransfer by BooleanSetting(name = "Warp & pt (warptransfer)", default = true, description = "Executes the /party warp and /party transfer commands.").withDependency { showSettings }
    private var coords by BooleanSetting(name = "Coords (coords)", default = true, description = "Sends your current coordinates.").withDependency { showSettings }
    private var allinvite by BooleanSetting(name = "Allinvite", default = true, description = "Executes the /party settings allinvite command.").withDependency { showSettings }
    private var odin by BooleanSetting(name = "Odin", default = true, description = "Sends the odin discord link.").withDependency { showSettings }
    private var boop by BooleanSetting(name = "Boop", default = true, description = "Executes the /boop command.").withDependency { showSettings }
    private var cf by BooleanSetting(name = "Coinflip (cf)", default = true, description = "Sends the result of a coinflip..").withDependency { showSettings }
    private var eightball by BooleanSetting(name = "Eightball", default = true, description = "Sends a random 8ball response.").withDependency { showSettings }
    private var dice by BooleanSetting(name = "Dice", default = true, description = "Rolls a dice.").withDependency { showSettings }
    private var pt by BooleanSetting(name = "Party transfer (pt)", default = false, description = "Executes the /party transfer command.").withDependency { showSettings }
    private var ping by BooleanSetting(name = "Ping", default = true, description = "Sends your current ping.").withDependency { showSettings }
    private var tps by BooleanSetting(name = "TPS", default = true, description = "Sends the server's current TPS.").withDependency { showSettings }
    private var fps by BooleanSetting(name = "FPS", default = true, description = "Sends your current FPS.").withDependency { showSettings }
    private var dt by BooleanSetting(name = "DT", default = true, description = "Sets a reminder for the end of the run.").withDependency { showSettings }
    private val invite by BooleanSetting(name = "invite", default = true, description = "Invites the player to your party.").withDependency { showSettings }
    private val racism by BooleanSetting(name = "Racism", default = true, description = "Sends a random racism percentage.").withDependency { showSettings }
    private val queInstance by BooleanSetting(name = "Queue instance cmds", default = true, description = "Queue dungeons commands.").withDependency { showSettings }
    private val time by BooleanSetting(name = "Time", default = false, description = "Sends the current time.").withDependency { showSettings }
    private var demote by BooleanSetting(name = "Demote", default = false, description = "Executes the /party demote command.").withDependency { showSettings }
    private var promote by BooleanSetting(name = "Promote", default = false, description = "Executes the /party promote command.").withDependency { showSettings }

    private var dtPlayer: String? = null
    private val dtReason = mutableListOf<Pair<String, String>>()
    val blacklist: MutableList<String> by ListSetting("Blacklist", mutableListOf())

    // https://regex101.com/r/in8hej/6
    private val messageRegex = Regex("^(?:Party > (\\[[^]]*?])? ?(\\w{1,16})(?: [ቾ⚒])?: ?(.+)\$|Guild > (\\[[^]]*?])? ?(\\w{1,16})(?: \\[([^]]*?)])?: ?(.+)\$|From (\\[[^]]*?])? ?(\\w{1,16}): ?(.+)\$)")

    init {
        onMessage(Regex(" {29}> EXTRA STATS <|^\\[NPC] Elle: Good job everyone. A hard fought battle come to an end. Let's get out of here before we run into any more trouble!$")) {
            dt()
        }

        onMessage(messageRegex) {
            val channel = when(it.split(" ")[0]) {
                "Party" -> if (!party) return@onMessage else ChatChannel.PARTY
                "Guild" -> if (!guild) return@onMessage else ChatChannel.GUILD
                "From" -> if (!private) return@onMessage else ChatChannel.PRIVATE
                else -> return@onMessage
            }

            val match = messageRegex.find(it) ?: return@onMessage
            val ign = match.groups[2]?.value ?: match.groups[5]?.value ?: match.groups[9]?.value ?: return@onMessage
            val msg = match.groups[3]?.value ?: match.groups[7]?.value ?: match.groups[10]?.value ?: return@onMessage

            if (whitelistOnly != isInBlacklist(ign)) return@onMessage modMessage("§cPlayer is not in the list!")

            runIn(8) {
                handleChatCommands(msg, ign, channel)
            }
        }
    }

    private fun handleChatCommands(message: String, name: String, channel: ChatChannel) {
        val commandsMap = when (channel) {
            ChatChannel.PARTY -> mapOf (
                "coords" to coords, "odin" to odin, "boop" to boop, "cf" to cf, "8ball" to eightball, "dice" to dice, "racism" to racism, "tps" to tps, "warp" to warp,
                "warptransfer" to warptransfer, "allinvite" to allinvite, "pt" to pt, "dt" to dt, "m?" to queInstance, "f?" to queInstance, "t?" to queInstance, "time" to time,
                "demote" to demote, "promote" to promote
            )
            ChatChannel.GUILD -> mapOf ("coords" to coords, "odin" to odin, "boop" to boop, "cf" to cf, "8ball" to eightball, "dice" to dice, "racism" to racism, "ping" to ping, "tps" to tps, "time" to time)
            ChatChannel.PRIVATE -> mapOf ("coords" to coords, "odin" to odin, "boop" to boop, "cf" to cf, "8ball" to eightball, "dice" to dice, "racism" to racism, "ping" to ping, "tps" to tps, "invite" to invite, "time" to time)
        }

        if (!message.startsWith("!")) return
        when (message.split(" ")[0].drop(1).lowercase()) {
            "help", "h" -> channelMessage("Commands: ${commandsMap.filterValues { it }.keys.joinToString(", ")}", name, channel)
            "coords", "co" -> if (coords) channelMessage(PlayerUtils.getPositionString(), name, channel)
            "odin", "od" -> if (odin) channelMessage("Odin! https://discord.gg/2nCbC9hkxT", name, channel)
            "boop" -> if (boop) sendChatMessage("/boop ${message.substringAfter("boop ")}")
            "cf" -> if (cf) channelMessage(flipCoin(), name, channel)
            "8ball" -> if (eightball) channelMessage(eightBall(), name, channel)
            "dice" -> if (dice) channelMessage(rollDice(), name, channel)
            "racism" -> if (racism) channelMessage("$name is ${Random.nextInt(1, 101)}% racist. Racism is not allowed!", name, channel)
            "ping" -> if (ping) channelMessage("Current Ping: ${floor(ServerUtils.averagePing).toInt()}ms", name, channel)
            "tps" -> if (tps) channelMessage("Current TPS: ${ServerUtils.averageTps.floor()}", name, channel)
            "fps" -> if (fps) channelMessage("Current FPS: ${ServerUtils.fps}", name, channel)
            "time" -> if (time) channelMessage("Current Time: ${ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"))}", name, channel)

            // Party cmds only
            "warp", "w" -> if (warp && channel == ChatChannel.PARTY) sendCommand("p warp")
            "warptransfer", "wt" -> if (warptransfer && channel == ChatChannel.PARTY) {
                sendCommand("p warp")
                runIn(12) {
                    sendCommand("p transfer $name")
                }
            }
            "allinvite", "allinv" -> if (allinvite && channel == ChatChannel.PARTY) sendCommand("p settings allinvite")
            "pt", "ptme" -> if (pt && channel == ChatChannel.PARTY) sendCommand("p transfer $name")
            "downtime", "dt" -> {
                if (!dt || channel != ChatChannel.PARTY) return
                var reason = "No reason given"
                if (message.substringAfter("dt ") != message && !message.substringAfter("dt ").contains("!dt"))
                    reason = message.substringAfter("dt ")
                if (dtReason.any { it.first == name }) return modMessage("§cThat player already has a reminder!")
                dtReason.add(Pair(name, reason))
                modMessage("§aReminder set for the end of the run!")
                dtPlayer = name
                disableRequeue = true
            }
            "f1", "f2", "f3", "f4", "f5", "f6", "f7", "m1", "m2", "m3", "m4", "m5", "m6", "m7", "t1", "t2", "t3", "t4", "t5" -> {
                if (!queInstance || channel != ChatChannel.PARTY) return
                modMessage("§aEntering -> ${message.substring(1).capitalizeFirst()}")
                sendCommand("od ${message.substring(1)}", true)
            }
            "demote" -> if (demote && channel == ChatChannel.PARTY) sendCommand("p demote $name")
            "promote" -> if (promote && channel == ChatChannel.PARTY) sendCommand("p promote $name")

            // Private cmds only
            "invite", "inv" -> if (invite && channel == ChatChannel.PRIVATE) inviteCommand(name)
        }
    }

    private fun inviteCommand(name: String) {
        PlayerUtils.playLoudSound("note.pling", 100f, 1f)
        modMessage("Click on this message to invite $name to your party!",
            chatStyle = createClickStyle(ClickEvent.Action.RUN_COMMAND, "/party invite $name"))
    }

    private fun dt() {
        if (dtReason.isEmpty() || !dt) return
        runIn(30) {
            PlayerUtils.alert("§cPlayers need DT")
            partyMessage("Players need DT: ${dtReason.joinToString(separator = ", ") { (name, reason) -> "$name: $reason" }}")
            dtReason.clear()
        }
    }

    private fun isInBlacklist(name: String) =
        blacklist.contains(name.lowercase())

    enum class ChatChannel {
        PARTY, GUILD, PRIVATE
    }
}