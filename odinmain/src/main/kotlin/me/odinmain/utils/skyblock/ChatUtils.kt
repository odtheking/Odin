package me.odinmain.utils.skyblock

import me.odinmain.OdinMain.logger
import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.render.ClickGUIModule.devMessages
import me.odinmain.features.impl.render.DevPlayers
import me.odinmain.features.impl.skyblock.ChatCommands
import me.odinmain.utils.noControlCodes
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.*
import net.minecraftforge.client.ClientCommandHandler
import kotlin.math.roundToInt


/**
 * Generates a random response from an eight-ball simulation.
 *
 * @return A randomly selected response.
 */
fun eightBall(): String {
    return responses.random()
}

/**
 * Simulates the flipping of a coin.
 *
 * @return The result of the coin flip ("heads" or "tails").
 */
fun flipCoin(): String = if (Math.random() < 0.5) "heads" else "tails"

/**
 * Rolls a six-sided die and returns the result.
 *
 * @return The result of the die roll (1 to 6).
 */
fun rollDice(): Int = (1..6).random()

/**
 * Executes a given command either client-side or server-side.
 *
 * @param text Command to be executed.
 * @param clientSide If `true`, the command is executed client-side; otherwise, server-side.
 */
fun sendCommand(text: Any, clientSide: Boolean = false) {
    if (LocationUtils.currentArea.isArea(Island.SinglePlayer) && !clientSide) return modMessage("Sending command: $text")
    if (clientSide) ClientCommandHandler.instance.executeCommand(mc.thePlayer, "/$text")
    else sendChatMessage("/$text")
}

/**
 * Sends a chat message directly to the chat.
 *
 * @param message Message to be sent.
 */
fun sendChatMessage(message: Any) {
    if (mc.thePlayer == null) return
    mc.thePlayer.sendChatMessage(message.toString())
}

/**
 * Sends a client-side message with an optional prefix.
 *
 * @param message Message to be sent.
 * @param prefix If `true`, adds a prefix to the message.
 * @param chatStyle Optional chat style to be applied to the message.
 */
fun modMessage(message: Any?, prefix: Boolean = true, chatStyle: ChatStyle? = null) {
    if (mc.thePlayer == null) return
    val chatComponent = ChatComponentText(if (prefix) "§3Odin §8»§r $message" else message.toString())
    chatStyle?.let { chatComponent.setChatStyle(it) } // Set chat style using setChatStyle method
    try { mc.thePlayer?.addChatMessage(chatComponent) }
    catch (e: Exception) { logger.error("Error sending message: $message", e)}
}


/**
 * Sends a client-side message for developers only.
 *
 * @param message Message to be sent.
 * @param prefix If `true`, adds a prefix to the message.
 */
fun devMessage(message: Any?, prefix: Boolean = true) {
    if (!devMessages || mc.thePlayer == null || !DevPlayers.isDev) return
    val msg = if (prefix) "§3Odin§bDev §8»§r $message" else message.toString()
    mc.thePlayer?.addChatMessage(ChatComponentText(msg))
}

/**
 * Sends a message in all chat on Hypixel.
 *
 * @param message Message to be sent.
 */
fun allMessage(message: Any) {
    sendCommand("ac $message")
}

/**
 * Sends a message in guild chat on Hypixel.
 *
 * @param message Message to be sent.
 */
fun guildMessage(message: Any) {
    sendCommand("gc $message")
}

/**
 * Sends a message in party chat on Hypixel.
 *
 * @param message Message to be sent.
 */
fun partyMessage(message: Any) {
    sendCommand("pc $message")
}

/**
 * Sends a message in private chat on Hypixel.
 *
 * @param message Message to be sent.
 * @param name Person to send to.
 */
fun privateMessage(message: Any, name: String) {
    sendCommand("w $name $message")
}

/**
 * Sends a message in the corresponding channel.
 *
 * @param message Message to be sent.
 * @param name Name for private message.
 * @param channel Channel to send the message.
 */
fun channelMessage(message: Any, name: String, channel: ChatCommands.ChatChannel) {
    when (channel) {
        ChatCommands.ChatChannel.GUILD -> guildMessage(message)
        ChatCommands.ChatChannel.PARTY -> partyMessage(message)
        ChatCommands.ChatChannel.PRIVATE -> privateMessage(message, name)
    }
}

/**
 * Generates a chat line break with a specific color and style.
 *
 * @return A formatted string representing a chat line break.
 */
fun getChatBreak(): String =
    mc.ingameGUI?.chatGUI?.chatWidth?.let {
        "§9§m" + "-".repeat(it / mc.fontRendererObj.getStringWidth("-"))
    } ?: ""

/**
 * Centers a given text in the chat.
 *
 * @param text Text to be centered.
 * @return Centered text.
 */
fun getCenteredText(text: String): String {
    val textWidth = mc.fontRendererObj.getStringWidth(text.noControlCodes)
    val chatWidth = mc.ingameGUI?.chatGUI?.chatWidth ?: 0

    if (textWidth >= chatWidth) return text

    return StringBuilder().apply {
        repeat((((chatWidth - textWidth) / 2f) / mc.fontRendererObj.getStringWidth(" ")).roundToInt()) { append(' ') } }.append(text).toString()
}

/**
 * Creates a `ChatStyle` with click and hover events for making a message clickable.
 *
 * @param action Action to be executed on click.
 * @param value Text to show up when hovered.
 * @return A `ChatStyle` with click and hover events.
 */
fun createClickStyle(action: ClickEvent.Action?, value: String): ChatStyle {
    val style = ChatStyle()
    style.chatClickEvent = ClickEvent(action, value)
    style.chatHoverEvent = HoverEvent(
        HoverEvent.Action.SHOW_TEXT,
        ChatComponentText(EnumChatFormatting.YELLOW.toString() + value)
    )
    return style
}

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