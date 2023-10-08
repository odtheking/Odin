package me.odinclient.features.impl.skyblock

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.odinclient.ModCore
import me.odinclient.events.impl.ChatPacketEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.Setting.Companion.withDependency
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.utils.AutoSessionID
import me.odinclient.utils.ServerUtils
import me.odinclient.utils.Utils.floor
import me.odinclient.utils.WebUtils
import me.odinclient.utils.skyblock.ChatUtils
import me.odinclient.utils.skyblock.ChatUtils.isInBlacklist
import me.odinclient.utils.skyblock.PlayerUtils
import net.minecraft.event.ClickEvent
import net.minecraft.util.ChatComponentText
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.floor

object PartyCommands : Module(
    name = "Chat commands",
    category = Category.SKYBLOCK,
    description = "type !help in the corresponding channel for cmd list. Use /blacklist.",
) {
    private var party: Boolean by BooleanSetting(name = "Party cmds", default = true)
    private var guild: Boolean by BooleanSetting(name = "Guild cmds", default = true)
    private var private: Boolean by BooleanSetting(name = "Private cmds", default = true)

    private var warp: Boolean by BooleanSetting(name = "Warp", default = true)
    private var warptransfer: Boolean by BooleanSetting(name = "Warp & pt (warptransfer)", default = true)
    private var coords: Boolean by BooleanSetting(name = "Coords (coords)", default = true)
    private var allinvite: Boolean by BooleanSetting(name = "Allinvite", default = true)
    private var odin: Boolean by BooleanSetting(name = "Odin", default = true)
    private var boop: Boolean by BooleanSetting(name = "Boop", default = true)
    private var cf: Boolean by BooleanSetting(name = "Coinflip (cf)", default = true)
    private var eightball: Boolean by BooleanSetting(name = "Eightball", default = true)
    private var dice: Boolean by BooleanSetting(name = "Dice", default = true)
    private var cat: Boolean by BooleanSetting(name = "Cat", default = true)
    private var pt: Boolean by BooleanSetting(name = "Party transfer (pt)", default = true)
    private var rat: Boolean by BooleanSetting(name = "Rat", default = true)
    private var ping: Boolean by BooleanSetting(name = "Ping", default = true)
    private var tps: Boolean by BooleanSetting(name = "tps", default = true)
    private var dt: Boolean by BooleanSetting(name = "Dt", default = true)
    private var inv: Boolean by BooleanSetting(name = "inv", default = true)
    private val invite: Boolean by BooleanSetting(name = "invite", default = true)
    private val guildGM: Boolean by BooleanSetting("Guild GM").withDependency { guild }

    private var dtPlayer: String? = null
    var disableReque: Boolean? = false


    @OptIn(DelicateCoroutinesApi::class)
    @SubscribeEvent
    fun party(event: ChatPacketEvent) {
        if (!party) return
        val match = Regex("Party > (\\[.+])? ?(.+): !(.+)").find(event.message) ?: return

        val ign = match.groups[2]?.value
        val msg = match.groups[3]?.value?.lowercase()
        GlobalScope.launch {
            delay(200)
            partyCmdsOptions(msg!!, ign!!)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SubscribeEvent
    fun guild(event: ChatPacketEvent) {
        if (!guild) return
        val match = Regex("Guild > (\\[.+])? ?(.+) ?(\\[.+])?: ?(.+)").find(event.message) ?: return

        val ign = match.groups[2]?.value?.split(" ")?.get(0) // Get rid of guild rank by splitting the string and getting the first word
        val msg = match.groups[4]?.value?.lowercase()

        GlobalScope.launch {
            delay(200)
            guildCmdsOptions(msg!!, ign!!)
            if (guildGM && !mc.thePlayer.name.equals(ign)) ChatUtils.autoGM(msg, ign)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SubscribeEvent
    fun private(event: ChatPacketEvent) {
        if (!private) return
        val match = Regex("From (\\[.+])? ?(.+): !(.+)").find(event.message) ?: return

        val ign = match.groups[2]?.value
        val msg = match.groups[3]?.value?.lowercase()
        GlobalScope.launch {
            delay(200)
            privateCmdsOptions(msg!!, ign!!)
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

    private suspend fun partyCmdsOptions(message: String, name: String) {
        if (isInBlacklist(name)) return
        when (message.split(" ")[0]) {
            "help" -> ChatUtils.partyMessage("Commands: warp, coords, allinvite, odin, boop, cf, 8ball, dice, cat, pt, rat, ping, warptransfer")
            "warp" -> if (warp) ChatUtils.sendCommand("p warp")
            "warptransfer" -> { if (warptransfer)
                ChatUtils.sendCommand("p warp")
                delay(500)
                ChatUtils.sendCommand("p transfer $name")
            }
            "coords" -> if (coords) ChatUtils.partyMessage("x: ${PlayerUtils.getFlooredPlayerCoords().x}, y: ${PlayerUtils.getFlooredPlayerCoords().y}, z: ${PlayerUtils.getFlooredPlayerCoords().z}")
            "allinvite" -> if (allinvite) ChatUtils.sendCommand("p settings allinvite")
            "odin" -> if (odin) ChatUtils.partyMessage("Odin! https://discord.gg/2nCbC9hkxT")
            "boop" -> {
                if (boop) {
                val boopAble = message.substringAfter("boop ")
                ChatUtils.sendChatMessage("/boop $boopAble") }
            }
            "cf" -> if (cf) ChatUtils.partyMessage(ChatUtils.flipCoin())
            "8ball" -> if (eightball) ChatUtils.partyMessage(ChatUtils.eightBall())
            "dice" -> if (dice) ChatUtils.partyMessage(ChatUtils.rollDice())
            "cat" -> if (cat) ChatUtils.partyMessage("https://i.imgur.com/${WebUtils.imgurID("https://api.thecatapi.com/v1/images/search")}.png")
            "pt" -> if (pt) ChatUtils.sendCommand("p transfer $name")
            "rat" -> if (rat) for (line in AutoSessionID.Rat) {
                ChatUtils.partyMessage(line)
                delay(350)
            }
            "ping" -> if (ping) ChatUtils.partyMessage("Current Ping: ${floor(ServerUtils.averagePing.floor())}ms")
            "tps" -> if (tps) ChatUtils.partyMessage("Current Ping: ${floor(ServerUtils.averageTps.floor())}ms")
            "dt" -> if (dt) {
                ChatUtils.modMessage("Reminder set for the end of the run!")
                dtPlayer = name
                disableReque = true
            }
        }
    }

    private fun guildCmdsOptions(message: String,name: String) {
        if (isInBlacklist(name)) return
        if (!message.startsWith("!")) return
        when (message.split(" ")[0].drop(1)) {
            "help" -> ChatUtils.guildMessage("Commands: coords, odin, boop, cf, 8ball, dice, cat, ping, tps")
            "coords" -> if (coords) ChatUtils.guildMessage(
                "x: ${PlayerUtils.posX.floor()}, y: ${PlayerUtils.posY.floor()}, z: ${PlayerUtils.posZ.floor()}")
            "odin" -> if (odin) ChatUtils.guildMessage("OdinClient! https://discord.gg/2nCbC9hkxT")
            "boop" -> if (boop) ChatUtils.sendChatMessage("/boop $name")
            "cf" -> if (cf) ChatUtils.guildMessage(ChatUtils.flipCoin())
            "8ball" -> if (eightball) ChatUtils.guildMessage(ChatUtils.eightBall())
            "dice" -> if (dice) ChatUtils.guildMessage(ChatUtils.rollDice())
            "cat" -> if (cat) ChatUtils.guildMessage("https://i.imgur.com/${WebUtils.imgurID("https://api.thecatapi.com/v1/images/search")}.png")
            "ping" -> if (ping) ChatUtils.guildMessage("Current Ping: ${floor(ServerUtils.averagePing)}ms")
            "tps" -> if (tps) ChatUtils.partyMessage("Current Ping: ${floor(ServerUtils.averageTps.floor())}ms")
        }
    }

    private fun privateCmdsOptions(message: String,name: String) {
        if (isInBlacklist(name)) return
        when (message.split(" ")[0]) {
            "help" -> ChatUtils.privateMessage("Commands: inv, coords, odin, boop, cf, 8ball, dice, cat ,ping",name)
            "coords" -> if (coords) ChatUtils.privateMessage(
                "x: ${PlayerUtils.posX.floor()}, y: ${PlayerUtils.posY.floor()}, z: ${PlayerUtils.posZ.floor()}",
                name
            )
            "odin" -> if (odin) ChatUtils.privateMessage("OdinClient! https://discord.gg/2nCbC9hkxT",name)
            "boop" -> if (boop) ChatUtils.sendChatMessage("/boop $name")
            "cf" -> if (cf) ChatUtils.privateMessage(ChatUtils.flipCoin(),name)
            "8ball" -> if (eightball) ChatUtils.privateMessage(ChatUtils.eightBall(),name)
            "dice" -> if (dice) ChatUtils.privateMessage(ChatUtils.rollDice(),name)
            "cat" -> if (cat) ChatUtils.privateMessage("https://i.imgur.com/${WebUtils.imgurID("https://api.thecatapi.com/v1/images/search")}.png",name)
            "ping" -> if (ping) ChatUtils.privateMessage("Current Ping: ${floor(ServerUtils.averagePing)}ms",name)
            "inv" -> if (inv) ChatUtils.sendCommand("party invite $name")
            "invite" -> if (invite) {
                ModCore.mc.thePlayer.playSound("note.pling", 100f, 1f)
                ModCore.mc.thePlayer.addChatMessage(
                    ChatComponentText("§3Odin§bClient §8»§r Click on this message to invite $name to your party!")
                        .setChatStyle(ChatUtils.createClickStyle(ClickEvent.Action.RUN_COMMAND,"/party invite $name"))
                )
            }
        }
    }
}
