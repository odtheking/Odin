package me.odinmain.features.impl.skyblock

import me.odinmain.events.impl.MessageSentEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.skyblock.sendChatMessage
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ChatEmotes : Module(
    name = "Chat Emotes",
    category = Category.SKYBLOCK,
    description = "Allows you to send hypixel's emotes.",
) {
    private var replaced = false

    @SubscribeEvent
    fun onMessageSent(event: MessageSentEvent) {
        if (event.message.startsWith("/") && !listOf("/pc", "/ac", "/gc", "/msg", "/w", "/r").any { event.message.startsWith(it) }) return

        replaced = false
        val words = event.message.split(" ").toMutableList()

        for (i in words.indices) {
            replacements[words[i]]?.let {
                replaced = true
                words[i] = it
            }
        }

        val newMessage = words.joinToString(" ")
        if (!replaced) return

        event.isCanceled = true
        sendChatMessage(newMessage)
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
        ":skull:" to "☠"
    )

}
