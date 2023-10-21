package me.odinmain.features.impl.floor7.p3

import me.odinmain.config.Config
import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.TerminalTimes.currentTerminal
import me.odinmain.features.impl.floor7.p3.TerminalTimes.sendMessage
import me.odinmain.features.impl.floor7.p3.TerminalTimes.startTimer
import me.odinmain.features.impl.skyblock.ChatCommands.private
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.utils.name
import me.odinmain.utils.skyblock.ChatUtils.modMessage
import me.odinmain.utils.skyblock.ChatUtils.unformattedText
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object TerminalTimes : Module(
    name = "Terminal Times",
    description = "Keeps track of how long you took to complete a terminal.",
    category = Category.FLOOR7
) {
    private val sendMessage: Int by SelectorSetting("Send Message", "Always", arrayListOf("Only PB", "Always"))
    private var currentTerminal: Terminals? = null
    private var startTimer = 0L

    @Suppress("UNUSED")
    enum class Terminals(
        val fullName: String,
        val setting: NumberSetting<Double>
    ) {
        Panes("Correct all the panes!", +NumberSetting("Panes PB", 1000.0, increment = 0.01, hidden = true)),
        Color("Change all to same color!", +NumberSetting("Panes PB", 1000.0, increment = 0.01, hidden = true)),
        Numbers("Click in order!", +NumberSetting("Numbers PB", 1000.0, increment = 0.01, hidden = true)),
        Melody("Click the button on time!", +NumberSetting("Melody PB", 1000.0, increment = 0.01, hidden = true)),
        `Starts With`("What starts with", +NumberSetting("Starts With PB", 1000.0, increment = 0.01, hidden = true)),
        `Select All`("Select all the", +NumberSetting("Select All PB", 1000.0, increment = 0.01, hidden = true)),
    }

    @SubscribeEvent
    fun onClientTick(event: TickEvent.ClientTickEvent) {
        if (currentTerminal != null) return

        val container = mc.thePlayer?.openContainer ?: return
        if (container !is ContainerChest) return

        Terminals.entries.find { container.name.startsWith(it.fullName) }?.let {
            currentTerminal = it
            startTimer = System.currentTimeMillis()
        }
    }

    @SubscribeEvent
    fun onClientChatReceived(event: ChatPacketEvent) {
        if (currentTerminal == null) return
        modMessage(event.message)
        val match = Regex("(.+) (?:activated|completed) a terminal! \\((\\d)/(\\d)\\)").find(event.message) ?: return
        val (_, name, current, max) = match.groups.map { it?.value }

        if (current?.toInt() == max?.toInt() || current?.toInt() == 0) {
            if (name != mc.thePlayer.name) {
                // Gate opened and not by player
                currentTerminal = null
                return
            }
        }

        if (name != mc.thePlayer.name) return
        val time = (System.currentTimeMillis() - startTimer) / 1000.0

        if (sendMessage == 1) modMessage("§6${currentTerminal?.name} §ftook §a${time}s")

        val previousTime = currentTerminal!!.setting.value
        if (time < previousTime) {
            modMessage("§fNew best time for §6${currentTerminal?.name} §fis §a${time}s, §fold best time was §a${previousTime}s")
            currentTerminal!!.setting.value = time
            Config.saveConfig()
        }
        currentTerminal = null
    }
}