package me.odinmain.features.impl.skyblock

import me.odinmain.events.impl.MessageSentEvent
import me.odinmain.events.impl.PacketReceivedEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.StringSetting
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.skyblock.*
import net.minecraft.event.ClickEvent
import net.minecraft.network.play.server.S02PacketChat
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*


object PartyEncoding: Module(
    "Party Encoding",
    category = Category.SKYBLOCK,
    description = "Encodes and decodes party messages."
) {
    private val key: String by StringSetting("Key", "odin", description = "Key")

    private val urlRegex = Regex("https?://\\S+")

    @SubscribeEvent
    fun onChatMessage(event: PacketReceivedEvent) { // foolish method will be reworked maybe
        if (event.packet !is S02PacketChat) return

        val chatComponent = event.packet.chatComponent
        val matchResult = Regex("Party > (\\[.+?]) ?(.{1,16}): (.*)").matches(event.packet.chatComponent.unformattedText.noControlCodes)
        if (!matchResult) return

        val name = chatComponent.formattedText.split(":")[0]
        val message = chatComponent.unformattedText.split(":")[1].noControlCodes.drop(1)
        val decoded = decodeMessage(message, key)

        if (decoded.isEmpty()) return
        val url = urlRegex.find(decoded)?.value
        if (url != null)
            modMessage("$name: $decoded", false, createClickStyle(ClickEvent.Action.OPEN_URL, url))
        else
            modMessage("$name: $decoded", false)

        event.isCanceled = true
    }

    @SubscribeEvent
    fun onMessageSent(event: MessageSentEvent) {
        if (!event.message.startsWith("/pcd")) return

        val encoded = encodeMessage(event.message.drop(5), key)
        if (encoded.length > 160) return
        partyMessage(encoded)
        modMessage("§8Actual message for hypixel: §7$encoded", false)
        event.isCanceled = true
    }

    private fun encodeMessage(message: String, key: String): String {
        require(key.isNotEmpty()) { "Key must not be empty" }

        val keyChars = key.toCharArray()
        val encodedChars = CharArray(message.length) { i ->
            (message[i].code xor keyChars[i % keyChars.size].code).toChar()
        }

        val encodedString = String(encodedChars)
        return Base64.getEncoder().encodeToString(encodedString.toByteArray())
    }

    private fun decodeMessage(encodedMessage: String, key: String): String {
        require(key.isNotEmpty()) { "Key must not be empty" }

        return try {
            val decodedBytes = Base64.getDecoder().decode(encodedMessage)
            val decodedChars = decodedBytes.map { it.toInt().toChar() }.toCharArray()

            val keyChars = key.toCharArray()
            val originalChars = CharArray(decodedChars.size) { i ->
                (decodedChars[i].code xor keyChars[i % keyChars.size].code).toChar()
            }

            String(originalChars)
        } catch (e: IllegalArgumentException) {
            "" // Normal messages won't be able to be decoded, so we just return an empty string
        }
    }

}