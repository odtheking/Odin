package me.odinclient.utils.skyblock

import kotlinx.coroutines.delay
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.impl.skyblock.BlackList
import me.odinclient.utils.AutoSessionID
import me.odinclient.utils.ServerUtils
import me.odinclient.utils.Utils.floor
import me.odinclient.utils.Utils.noControlCodes
import me.odinclient.utils.WebUtils
import me.odinclient.utils.skyblock.PlayerUtils.posX
import me.odinclient.utils.skyblock.PlayerUtils.posY
import me.odinclient.utils.skyblock.PlayerUtils.posZ
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.client.event.ClientChatReceivedEvent
import kotlin.math.floor

object ChatUtils {

    inline val ClientChatReceivedEvent.unformattedText
        get() = this.message.unformattedText.noControlCodes

    fun eightBall(): String {
        return responses.random()
    }

    fun catPics(): String {
        val catsArray = cats.toString().split(",")
        return catsArray.random()
    }

    fun flipCoin(): String = if (Math.random() < 0.5) "heads" else "tails"


    fun rollDice(): Int = (1..6).random()


    fun sendCommand(text: Any, clientSide: Boolean = false) {
        if (clientSide) ClientCommandHandler.instance.executeCommand(mc.thePlayer, "/$text")
        else sendChatMessage("/$text")
    }

    fun sendChatMessage(message: Any) {
        mc.thePlayer.sendChatMessage(message.toString())
    }

    fun modMessage(message: Any, prefix: Boolean = true) {
        if (mc.thePlayer == null) return
        val msg = if (prefix) "§3Odin§bClient §8»§r $message" else message.toString()
        mc.thePlayer?.addChatMessage(ChatComponentText(msg))
    }

    fun guildMessage(message: Any) {
        sendCommand("gc $message")
    }

    fun partyMessage(message: Any) {
        sendCommand("pc $message")
    }

    fun privateMessage(message: Any, name: String) {
        sendCommand("w $name $message")
    }

    fun getChatBreak(): String =
        mc.ingameGUI?.chatGUI?.chatWidth?.let {
            "§9§m" + "-".repeat(it / mc.fontRendererObj.getStringWidth("-"))
        } ?: ""

    fun guildCmdsOptions(message: String, name: String) {
        if (BlackList.isInBlacklist(name)) return
        when (message.split(" ")[0].drop(1)) {
            "help" -> guildMessage("Commands: coords, odin, boop, cf, 8ball, dice, cat, ping")
            "coords" -> guildMessage(
                "x: ${posX.floor()}, y: ${posY.floor()}, z: ${posZ.floor()}"
            )
            "odin" -> guildMessage("OdinClient! https://discord.gg/2nCbC9hkxT")
            "boop" -> sendChatMessage("/boop $name")
            "cf" -> guildMessage(flipCoin())
            "8ball" -> guildMessage(eightBall())
            "dice" -> guildMessage(rollDice())
            "cat" -> guildMessage(catPics())
            "ping" -> guildMessage("Current Ping: ${floor(ServerUtils.averagePing)}ms")
            "gm" -> guildMessage("Good Morning $name!")
            "gn" -> guildMessage("Good Night $name.")
        }
    }

    fun autoGM(message: String, name: String) {
        if (BlackList.isInBlacklist(name)) return
        if(message.lowercase().startsWith("gm")) guildMessage("gm $name")
        if(message.lowercase().startsWith("gn")) guildMessage("gn $name")
    }
    
    var dtPlayer: String? = null


    fun privateCmdsOptions(message: String, name: String) {
        if (BlackList.isInBlacklist(name)) return
        when (message.split(" ")[0]) {
            "help" -> privateMessage("Commands: inv, coords, odin, boop, cf, 8ball, dice, cat ,ping", name)
            "coords" -> privateMessage(
                "x: ${posX.floor()}, y: ${posY.floor()}, z: ${posZ.floor()}",
                name
            )
            "odin" -> privateMessage("OdinClient! https://discord.gg/2nCbC9hkxT", name)
            "boop" -> sendChatMessage("/boop $name")
            "cf" -> privateMessage(flipCoin(), name)
            "8ball" -> privateMessage(eightBall(), name)
            "dice" -> privateMessage(rollDice(), name)
            "cat" -> privateMessage(catPics(), name)
            "ping" -> privateMessage("Current Ping: ${floor(ServerUtils.averagePing)}ms", name)
            "inv" -> sendCommand("party invite $name")
            "gm" -> privateMessage("Good Morning $name!", name)
            "gn" -> privateMessage("Good Night $name.", name)
            "invite" -> {
                mc.thePlayer.playSound("note.pling", 100f, 1f)
                mc.thePlayer.addChatMessage(
                    ChatComponentText("§3Odin§bClient §8»§r Click on this message to invite $name to your party!")
                        .setChatStyle(createClickStyle(ClickEvent.Action.RUN_COMMAND, "/party invite $name"))
                )
            }
        }
    }

    fun joinDungeon(message: String, num: String) {
        if (message.startsWith("m")) {
            sendCommand("joindungeon master_catacombs $num")
            modMessage("You should be in m$num in 5 seconds.")
        }
        if (message.startsWith("f")) {
            sendCommand("joindungeon catacombs $num")
            modMessage("You should be in f$num in 5 seconds.")
        }
    }

    fun createClickStyle(action: ClickEvent.Action?, value: String): ChatStyle {
        val style = ChatStyle()
        style.chatClickEvent = ClickEvent(action, value)
        style.chatHoverEvent = HoverEvent(
            HoverEvent.Action.SHOW_TEXT,
            ChatComponentText(EnumChatFormatting.YELLOW.toString() + value)
        )
        return style
    }


    private var cats: Any? = WebUtils.fetchURLData("https://pastebin.com/raw/m4L2e62y")

    private val responses = arrayOf(
        "It is certain",
        "It is decidedly so",
        "Without a doubt",
        "Yes definitely",
        "You may rely on it",
        "As I see it, yes",
        "Most likely",
        "Outlook good",
        "Yes",
        "Signs point to yes",
        "Reply hazy try again",
        "Ask again later",
        "Better not tell you now",
        "Cannot predict now",
        "Concentrate and ask again",
        "Don't count on it",
        "My reply is no",
        "My sources say no",
        "Outlook not so good",
        "Very doubtful"
    )
}
