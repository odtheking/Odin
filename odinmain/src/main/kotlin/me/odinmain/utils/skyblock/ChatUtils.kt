package me.odinmain.utils.skyblock

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.skyblock.DevPlayers.devs
import me.odinmain.utils.Utils.noControlCodes
import me.odinmain.utils.WebUtils
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.client.event.ClientChatReceivedEvent

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
        if (mc.thePlayer == null) return
        mc.thePlayer.sendChatMessage(message.toString())
    }

    fun modMessage(message: Any, prefix: Boolean = true) {
        if (mc.thePlayer == null) return
        val msg = if (prefix) "§3Odin §8»§r $message" else message.toString()
        mc.thePlayer?.addChatMessage(ChatComponentText(msg))
    }

    fun devMessage(message: Any, prefix: Boolean = true) {
        if (mc.thePlayer == null) return
        if (!devs.keys.contains(mc.thePlayer.name)) return
        val msg = if (prefix) "§3Odin§bDev §8»§r $message" else message.toString()
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
