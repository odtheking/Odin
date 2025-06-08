package me.odinmain.features.impl.skyblock

import me.odinmain.events.impl.MessageSentEvent
import me.odinmain.features.Module
import me.odinmain.features.impl.dungeon.DungeonRequeue.disableRequeue
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.DropdownSetting
import me.odinmain.features.settings.impl.ListSetting
import me.odinmain.utils.ServerUtils
import me.odinmain.utils.capitalizeFirst
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.*
import net.minecraft.event.ClickEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.math.floor
import kotlin.random.Random

object ChatCommands : Module(
    name = "Chat Commands",
    desc = "Type !help in the corresponding channel for cmd list. Use /chatclist.",
) {
    private val chatEmotes by BooleanSetting("Chat Emotes", true, desc = "Replaces chat emotes with their corresponding emojis.")
    private val party by BooleanSetting("Party commands", true, desc = "Toggles chat commands in party chat.")
    private val guild by BooleanSetting("Guild commands", true, desc = "Toggles chat commands in guild chat.")
    private val private by BooleanSetting("Private commands", true, desc = "Toggles chat commands in private chat.")
    private val whitelistOnly by BooleanSetting("Whitelist Only", false, desc = "Whether the list should act like a whitelist or a blacklist.")
    private val showSettings by DropdownSetting("Show Settings", false)

    private val warp by BooleanSetting("Warp", true, desc = "Executes the /party warp commnad.").withDependency { showSettings }
    private val warptransfer by BooleanSetting("Warp & pt (warptransfer)", true, desc = "Executes the /party warp and /party transfer commands.").withDependency { showSettings }
    private val coords by BooleanSetting("Coords (coords)", true, desc = "Sends your current coordinates.").withDependency { showSettings }
    private val allinvite by BooleanSetting("Allinvite", true, desc = "Executes the /party settings allinvite command.").withDependency { showSettings }
    private val odin by BooleanSetting("Odin", true, desc = "Sends the odin discord link.").withDependency { showSettings }
    private val boop by BooleanSetting("Boop", true, desc = "Executes the /boop command.").withDependency { showSettings }
    private val kick by BooleanSetting("Kick", true, desc = "Executes the /p kick command.").withDependency { showSettings }
    private val cf by BooleanSetting("Coinflip (cf)", true, desc = "Sends the result of a coinflip..").withDependency { showSettings }
    private val eightball by BooleanSetting("Eightball", true, desc = "Sends a random 8ball response.").withDependency { showSettings }
    private val dice by BooleanSetting("Dice", true, desc = "Rolls a dice.").withDependency { showSettings }
    private val pt by BooleanSetting("Party transfer (pt)", false, desc = "Executes the /party transfer command.").withDependency { showSettings }
    private val ping by BooleanSetting("Ping", true, desc = "Sends your current ping.").withDependency { showSettings }
    private val tps by BooleanSetting("TPS", true, desc = "Sends the server's current TPS.").withDependency { showSettings }
    private val fps by BooleanSetting("FPS", true, desc = "Sends your current FPS.").withDependency { showSettings }
    private val dt by BooleanSetting("DT", true, desc = "Sets a reminder for the end of the run.").withDependency { showSettings }
    private val invite by BooleanSetting("Invite", true, desc = "Invites the player to your party.").withDependency { showSettings }
    private val autoConfirm by BooleanSetting("Auto Confirm", false, desc = "Removes the need to confirm a party invite with the !invite command.").withDependency { showSettings && invite }
    private val racism by BooleanSetting("Racism", false, desc = "Sends a random racism percentage.").withDependency { showSettings }
    private val queInstance by BooleanSetting("Queue instance cmds", true, desc = "Queue dungeons commands.").withDependency { showSettings }
    private val time by BooleanSetting("Time", false, desc = "Sends the current time.").withDependency { showSettings }
    private val demote by BooleanSetting("Demote", false, desc = "Executes the /party demote command.").withDependency { showSettings }
    private val promote by BooleanSetting("Promote", false, desc = "Executes the /party promote command.").withDependency { showSettings }
    private val location by BooleanSetting("Location", true, desc = "Sends your current location.").withDependency { showSettings }
    private val holding by BooleanSetting("Holding", true, desc = "Sends the item you are holding.").withDependency { showSettings }

    private val dtReason = mutableListOf<Pair<String, String>>()
    val blacklist: MutableList<String> by ListSetting("Blacklist", mutableListOf())

    // https://regex101.com/r/in8hej/6
    private val messageRegex = Regex("^(?:Party > (\\[[^]]*?])? ?(\\w{1,16})(?: [ቾ⚒])?: ?(.+)\$|Guild > (\\[[^]]*?])? ?(\\w{1,16})(?: \\[([^]]*?)])?: ?(.+)\$|From (\\[[^]]*?])? ?(\\w{1,16}): ?(.+)\$)")

    init {
        onMessage(Regex(" {29}> EXTRA STATS <|^\\[NPC] Elle: Good job everyone. A hard fought battle come to an end. Let's get out of here before we run into any more trouble!$")) {
            if (!dt || dtReason.isEmpty()) return@onMessage
            runIn(30) {
                dtReason.find { it.first == mc.thePlayer.name }?.let { partyMessage("Downtime needed: ${it.second}") }
                modMessage("DT Reasons: ${dtReason.groupBy({ it.second }, { it.first }).entries.joinToString(separator = ", ") { (reason, names) -> "${names.joinToString(", ")}: $reason" }}")
                PlayerUtils.alert("§cPlayers need DT")
                dtReason.clear()
            }
        }

        onMessage(messageRegex) {
            val channel = when(it.value.split(" ")[0]) {
                "From" -> if (!private) return@onMessage else ChatChannel.PRIVATE
                "Party" -> if (!party)  return@onMessage else ChatChannel.PARTY
                "Guild" -> if (!guild)  return@onMessage else ChatChannel.GUILD
                else -> return@onMessage
            }

            val ign = it.groups[2]?.value ?: it.groups[5]?.value ?: it.groups[9]?.value ?: return@onMessage
            val msg = it.groups[3]?.value ?: it.groups[7]?.value ?: it.groups[10]?.value ?: return@onMessage

            if (whitelistOnly != isInBlacklist(ign) || !msg.startsWith("!")) return@onMessage

            runIn(5) { handleChatCommands(msg, ign, channel) }
        }

        onWorldLoad { dtReason.clear() }
    }

    private fun handleChatCommands(message: String, name: String, channel: ChatChannel) {
        val commandsMap = when (channel) {
            ChatChannel.PARTY -> mapOf (
                "coords" to coords, "odin" to odin, "boop" to boop, "kick" to kick, "cf" to cf, "8ball" to eightball, "dice" to dice, "racism" to racism, "tps" to tps, "warp" to warp,
                "warptransfer" to warptransfer, "allinvite" to allinvite, "pt" to pt, "dt" to dt, "m?" to queInstance, "f?" to queInstance, "t?" to queInstance, "time" to time,
                "demote" to demote, "promote" to promote
            )
            ChatChannel.GUILD -> mapOf ("coords" to coords, "odin" to odin, "boop" to boop, "cf" to cf, "8ball" to eightball, "dice" to dice, "racism" to racism, "ping" to ping, "tps" to tps, "time" to time)
            ChatChannel.PRIVATE -> mapOf ("coords" to coords, "odin" to odin, "boop" to boop, "cf" to cf, "8ball" to eightball, "dice" to dice, "racism" to racism, "ping" to ping, "tps" to tps, "invite" to invite, "time" to time)
        }

        val words = message.drop(1).split(" ").map { it.lowercase() }

        when (words[0]) {
            "help", "h" -> channelMessage("Commands: ${commandsMap.filterValues { it }.keys.joinToString(", ")}", name, channel)
            "coords", "co" -> if (coords) channelMessage(PlayerUtils.getPositionString(), name, channel)
            "odin", "od" -> if (odin) channelMessage("Odin! https://discord.gg/2nCbC9hkxT", name, channel)
            "boop" -> if (boop) words.getOrNull(1)?.let { sendCommand("boop $it") }
            "cf" -> if (cf) channelMessage(if (Math.random() < 0.5) "heads" else "tails", name, channel)
            "8ball" -> if (eightball) channelMessage(responses.random(), name, channel)
            "dice" -> if (dice) channelMessage((1..6).random(), name, channel)
            "racism" -> if (racism) channelMessage("$name is ${Random.nextInt(1, 101)}% racist. Racism is not allowed!", name, channel)
            "ping" -> if (ping) channelMessage("Current Ping: ${floor(ServerUtils.averagePing).toInt()}ms", name, channel)
            "tps" -> if (tps) channelMessage("Current TPS: ${ServerUtils.averageTps.toInt()}", name, channel)
            "fps" -> if (fps) channelMessage("Current FPS: ${mc.debug.split(" ")[0].toIntOrNull() ?: 0}", name, channel)
            "time" -> if (time) channelMessage("Current Time: ${ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"))}", name, channel)
            "location" -> if (location) channelMessage("Current Location: ${LocationUtils.currentArea.displayName}", name, channel)
            "holding" -> if (holding) channelMessage("Holding: ${mc.thePlayer?.heldItem?.displayName?.noControlCodes ?: "Nothing :("}", name, channel)

            // Party cmds only
            "warp", "w" -> if (warp && channel == ChatChannel.PARTY) sendCommand("p warp")
            "warptransfer", "wt" -> if (warptransfer && channel == ChatChannel.PARTY) {
                sendCommand("p warp")
                runIn(12) {
                    sendCommand("p transfer $name")
                }
            }
            "allinvite", "allinv" -> if (allinvite && channel == ChatChannel.PARTY) sendCommand("p settings allinvite")
            "pt", "ptme", "transfer" -> if (pt && channel == ChatChannel.PARTY) sendCommand("p transfer $name")
            "downtime", "dt" -> {
                if (!dt || channel != ChatChannel.PARTY) return
                val reason = words.getOrNull(1)?.takeIf { it.isNotBlank() } ?: "No reason given"
                if (dtReason.any { it.first == name }) return modMessage("§6${name} §calready has a reminder!")
                modMessage("§aReminder set for the end of the run! §7(disabled auto requeue for this run)")
                dtReason.add(name to reason)
                disableRequeue = true
            }
            "undowntime", "undt" -> {
                if (!dt || channel != ChatChannel.PARTY) return
                if (dtReason.none { it.first == name }) return modMessage("§6${name} §chas no reminder set!")
                modMessage("§aReminder removed!")
                dtReason.removeIf { it.first == name }
                if (dtReason.isEmpty()) disableRequeue = false
            }
            "f1", "f2", "f3", "f4", "f5", "f6", "f7", "m1", "m2", "m3", "m4", "m5", "m6", "m7", "t1", "t2", "t3", "t4", "t5" -> {
                if (!queInstance || channel != ChatChannel.PARTY) return
                modMessage("§8Entering -> §e${words[0].capitalizeFirst()}")
                sendCommand("od ${words[0].lowercase()}", true)
            }
            "demote" -> if (demote && channel == ChatChannel.PARTY) sendCommand("p demote $name")
            "promote" -> if (promote && channel == ChatChannel.PARTY) sendCommand("p promote $name")
            "kick", "k" -> if (kick && channel == ChatChannel.PARTY) words.getOrNull(1)?.let { sendCommand("p kick $it") }

            // Private cmds only
            "invite", "inv" -> if (invite && channel == ChatChannel.PRIVATE) {
                if (autoConfirm) return sendCommand("p invite $name")
                modMessage("§aClick on this message to invite $name to your party!", chatStyle = createClickStyle(ClickEvent.Action.RUN_COMMAND, "/party invite $name"))
                PlayerUtils.playLoudSound("note.pling", 100f, 1f)
            }
        }
    }

    @SubscribeEvent
    fun onMessageSent(event: MessageSentEvent) {
        if (!chatEmotes ||( event.message.startsWith("/") && !listOf("/pc", "/ac", "/gc", "/msg", "/w", "/r").any { event.message.startsWith(it) })) return

        var replaced = false
        val words = event.message.split(" ").toMutableList()

        for (i in words.indices) {
            replacements[words[i]]?.let {
                replaced = true
                words[i] = it
            }
        }

        if (!replaced) return

        event.isCanceled = true
        sendChatMessage(words.joinToString(" "))
    }

    private val replacements = mapOf(
        "<3" to "❤",
        "o/" to "( ﾟ◡ﾟ)/",
        ":star:" to "✮",
        ":yes:" to "✔",
        ":no:" to "✖",
        ":java:" to "☕",
        ":arrow:" to "➜",
        ":shrug:" to "¯\\_(\u30c4)_/¯",
        ":tableflip:" to "(╯°□°）╯︵ ┻━┻",
        ":totem:" to "☉_☉",
        ":typing:" to "✎...",
        ":maths:" to "√(π+x)=L",
        ":snail:" to "@'-'",
        "ez" to "ｅｚ",
        ":thinking:" to "(0.o?)",
        ":gimme:" to "༼つ◕_◕༽つ",
        ":wizard:" to "('-')⊃━☆ﾟ.*･｡ﾟ",
        ":pvp:" to "⚔",
        ":peace:" to "✌",
        ":puffer:" to "<('O')>",
        "h/" to "ヽ(^◇^*)/",
        ":sloth:" to "(・⊝・)",
        ":dog:" to "(ᵔᴥᵔ)",
        ":dj:" to "ヽ(⌐■_■)ノ♬",
        ":yey:" to "ヽ (◕◡◕) ﾉ",
        ":snow:" to "☃",
        ":dab:" to "<o/",
        ":cat:" to "= ＾● ⋏ ●＾ =",
        ":cute:" to "(✿◠‿◠)",
        ":skull:" to "☠",
        ":bum:" to "♿"
    )

    private fun isInBlacklist(name: String) =
        blacklist.contains(name.lowercase())

    enum class ChatChannel {
        PARTY, GUILD, PRIVATE
    }

    private val responses = arrayOf(
        "It is certain", "It is decidedly so", "Without a doubt",
        "Yes definitely", "You may rely on it", "As I see it, yes",
        "Most likely", "Outlook good", "Yes", "Signs point to yes",
        "Reply hazy try again", "Ask again later", "Better not tell you now",
        "Cannot predict now", "Concentrate and ask again", "Don't count on it",
        "My reply is no", "My sources say no", "Outlook not so good", "Very doubtful"
    )
}
