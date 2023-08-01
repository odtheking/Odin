package me.odinclient.features.impl.m7

import me.odinclient.OdinClient.Companion.config
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.OdinClient.Companion.miscConfig
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.features.settings.impl.SelectorSetting
import me.odinclient.utils.Utils.noControlCodes
import me.odinclient.utils.skyblock.ChatUtils.modMessage
import me.odinclient.utils.skyblock.ChatUtils.unformattedText
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object TerminalTimes : Module(
    name = "Terminal Times",
    description = "Keeps track of how long you took to complete a terminal.",
    category = Category.M7
) {
    private val sendMessage: Int by SelectorSetting("Show Message", "Always", arrayListOf("Only if Personal Best", "Always"))

    private var inTerm = false
    private var timer = 0L
    private var currentTerminal = ""
    private val terminalNames = listOf(
        "Correct all the panes!",
        "Change all to same color!",
        "Click in order!",
        "Click the button on time!",
        "What starts with",
        "Select all the"
    )

    enum class Times (
        val fullName: String,
        var time: Double = 1000.0
    ) {
        Panes("Correct all the panes!"),
        Color("Change all to same color!"),
        Numbers("Click in order!"),
        Melody("Click the button on time!"),
        StartsWith("What starts with"),
        Select("Select all the");
    }

    @SubscribeEvent
    fun onClientTick(event: TickEvent.ClientTickEvent) {
        if (inTerm) return
        val currentScreen = mc.currentScreen

        if (currentScreen !is GuiChest) return
        val container = currentScreen.inventorySlots
        if (container !is ContainerChest) return
        val chestName = container.lowerChestInventory.displayName.unformattedText ?: return

        terminalNames.forEach { name ->
            if (chestName.startsWith(name)) {
                inTerm = true
                currentTerminal = chestName
                timer = System.currentTimeMillis()
            }
        }
    }

    @SubscribeEvent
    fun onClientChatReceived(event: ClientChatReceivedEvent) {
        val message = event.unformattedText
        val match = Regex("(.+) (?:activated|completed) a terminal! \\((\\d)/(\\d)\\)").find(message) ?: return
        val (_, name, current, max) = match.groups.map { it?.value }

        if (current?.toInt() == max?.toInt() || current?.toInt() == 0) {
            if (name != mc.thePlayer.name) {
                // Gate opened and not by player
                inTerm = false
                currentTerminal = ""
                return
            }
        }

        if (name != mc.thePlayer.name) return
        inTerm = false
        val time = (System.currentTimeMillis() - timer) / 1000.0

        if (sendMessage == 1) modMessage("§6$currentTerminal §ftook §a${time}s")

        for (times in Times.values()) {
            if (times.fullName == currentTerminal && time < times.time) {
                modMessage("§fNew best time for §6${times.fullName} §fis §a${time}s, §fold best time was §a${times.time}s")
                times.time = time
                miscConfig.saveAllConfigs()
                break
            }
        }

        currentTerminal = ""
    }
}
