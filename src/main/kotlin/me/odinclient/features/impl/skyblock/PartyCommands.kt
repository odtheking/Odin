package me.odinclient.features.impl.skyblock

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.odinclient.OdinClient.Companion.scope
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.utils.AutoSessionID
import me.odinclient.utils.ServerUtils
import me.odinclient.utils.Utils.noControlCodes
import me.odinclient.utils.skyblock.ChatUtils
import me.odinclient.utils.skyblock.LocationUtils
import me.odinclient.utils.skyblock.PlayerUtils
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.floor

object PartyCommands : Module(
    name = "Party Commands",
    category = Category.SKYBLOCK,
    description = "Party Commands! Use /blacklist to blacklist players from using this module. !help for help."
) {

    private var help: Boolean by BooleanSetting(name = "help", default = true)
    private var warp: Boolean by BooleanSetting(name = "warp", default = true)
    private var warptransfer: Boolean by BooleanSetting(name = "warptransfer", default = true)
    private var coords: Boolean by BooleanSetting(name = "coords", default = true)
    private var allinvite: Boolean by BooleanSetting(name = "allinvite", default = true)
    private var odin: Boolean by BooleanSetting(name = "oding", default = true)
    private var boop: Boolean by BooleanSetting(name = "boop", default = true)
    private var cf: Boolean by BooleanSetting(name = "cf", default = true)
    private var eightball: Boolean by BooleanSetting(name = "eightball", default = true)
    private var dice: Boolean by BooleanSetting(name = "dice", default = true)
    private var cat: Boolean by BooleanSetting(name = "cat", default = true)
    private var rs: Boolean by BooleanSetting(name = "rs", default = true)
    private var pt: Boolean by BooleanSetting(name = "pt", default = true)
    private var rat: Boolean by BooleanSetting(name = "rat", default = true)
    private var ping: Boolean by BooleanSetting(name = "ping", default = true)
    private var dt: Boolean by BooleanSetting(name = "dt", default = true)

    @SubscribeEvent
    fun party(event: ClientChatReceivedEvent) {
        val message = event.message.unformattedText.noControlCodes
        val match = Regex("Party > (\\[.+])? ?(.+): !(.+)").find(message) ?: return

        val ign = match.groups[2]?.value
        val msg = match.groups[3]?.value?.lowercase()

        scope.launch {
            delay(150)
            partyCmdsOptions(msg!!, ign!!)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SubscribeEvent
    fun dt(event: ClientChatReceivedEvent) {
        val message = event.message.unformattedText.noControlCodes

        if (!message.contains("EXTRA STATS") || dtPlayer == null) return

        GlobalScope.launch{
            delay(2500)
            PlayerUtils.alert("§c${dtPlayer} needs downtime")
            ChatUtils.partyMessage("$dtPlayer needs downtime")
            dtPlayer = null
        }
    }

    @SubscribeEvent
    fun joinDungeon(event: ClientChatReceivedEvent) {
        val message = event.message.unformattedText.noControlCodes
        val match = Regex("(Party >) (\\[.+])? ?(.+): !(.+) (.+)").find(message) ?: return

        val msg = match.groups[3]?.value?.lowercase()
        val num = match.groups[4]?.value

        ChatUtils.joinDungeon(msg!!, num!!)
    }

    private var dtPlayer: String? = null
    private suspend fun partyCmdsOptions(message: String,name: String) {
        if (BlackList.isInBlacklist(name)) return
        when (message.split(" ")[0]) {
            "help" -> if (help) ChatUtils.partyMessage("Commands: warp, coords, allinvite, odin, boop, cf, 8ball, dice, cat, rs, pt, rat, ping, warptransfer")
            "warp" -> if (warp) ChatUtils.sendCommand("p warp")
            "warptransfer" -> if (warptransfer) {
                ChatUtils.sendCommand("p warp")
                delay(500)
                ChatUtils.sendCommand("p transfer $name")
            }
            "coords" -> if (coords) ChatUtils.partyMessage("x: ${PlayerUtils.getFlooredPlayerCoords().x}, y: ${PlayerUtils.getFlooredPlayerCoords().y}, z: ${PlayerUtils.getFlooredPlayerCoords().z}")
            "allinvite" -> if (allinvite) ChatUtils.sendCommand("p settings allinvite")
            "odin" -> if (odin) ChatUtils.partyMessage("Odin! https://discord.gg/2nCbC9hkxT")
            "boop" -> if (boop) {
                val boopAble = message.substringAfter("boop ")
                ChatUtils.sendChatMessage("/boop $boopAble")
            }
            "cf" -> if (cf) ChatUtils.partyMessage(ChatUtils.flipCoin())
            "8ball" -> if (eightball) ChatUtils.partyMessage(ChatUtils.eightBall())
            "dice" -> if (dice) ChatUtils.partyMessage(ChatUtils.rollDice())
            "cat" -> if (cat) ChatUtils.partyMessage(ChatUtils.catPics())
            "rs" -> if (rs) {
                val currentFloor = LocationUtils.currentDungeon?.floor ?: return
                ChatUtils.modMessage("restarting")
                ChatUtils.sendCommand("reparty",true)
                if (!currentFloor.isInMM) {
                    ChatUtils.modMessage("joindungeon catacombs ${currentFloor.floorNumber}")
                    ChatUtils.sendCommand("joindungeon catacombs ${currentFloor.floorNumber}")
                } else {
                    ChatUtils.modMessage("joindungeon master_catacombs ${currentFloor.floorNumber}")
                    ChatUtils.sendCommand("joindungeon master_catacombs ${currentFloor.floorNumber}")
                }

            }
            "pt" -> if (pt) ChatUtils.sendCommand("p transfer $name")
            "rat" -> if (rat) for (line in AutoSessionID.Rat) {
                ChatUtils.partyMessage(line)
                delay(350)
            }
            "ping" -> if (ping) ChatUtils.partyMessage("Current Ping: ${floor(ServerUtils.averagePing)}ms")
            "dt" -> if (dt) {
                ChatUtils.modMessage("Reminder set for the end of the run!")
                dtPlayer = name
            }
        }
    }

}
