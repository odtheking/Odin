package com.odtheking.odin.features.impl.boss

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.StringSetting
import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.setSubtitle
import com.odtheking.odin.utils.setTitle
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket

object TerminalTitles : Module(
    name = "Terminal Titles",
    description = "Custom terminal titles for the terminal."
) {
    private val selfOnly by BooleanSetting("Self Only", false, desc = "Only replace title messages when your own name appears.")
    private val useSubtitle by BooleanSetting("Use Subtitle", true, desc = "Use the subtitle instead of the title. ")
    private val replaceTerminal by BooleanSetting("Replace Terminal", true, desc = "Replace terminal completion titles.")
    private val replaceLever by BooleanSetting("Replace Lever", true, desc = "Replace lever completion titles.")
    private val replaceDevice by BooleanSetting("Replace Device", true, desc = "Replace device completion titles.")
    private val customTitle by StringSetting(
        "Custom Title", "&6{name} &8[&c{current}&8/&a{total}&8]", 128,
        "Use {name}, {action}, {type}, {current}, and {total}. Use & color codes (e.g. &a)."
    )

    private val titleRegex = Regex("^(.{1,16}) (activated|completed) a (terminal|lever|device)! \\((\\d+)\\/(\\d+)\\)$")
    private val colorCodeRegex = Regex("&([0-9A-FK-ORa-fk-or])")

    init {
        onReceive<ClientboundSetTitleTextPacket> { it.handleTitle(text.string) }
        onReceive<ClientboundSetSubtitleTextPacket> { it.handleTitle(text.string) }
    }

    fun PacketEvent.Receive.handleTitle(text: String) {
        val (name, action, type, current, total) = titleRegex.find(text)?.destructured ?: return
        if (selfOnly && name != mc.player?.name?.string) return

        val shouldReplace = when (type) {
            "terminal" -> replaceTerminal
            "lever" -> replaceLever
            "device" -> replaceDevice
            else -> false
        }
        if (!shouldReplace) return

        cancel()
        mc.execute {
            val title = customTitle
                .replace("{name}", name)
                .replace("{action}", action)
                .replace("{type}", type)
                .replace("{current}", current)
                .replace("{total}", total)
                .replace(colorCodeRegex, "\u00A7$1").ifEmpty { return@execute }

            if (useSubtitle) setSubtitle(title)
            else setTitle(title)
        }
    }
}