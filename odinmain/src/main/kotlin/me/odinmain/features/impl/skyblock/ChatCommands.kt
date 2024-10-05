package me.odinmain.features.impl.skyblock

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.dungeon.DungeonRequeue.disableRequeue
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.*
import me.odinmain.utils.skyblock.*
import net.minecraft.event.ClickEvent
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
    private var pt by BooleanSetting(name = "Party transfer (pt)", default = true, description = "Executes the /party transfer command.").withDependency { showSettings }
    private var ping by BooleanSetting(name = "Ping", default = true, description = "Sends your current ping.").withDependency { showSettings }
    private var tps by BooleanSetting(name = "TPS", default = true, description = "Sends the server's current TPS.").withDependency { showSettings }
    private var fps by BooleanSetting(name = "FPS", default = true, description = "Sends your current FPS.").withDependency { showSettings }
    private var dt by BooleanSetting(name = "DT", default = true, description = "Sets a reminder for the end of the run.").withDependency { showSettings }
    private var inv by BooleanSetting(name = "inv", default = true, description = "Invites the player to your party.").withDependency { showSettings }
    private val invite by BooleanSetting(name = "invite", default = true, description = "Invites the player to your party.").withDependency { showSettings }
    private val racism by BooleanSetting(name = "Racism", default = true, description = "Sends a random racism percentage.").withDependency { showSettings }
    private val queDungeons by BooleanSetting(name = "Queue dungeons cmds", default = true, description = "Queue dungeons commands.").withDependency { showSettings }
    private val queKuudra by BooleanSetting(name = "Queue kuudra cmds", default = true, description = "Queue kuudra commands.").withDependency { showSettings }

    private var dtPlayer: String? = null
    private val dtReason = mutableListOf<Pair<String, String>>()
    val blacklist: MutableList<String> by ListSetting("Blacklist", mutableListOf())

    private val messageRegex = Regex("^(?:Party > (\\[.+])? ?(.{1,16}): ?(.+)\$|Guild > (\\[.+])? ?(.{1,16}?)(?= ?\\[| ?: ) ?\\[.+] ?: ?(.+)\$|From (\\[.+])? ?(.{1,16}): ?(.+)\$)")

    init {
        onMessage(Regex(" {29}> EXTRA STATS <|^\\[NPC] Elle: Good job everyone. A hard fought battle come to an end. Let's get out of here before we run into any more trouble!$")) {
            dt()
        }

        onMessage(messageRegex) {
            val chatMessage = messageRegex.find(it) ?: return@onMessage
            val (_, ign, message) = chatMessage.destructured

            if (whitelistOnly != isInBlacklist(ign)) return@onMessage

            val channel = when(it.split(" ")[0]) {
                "Party" -> if (!party) return@onMessage else ChatChannel.PARTY
                "Guild" -> if (!guild) return@onMessage else ChatChannel.GUILD
                "From" -> if (!private) return@onMessage else ChatChannel.PRIVATE
                else -> return@onMessage
            }

            runIn(5) {
                handleChatCommands(message, ign, channel)
            }
        }
    }

    private fun handleChatCommands(message: String, name: String, channel: ChatChannel) {
        val commandsMap = when (channel) {
            ChatChannel.PARTY -> mapOf("coords" to coords, "odin" to odin, "boop" to boop, "cf" to cf, "8ball" to eightball, "dice" to dice, "racism" to racism, "tps" to tps, "warp" to warp, "warptransfer" to warptransfer, "allinvite" to allinvite, "pt" to pt, "dt" to dt, "m" to queDungeons, "f" to queDungeons)
            ChatChannel.GUILD -> mapOf("coords" to coords, "odin" to odin, "boop" to boop, "cf" to cf, "8ball" to eightball, "dice" to dice, "racism" to racism, "ping" to ping, "tps" to tps)
            ChatChannel.PRIVATE -> mapOf("coords" to coords, "odin" to odin, "boop" to boop, "cf" to cf, "8ball" to eightball, "dice" to dice, "racism" to racism, "ping" to ping, "tps" to tps, "inv" to inv, "invite" to invite)
        }

        if (!message.startsWith("!")) return
        when (message.split(" ")[0].drop(1)) {
            "help" -> channelMessage("Commands: ${commandsMap.filterValues { it }.keys.joinToString(", ")}", name, channel)
            "coords" -> if (coords) channelMessage(PlayerUtils.getPositionString(), name, channel)
            "odin" -> if (odin) channelMessage("Odin! https://discord.gg/2nCbC9hkxT", name, channel)
            "boop" -> if (boop) sendChatMessage("/boop ${message.substringAfter("boop ")}")
            "cf" -> if (cf) channelMessage(flipCoin(), name, channel)
            "8ball" -> if (eightball) channelMessage(eightBall(), name, channel)
            "dice" -> if (dice) channelMessage(rollDice(), name, channel)
            "racism" -> if (racism) channelMessage("$name is ${Random.nextInt(1, 101)}% racist. Racism is not allowed!", name, channel)
            "ping" -> if (ping) channelMessage("Current Ping: ${floor(ServerUtils.averagePing).toInt()}ms", name, channel)
            "tps" -> if (tps) channelMessage("Current TPS: ${ServerUtils.averageTps.floor()}", name, channel)
            "fps" -> if (fps) channelMessage("Current FPS: ${ServerUtils.fps}", name, channel)

            // Party cmds only

            "warp" -> if (warp && channel == ChatChannel.PARTY) sendCommand("p warp")

            "warptransfer" ->
                if (warptransfer && channel == ChatChannel.PARTY) {
                    sendCommand("p warp")
                    runIn(12) {
                        sendCommand("p transfer $name")
                    }
                }

            "allinvite" -> if (allinvite && channel == ChatChannel.PARTY) sendCommand("p settings allinvite")

            "pt" -> if (pt && channel == ChatChannel.PARTY) sendCommand("p transfer $name")

            "dt" -> {
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

            "m" -> {
                if (!queDungeons || channel != ChatChannel.PARTY) return
                val floor = message.substringAfter("m ")
                if (message.substringAfter("m ") == message) return modMessage("§cPlease specify a floor.")
                modMessage("§aEntering master mode floor: $floor")
                sendCommand("od m$floor", true)
            }

            "f" -> {
                if (!queDungeons || channel != ChatChannel.PARTY) return
                val floor = message.substringAfter("f ")
                if (message.substringAfter("f ") == message) return modMessage("§cPlease specify a floor.")
                modMessage("§aEntering floor: $floor")
                sendCommand("od f$floor", true)
            }

            "t" -> {
                if (!queKuudra || channel != ChatChannel.PARTY) return
                val tier = message.substringAfter("t ")
                if (message.substringAfter("t ") == message) return modMessage("§cPlease specify a tier.")
                modMessage("§aEntering kuudra run: $tier")
                sendCommand("od t$tier", true)
            }

            // Private cmds only

            "inv" -> if (inv && channel == ChatChannel.PRIVATE) inviteCommand(name)
            "invite" -> if (invite && channel == ChatChannel.PRIVATE) inviteCommand(name)
        }
    }

    private fun inviteCommand(name: String) {
        PlayerUtils.playLoudSound("note.pling", 100f, 1f)
        modMessage("Click on this message to invite $name to your party!",
            chatStyle = createClickStyle(ClickEvent.Action.RUN_COMMAND, "/party invite $name"))
    }

    private fun dt() {
        if (dtReason.isEmpty()) return
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