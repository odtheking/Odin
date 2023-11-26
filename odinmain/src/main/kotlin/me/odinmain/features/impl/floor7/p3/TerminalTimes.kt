package me.odinmain.features.impl.floor7.p3

import me.odinmain.config.Config
import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.utils.name
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.inventory.ContainerChest
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
    private val panesPB = +NumberSetting("Panes PB", 99.0, increment = 0.01, hidden = true)
    private val colorPB = +NumberSetting("Color PB", 99.0, increment = 0.01, hidden = true)
    private val numbersPB = +NumberSetting("Numbers PB", 99.0, increment = 0.01, hidden = true)
    private val melodyPB = +NumberSetting("Melody PB", 99.0, increment = 0.01, hidden = true)
    private val startsWithPB = +NumberSetting("Starts With PB", 99.0, increment = 0.01, hidden = true)
    private val selectAllPB = +NumberSetting("Select All PB", 99.0, increment = 0.01, hidden = true)
    val simPanesPB = +NumberSetting("Sim Panes PB", 99.0, increment = 0.01, hidden = true)
    val simColorPB = +NumberSetting("Sim Color PB", 99.0, increment = 0.01, hidden = true)
    val simNumbersPB = +NumberSetting("Sim Numbers PB", 99.0, increment = 0.01, hidden = true)
    val simStartsWithPB = +NumberSetting("Sim Starts With PB", 99.0, increment = 0.01, hidden = true)
    val simSelectAllPB = +NumberSetting("Sim Select All PB", 99.0, increment = 0.01, hidden = true)

    @Suppress("UNUSED")
    enum class Terminals(
        val fullName: String,
        val setting: NumberSetting<Double>
    ) {
        Panes("Correct all the panes!", panesPB),
        Color("Change all to same color!", colorPB),
        Numbers("Click in order!", numbersPB),
        Melody("Click the button on time!", melodyPB),
        `Starts With`("What starts with", startsWithPB),
        `Select All`("Select all the", selectAllPB),
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
            currentTerminal?.setting?.value = time
            Config.saveConfig()
        }
        currentTerminal = null
    }
}