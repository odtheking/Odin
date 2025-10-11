package me.odinmain.features.impl.floor7.p3

import me.odinmain.OdinMain.gson
import me.odinmain.clickgui.settings.Setting.Companion.withDependency
import me.odinmain.clickgui.settings.impl.BooleanSetting
import me.odinmain.clickgui.settings.impl.StringSetting
import me.odinmain.events.impl.TerminalEvent
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.termsim.TermSimGUI
import me.odinmain.features.impl.render.ClickGUIModule.wsServer
import me.odinmain.utils.network.webSocket
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.M7Phases
import me.odinmain.utils.skyblock.partyMessage
import me.odinmain.utils.skyblock.sendCommand
import me.odinmain.utils.ui.getTextWidth
import net.minecraft.item.Item
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.concurrent.ConcurrentHashMap

object MelodyMessage : Module(
    name = "Melody Message",
    description = "Helpful messages for the melody terminal in floor 7."
) {
    private val sendMelodyMessage by BooleanSetting("Send Melody Message", true, desc = "Sends a message when the melody terminal opens.")
    private val melodyMessage by StringSetting("Melody Message", "Melody Terminal start!", 128, desc = "Message sent when the melody terminal opens.").withDependency { sendMelodyMessage }
    private val melodyProgress by BooleanSetting("Melody Progress", false, desc = "Tells the party about melody terminal progress.")
    private val melodySendCoords by BooleanSetting("Melody Send Coords", false, desc = "Sends the coordinates of the melody terminal.").withDependency { melodyProgress }
    private val broadcast by BooleanSetting("Broadcast Progress", true, desc = "Broadcasts melody progress to all other odin users in your run using a websocket.")
    private val melodyGui by HUD("Progress GUI", "Shows a GUI with the progress of broadcasting odin users in the melody terminal.", true) {
        if (it) {
            drawMelody(MelodyData(3, 1, 2), 0)
            return@HUD 45f to 25f
        }

        if (!broadcast || !webSocket.connected) return@HUD 0f to 0f
        melodies.entries.forEachIndexed { i, (name, data) ->
            if (!showOwn && name == mc.session.username) return@forEachIndexed
            drawMelody(data, i)
        }
        45f to 25f
    }.withDependency { broadcast }

    // explicit boolean because showOwn is broken or something
    private val showOwn: Boolean by BooleanSetting("Show Own", false, desc = "Shows your own melody progress in the GUI.").withDependency { melodyGui.enabled && broadcast }

    val webSocket = webSocket {
        onMessage {
            val (user, type, slot) = try { gson.fromJson(it, UpdateMessage::class.java) } catch (_: Exception) { return@onMessage }
            val entry = melodies.getOrPut(user) { MelodyData(null, null, null) }
            when (type) {
                0 -> melodies.remove(user)
                1 -> entry.clay = slot
                2 -> entry.purple = slot
                5 -> entry.pane = slot
            }
        }
    }

    private val melodies = ConcurrentHashMap<String, MelodyData>()
    private val lastSent = MelodyData(null, null, null)

    init {
        onMessage(Regex("^\\[BOSS] Goldor: Who dares trespass into my domain\\?$"), { enabled && broadcast }) {
            webSocket.connect("${wsServer}${LocationUtils.lobbyId}")
        }

        onMessage(Regex("^The Core entrance is opening!$"), { enabled && broadcast }) {
            webSocket.shutdown()
            melodies.clear()
        }

        onWorldLoad {
            webSocket.shutdown()
            melodies.clear()
        }

        onPacket<S2FPacketSetSlot>({ enabled && broadcast }) {
            val term = TerminalSolver.currentTerm ?: return@onPacket
            if (DungeonUtils.getF7Phase() != M7Phases.P3 || term.type != TerminalTypes.MELODY || it.func_149173_d() !in 0 until term.type.windowSize || mc.currentScreen is TermSimGUI) return@onPacket

            val meta = it.func_149174_e()?.metadata ?: return@onPacket

            val clay = Item.getIdFromItem(it.func_149174_e().item) == 159
            if (clay && meta != 5) return@onPacket

            if (clay) {
                val position = it.func_149173_d() / 9
                if (lastSent.clay == position) return@onPacket
                webSocket.send(update(1, position))
                lastSent.clay = position
                if (melodyProgress) clayProgress[position]?.let { partyMessage(it) }
                return@onPacket
            }

            if (meta != 5 && meta != 2) return@onPacket

            val index = mapToRange(it.func_149173_d()) ?: return@onPacket

            val shouldSend = when (meta) {
                2 -> lastSent.purple != index
                5 -> lastSent.pane != index
                else -> false
            }

            if (!shouldSend) return@onPacket
            webSocket.send(update(meta, index))
            when (meta) {
                2 -> lastSent.purple = index
                5 -> lastSent.pane = index
            }
        }
    }

    @SubscribeEvent
    fun onClose(event: TerminalEvent.Closed) {
        if (event.terminal.type != TerminalTypes.MELODY) return
        webSocket.send(update(0, 0))
    }

    private val clayProgress = hashMapOf(1 to "Melody 25%", 2 to "Melody 50%", 3 to "Melody 75%")

    @SubscribeEvent
    fun onTermLoad(event: TerminalEvent.Opened) {
        if (DungeonUtils.getF7Phase() != M7Phases.P3 || event.terminal.type != TerminalTypes.MELODY || mc.currentScreen is TermSimGUI) return
        if (sendMelodyMessage) partyMessage(melodyMessage)
        if (melodySendCoords) sendCommand("od sendcoords", true)
    }

    fun update(type: Int, slot: Int): String = gson.toJson(UpdateMessage(mc.session.username, type, slot))

    val ranges = listOf(1..5, 10..14, 19..23, 28..32, 37..41)

    fun mapToRange(value: Int): Int? {
        for (r in ranges) {
            if (value in r) return (value - r.first) % 5
        }
        return null
    }

    private val width = getTextWidth("§d■").toFloat()

    fun drawMelody(data: MelodyData, index: Int) {
        val y = width * 2 * index

        repeat(5) {
            if (data.purple == it) RenderUtils.drawText("§d■", width * it, y)
            val color = if (data.pane == it) "§a" else "§f"
            RenderUtils.drawText("${color}■", width * it, y + width)
        }
        data.clay?.let { RenderUtils.drawText(it.toString(), 40f, y + width / 2) }
    }

    data class UpdateMessage(val user: String, val type: Int, val slot: Int)
    data class MelodyData(var purple: Int?, var pane: Int?, var clay: Int?)
}