package com.odtheking.odin.features.impl.boss

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.clickgui.settings.impl.StringSetting
import com.odtheking.odin.events.ChatMessageEvent
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.SetSlotEvent
import com.odtheking.odin.events.TerminalEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.features.impl.boss.termsim.TermSimGUI
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.network.WebUtils.gson
import com.odtheking.odin.utils.network.webSocket
import com.odtheking.odin.utils.render.getStringWidth
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.skyblock.LocationUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonClass
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.M7Phases
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalUtils
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.world.item.Items
import java.util.concurrent.ConcurrentHashMap

object MelodyMessage : Module(
    name = "Melody Message",
    description = "Helpful messages for the melody terminal in floor 7."
) {
    private val sendMelodyMessage by BooleanSetting("Send Melody Message", true, desc = "Sends a message when the melody terminal opens.")
    private val melodyMessage by StringSetting("Melody Message", "Melody Terminal start!", 128, desc = "Message sent when the melody terminal opens.").withDependency { sendMelodyMessage }
    private val melodyProgress by BooleanSetting("Melody Progress", false, desc = "Tells the party about melody terminal progress.")
    private val melodySendCoords by BooleanSetting("Melody Send Coords", false, desc = "Sends the coordinates of the melody terminal.").withDependency { melodyProgress }

    private val broadcast by BooleanSetting("Broadcast Progress", true, desc = "Broadcasts melody progress to all other odin users in the party.")
    private val melodyGui by HUD("Progress GUI", "Shows a gui with the progress of broadcasting odin users in melody.", true) {
        var rows = 0
        var labelWidth = 0

        fun track(data: MelodyData, index: Int, name: String) {
            rows = maxOf(rows, index + 1)
            val clay = data.clay ?: return
            val label = melodyLabel(data, name) ?: return
            labelWidth = maxOf(labelWidth, getStringWidth("$clay $label"))
        }

        if (it) {
            val example = MelodyData(3, 1, 2, DungeonClass.ARCHER)
            drawMelody(example, 0, mc.user.name)
            track(example, 0, mc.user.name)
        }

        if (broadcast && melodyWebSocket.connected) {
            melodies.entries.forEachIndexed { i, (name, data) ->
                if (showPlayer == 0 && name == mc.user.name) return@forEachIndexed
                drawMelody(data, i, name)
                track(data, i, name)
            }
        }

        (width * 5 + 2 + labelWidth) to (width * 2 * rows)
    }.withDependency { broadcast }

    private val showPlayer by SelectorSetting("Show Player", "None", arrayListOf("None", "Class", "Name", "Class & Name"), desc = "How player details should be rendered in the Melody GUI.").withDependency { broadcast }

    val melodyWebSocket = webSocket {
        onMessage { message ->
            val (username, type, slot) = try { gson.fromJson(message, UpdateMessage::class.java) } catch (_: Exception) { return@onMessage }
            val playerClass = DungeonUtils.dungeonTeammates.find { it.name == username }?.clazz ?: return@onMessage
            val entry = melodies.getOrPut(username) { MelodyData(null, null, null, playerClass) }
            when (type) {
                0 -> melodies.remove(username)
                1 -> entry.clay = slot
                2 -> entry.purple = slot
                5 -> entry.pane = slot
            }
        }
    }

    private val melodies = ConcurrentHashMap<String, MelodyData>()
    private val lastSent = MelodyData(null, null, null, DungeonClass.EMPTY)

    init {
        on<TerminalEvent.Open> {
            if (DungeonUtils.getF7Phase() != M7Phases.P3 || terminal.type != TerminalTypes.MELODY || mc.screen is TermSimGUI) return@on
            if (sendMelodyMessage) sendCommand("pc $melodyMessage")
            if (melodySendCoords) sendCommand("od sendcoords")
        }

        on<ChatMessageEvent> {
            if (broadcast || melodyProgress) onChatMessage(value)
        }

        on<SetSlotEvent> {
            if (broadcast || melodyProgress) onSlotUpdate(this)
        }

        on<LevelEvent.Load> {
            melodyWebSocket.shutdown()
            melodies.clear()
        }

        on<TerminalEvent.Close> {
            if (terminal.type != TerminalTypes.MELODY) return@on
            melodyWebSocket.send(update(0, 0))
        }
    }

    private val coreRegex = Regex("^The Core entrance is opening!$")
    private val p3StartRegex = Regex("^\\[BOSS] Goldor: Who dares trespass into my domain\\?$")

    private fun onChatMessage(value: String) {
        if (coreRegex.matches(value)) {
            melodyWebSocket.shutdown()
            melodies.clear()
        }

        if (p3StartRegex.matches(value)) LocationUtils.lobbyId?.let {
            melodyWebSocket.connect("${ClickGUIModule.webSocketUrl}${it}")
        }
    }

    private fun onSlotUpdate(event: SetSlotEvent) {
        val term = TerminalUtils.currentTerm ?: return
        if (DungeonUtils.getF7Phase() != M7Phases.P3 || term.type != TerminalTypes.MELODY || mc.screen is TermSimGUI) return

        val item = event.itemStack.item
        if (item == Items.LIME_TERRACOTTA) {
            val position = event.slotIndex / 9
            if (lastSent.clay == position) return
            if (broadcast) melodyWebSocket.send(update(1, position))
            if (melodyProgress) clayProgress[position]?.let { sendCommand("pc $it") }
            lastSent.clay = position
            return
        }
        if (!broadcast || !item.equalsOneOf(Items.MAGENTA_STAINED_GLASS_PANE, Items.LIME_STAINED_GLASS_PANE)) return
        val index = mapToRange(event.slotIndex) ?: return
        val meta = when (item) {
            Items.MAGENTA_STAINED_GLASS_PANE -> {
                if (lastSent.purple == index) return
                lastSent.purple = index
                2
            }
            Items.LIME_STAINED_GLASS_PANE -> {
                if (lastSent.pane == index) return
                lastSent.pane = index
                5
            }
            else -> return
        }

        melodyWebSocket.send(update(meta, index))
    }

    private fun update(type: Int, slot: Int): String = gson.toJson(UpdateMessage(mc.user.name, type, slot))

    private val clayProgress = hashMapOf(2 to "Melody 25%", 3 to "Melody 50%", 4 to "Melody 75%")
    private val ranges = listOf(1..5, 10..14, 19..23, 28..32, 37..41)

    private fun mapToRange(value: Int): Int? {
        for (r in ranges) {
            if (value in r) return (value - r.first) % 5
        }
        return null
    }

    private val width by lazy { getStringWidth("§d■") }

    private fun melodyLabel(data: MelodyData, playerName: String): String? = when (showPlayer) {
        1 -> data.dungeonClass.toString()
        2 -> playerName
        3 -> "$playerName (${data.dungeonClass})"
        else -> null
    }

    private fun GuiGraphicsExtractor.drawMelody(data: MelodyData, index: Int, playerName: String) {
        val y = width * 2 * index

        repeat(5) {
            if (data.purple == it) textDim("§d■", width * it, y)
            textDim("${if (data.pane == it) "§a" else "§f"}■", width * it, y + width)
        }

        val label = melodyLabel(data, playerName) ?: return
        data.clay?.let { textDim("$it $label", width * 5 + 2, y + width / 2) }
    }

    private data class UpdateMessage(val username: String, val type: Int, val slot: Int)
    private data class MelodyData(var purple: Int?, var pane: Int?, var clay: Int?, val dungeonClass: DungeonClass)
}
