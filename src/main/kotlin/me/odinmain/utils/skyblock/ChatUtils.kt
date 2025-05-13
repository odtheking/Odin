package me.odinmain.utils.skyblock

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.render.ClickGUIModule.devMessages
import me.odinmain.features.impl.skyblock.ChatCommands
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.runOnMCThread
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.client.ClientCommandHandler
import kotlin.math.roundToInt

/**
 * Executes a given command either client-side or server-side.
 *
 * @param text Command to be executed.
 * @param clientSide If `true`, the command is executed client-side; otherwise, server-side.
 */
fun sendCommand(text: Any, clientSide: Boolean = false) {
    if (LocationUtils.currentArea.isArea(Island.SinglePlayer) && !clientSide) modMessage("Sending command: $text")
    if (clientSide) ClientCommandHandler.instance.executeCommand(mc.thePlayer, "/$text")
    else sendChatMessage("/$text")
}

/**
 * Sends a chat message directly to the chat.
 *
 * @param message Message to be sent.
 */
fun sendChatMessage(message: Any) {
    runOnMCThread { mc.thePlayer?.sendChatMessage(message.toString()) }
}

/**
 * Sends a client-side message with an optional prefix.
 *
 * @param message Message to be sent.
 * @param prefix If `true`, adds a prefix to the message.
 * @param chatStyle Optional chat style to be applied to the message.
 */
fun modMessage(message: Any?, prefix: String = "§3Odin §8»§r ", chatStyle: ChatStyle? = null) {
    val chatComponent = ChatComponentText("$prefix$message")
    chatStyle?.let { chatComponent.setChatStyle(it) } // Set chat style using setChatStyle method
    runOnMCThread { mc.thePlayer?.addChatMessage(chatComponent) }
}

/**
 * Sends a client-side message for developers only.
 *
 * @param message Message to be sent.
 */
fun devMessage(message: Any?) {
    if (!devMessages) return println("OdinDev » $message")
    modMessage(message, prefix = "§3Odin§bDev §8»§r ")
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
 * @param name Name of the person to send the message to.
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