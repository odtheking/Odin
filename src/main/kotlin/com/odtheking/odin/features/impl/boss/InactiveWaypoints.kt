package com.odtheking.odin.features.impl.boss

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.events.EntityEvent
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.MessageEvent
import com.odtheking.odin.events.RenderExtractEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.addVec
import com.odtheking.odin.utils.containsOneOf
import com.odtheking.odin.utils.render.*
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.M7Phases
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.phys.AABB

object InactiveWaypoints : Module(
    name = "Inactive Waypoints",
    description = "Shows inactive terminals, devices and levers."
) {
    private val show by SelectorSetting("Show", Show.All, desc = "Which inactive waypoints to show.")
    private val style by SelectorSetting("Style", Style.Full, desc = "How the inactive waypoints are rendered.")
    private val color by ColorSetting("Waypoint color", Colors.MINECRAFT_YELLOW, true, desc = "The color of the waypoints.")
    private val throughWalls by BooleanSetting("Through Walls", true, desc = "Waypoints show through walls.")
    private val hideDefault by BooleanSetting("Hide Default", true, desc = "Hides Hypixel's floating names above inactive waypoints.")

    private enum class Show(private val displayName: String, val terminals: Boolean, val devices: Boolean, val levers: Boolean) {
        All("All", true, true, true),
        TermsAndLevers("Terms & Levers", true, false, true),
        Terminals("Terminals", true, false, false),
        Devices("Devices", false, true, false),
        Levers("Levers", false, false, true);

        override fun toString() = displayName
    }

    private enum class Style(private val displayName: String, val box: Boolean, val text: Boolean, val beacon: Boolean) {
        Full("Box, Text & Beacon", true, true, true),
        BoxText("Box & Text", true, true, false),
        Box("Box", true, false, false),
        Text("Text", false, true, false);

        override fun toString() = displayName
    }

    private enum class Kind(val label: String?) {
        Terminal("Terminal"), Device("Device"), Lever("Lever"), Other(null);

        val shown get() = when (this) {
            Terminal -> show.terminals
            Device -> show.devices
            Lever -> show.levers
            Other -> false
        }

        companion object {
            fun of(name: String): Kind? = when (name) {
                "Inactive Terminal" -> Terminal
                "Inactive" -> Device
                "Not Activated" -> Lever
                else -> if (name.containsOneOf("Inactive", "Not Activated", "CLICK HERE", ignoreCase = true)) Other else null
            }
        }
    }

    private val hud by HUD("Term Info", "Shows information about the terminals, levers and devices in the dungeon.") {
        if (!(DungeonUtils.inBoss && shouldRender) && !it) return@HUD 0 to 0
        val y = 0
        val width = textDim("§6Levers ${if (levers == 2) "§a" else "§c"}${levers}§8/§a2", 0, y, Colors.WHITE).first
        text("§6Terms ${if ((section == 2 && terminals == 5) || (section != 2 && terminals == 4)) "§a" else "§c"}${terminals}§8/§a${if (section == 2) 5 else 4}", 0, y + 9, Colors.WHITE)
        text("§6Device ${if (device) "§a✔" else "§c✘"}", 0, y + 18, Colors.WHITE)
        text("§6Gate ${if (gate) "§a✔" else "§c✘"}", 0, y + 27, Colors.WHITE)

        width to 36
    }

    private val inactive = hashMapOf<ArmorStand, Kind>()
    private var firstInSection = false
    private var shouldRender = false
    private var isComplete = false
    private var lastCompleted = 0
    private var device = false
    private var terminals = 0
    private var gate = false
    private var section = 1
    private var levers = 0

    private val completedRegex = Regex("^(.{1,16}) (activated|completed) a (terminal|lever|device)! \\((\\d)/(\\d)\\)$")
    private val goldorRegex = Regex("^\\[BOSS] Goldor: Who dares trespass into my domain\\?$")
    private val coreOpeningRegex = Regex("^The Core entrance is opening!$")
    private val gateRegex = Regex("^The gate has been destroyed!$")

    init {
        on<EntityEvent.SetData> {
            if (DungeonUtils.getF7Phase() != M7Phases.P3) return@on
            val stand = entity as? ArmorStand ?: return@on

            Kind.of(stand.name.string)?.let { inactive[stand] = it } ?: inactive.remove(stand)
        }

        on<EntityEvent.Remove> {
            (entity as? ArmorStand)?.let { inactive.remove(it) }
        }

        on<MessageEvent.Chat> {
            if (!DungeonUtils.inBoss) return@on

            when {
                completedRegex.matches(message) -> {
                    val it = completedRegex.find(message) ?: return@on
                    val completed = (it.groupValues[4].toIntOrNull() ?: 0).apply { if (this == 1) firstInSection = true }

                    if (completed == (it.groupValues[5].toIntOrNull() ?: 0)) {
                        if (gate) newSection() else isComplete = true
                        return@on
                    }

                    when (it.groupValues[3]) {
                        "lever" -> levers++
                        "terminal" -> terminals++
                        "device" -> if (!firstInSection || lastCompleted != completed) device = true
                    }
                    lastCompleted = completed
                }

                gateRegex.matches(message) -> {
                    gate = true
                    if (isComplete) newSection()
                }

                goldorRegex.matches(message) -> {
                    shouldRender = true
                    resetState()
                    section = 1
                }

                coreOpeningRegex.matches(message) -> {
                    inactive.clear()
                    shouldRender = false
                    resetState()
                }
            }
        }

        on<LevelEvent.Load> {
            inactive.clear()
            shouldRender = false
            resetState()
        }

        on<RenderExtractEvent> {
            if (inactive.isEmpty() || DungeonUtils.getF7Phase() != M7Phases.P3) return@on
            inactive.forEach { (stand, kind) ->
                stand.isCustomNameVisible = !hideDefault
                val label = kind.label?.takeIf { kind.shown } ?: return@forEach

                if (style.box) drawWireFrameBox(AABB.unitCubeFromLowerCorner(stand.position().addVec(-0.5, z = -0.5)), color, depth = !throughWalls)
                if (style.text) drawText(label, stand.position().addVec(y = 2.0), 1.5f, true)
                if (style.beacon) drawBeaconBeam(stand.blockPosition(), color)
            }
        }
    }

    private fun resetState() {
        firstInSection = false
        lastCompleted = 0
        isComplete = false
        device = false
        terminals = 0
        gate = false
        section = 1
        levers = 0
    }

    private fun newSection() {
        firstInSection = false
        isComplete = false
        device = false
        terminals = 0
        gate = false
        levers = 0
        section++
    }
}