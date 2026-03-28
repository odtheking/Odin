package com.odtheking.odin.features.impl.boss

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.addVec
import com.odtheking.odin.utils.containsOneOf
import com.odtheking.odin.utils.handlers.TickTask
import com.odtheking.odin.utils.render.*
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.M7Phases
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.phys.AABB

object InactiveWaypoints : Module(
    name = "Inactive Waypoints",
    description = "Shows inactive terminals, devices and levers."
) {
    private val showTerminals by BooleanSetting("Show Terminals", true, desc = "Shows inactive terminals.")
    private val showDevices by BooleanSetting("Show Devices", true, desc = "Shows inactive devices.")
    private val showLevers by BooleanSetting("Show Levers", true, desc = "Shows inactive levers.")
    private val renderText by BooleanSetting("Render Text", true, desc = "Renders the name of the inactive waypoint.")
    private val renderBeacon by BooleanSetting("Render Beacon", true, desc = "Renders a beacon beam on the inactive waypoint.")
    private val renderBox by BooleanSetting("Render Box", true, desc = "Renders a box around the inactive waypoint.")
    private val hideDefault by BooleanSetting("Hide Default", true, desc = "Hide the Hypixel names of Inactive Terminals.")
    private val color by ColorSetting("Color", Colors.MINECRAFT_YELLOW.withAlpha(.4f), true, desc = "The color of the box.")
    private val depthCheck by BooleanSetting("Depth check", false, desc = "Boxes show through walls.")

    private val hud by HUD("Term Info", "Shows information about the terminals, levers and devices in the dungeon.") {
        if (!(DungeonUtils.inBoss && shouldRender) && !it) return@HUD 0 to 0
        val y = 0
        val width = textDim("§6Levers ${if (levers == 2) "§a" else "§c"}${levers}§8/§a2", 0, y, Colors.WHITE).first
        text("§6Terms ${if ((section == 2 && terminals == 5) || (section != 2 && terminals == 4)) "§a" else "§c"}${terminals}§8/§a${if (section == 2) 5 else 4}", 0, y + 9, Colors.WHITE)
        text("§6Device ${if (device) "§a✔" else "§c✘"}", 0, y + 18, Colors.WHITE)
        text("§6Gate ${if (gate) "§a✔" else "§c✘"}", 0, y + 27, Colors.WHITE)

        width to 36
    }

    private var inactiveList = setOf<ArmorStand>()
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
        TickTask(10) {
            if (!enabled || DungeonUtils.getF7Phase() != M7Phases.P3) return@TickTask
            inactiveList = mc.level?.entitiesForRendering()?.filterIsInstance<ArmorStand>()?.filter {
                it.name.string.containsOneOf("Inactive", "Not Activated", "CLICK HERE", ignoreCase = true)
            }?.toSet().orEmpty()
        }

        on<ChatPacketEvent> {
            if (!DungeonUtils.inBoss) return@on

            when {
                completedRegex.matches(value) -> {
                    val it = completedRegex.find(value) ?: return@on
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

                gateRegex.matches(value) -> {
                    gate = true
                    if (isComplete) newSection()
                }

                goldorRegex.matches(value) -> {
                    shouldRender = true
                    resetState()
                    section = 1
                }

                coreOpeningRegex.matches(value) -> {
                    shouldRender = false
                    resetState()
                }
            }
        }

        on<WorldEvent.Load> {
            shouldRender = false
            resetState()
        }

        on<RenderEvent.Extract> {
            if (inactiveList.isEmpty() || DungeonUtils.getF7Phase() != M7Phases.P3) return@on
            inactiveList.forEach {
                val name = it.name.string
                if ((name == "Inactive Terminal" && showTerminals) || (name == "Inactive" && showDevices) || (name == "Not Activated" && showLevers)) {
                    val customName = if (name == "Inactive Terminal") "Terminal" else if (name == "Inactive") "Device" else "Lever"
                    if (renderBox)
                        drawWireFrameBox(AABB.unitCubeFromLowerCorner(it.position().addVec(-0.5, z = -0.5)), color, depth = depthCheck)
                    if (renderText)
                        drawText(customName, it.position().addVec(y = 2.0), 1.5f, true)
                    if (renderBeacon)
                        drawBeaconBeam(it.blockPosition(), color)
                }
                it.isCustomNameVisible = !hideDefault
            }
        }
    }

    private fun resetState() {
        inactiveList = emptySet()
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