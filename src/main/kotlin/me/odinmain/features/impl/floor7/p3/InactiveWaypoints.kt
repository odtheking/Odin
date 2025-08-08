package me.odinmain.features.impl.floor7.p3

import me.odinmain.clickgui.settings.impl.BooleanSetting
import me.odinmain.clickgui.settings.impl.ColorSetting
import me.odinmain.clickgui.settings.impl.NumberSetting
import me.odinmain.clickgui.settings.impl.SelectorSetting
import me.odinmain.features.Module
import me.odinmain.utils.*
import me.odinmain.utils.render.Color.Companion.withAlpha
import me.odinmain.utils.render.Colors
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.M7Phases
import me.odinmain.utils.ui.drawStringWidth
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

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
    private val style by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, desc = Renderer.STYLE_DESCRIPTION)
    private val color by ColorSetting("Color", Colors.MINECRAFT_BLUE.withAlpha(.4f), allowAlpha = true, desc = "The color of the box.")
    private val lineWidth by NumberSetting("Line Width", 2f, 0.1f, 10f, 0.1f, desc = "The width of the box's lines.")
    private val depthCheck by BooleanSetting("Depth check", false, desc = "Boxes show through walls.")

    private val hud by HUD("Term Info", "Shows information about the terminals, levers and devices in the dungeon.") {
        if (!(DungeonUtils.inBoss && shouldRender) && !it) return@HUD 0f to 0f
        val y = 1f
        val width = drawStringWidth("§6Levers ${if (levers == 2) "§a" else "§c"}${levers}§8/§a2", 1f, y, Colors.WHITE)
        RenderUtils.drawText("§6Terms ${if ((section == 2 && terminals == 5) || (section != 2 && terminals == 4)) "§a" else "§c"}${terminals}§8/§a${if (section == 2) 5 else 4}", 1f, y + 10f, Colors.WHITE)
        RenderUtils.drawText("§6Device ${if (device) "§a✔" else "§c✘"}", 1f, y + 20f, Colors.WHITE)
        RenderUtils.drawText("§6Gate ${if (gate) "§a✔" else "§c✘"}", 1f, y + 30f, Colors.WHITE)

        width + 1f to 40f
    }

    private var inactiveList = setOf<Entity>()
    private var firstInSection = false
    private var shouldRender = false
    private var isComplete = false
    private var lastCompleted = 0
    private var device = false
    private var terminals = 0
    private var gate = false
    private var section = 1
    private var levers = 0

    init {
        execute(500) {
            if (DungeonUtils.getF7Phase() != M7Phases.P3) return@execute
            inactiveList = mc.theWorld?.loadedEntityList?.filter {
                it is EntityArmorStand && it.name.noControlCodes.containsOneOf("Inactive", "Not Activated", "CLICK HERE", ignoreCase = true) }?.toSet().orEmpty()
        }

        onMessage(Regex("^(.{1,16}) (activated|completed) a (terminal|lever|device)! \\((\\d)/(\\d)\\)$")) {
            val completed = (it.groupValues[4].toIntOrNull() ?: 0).apply { if (this == 1) firstInSection = true }

            if (completed == (it.groupValues[5].toIntOrNull() ?: 0)) {
                if (gate) newSection()
                else isComplete = true
                return@onMessage
            }

            when (it.groupValues[3]) {
                "lever" -> levers++

                "terminal" -> terminals++

                "device" -> if (!firstInSection || lastCompleted != completed) device = true
            }
            lastCompleted = completed
        }

        onMessage(Regex("^The gate has been destroyed!$")) {
            gate = true
            if (isComplete) newSection()
        }

        onWorldLoad {
            shouldRender = false
            resetState()
        }

        onMessage(Regex("^\\[BOSS] Goldor: Who dares trespass into my domain\\?$")) {
            shouldRender = true
            resetState()
            section = 1
        }

        onMessage(Regex("^The Core entrance is opening!$")) {
            shouldRender = false
            resetState()
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

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (inactiveList.isEmpty() || DungeonUtils.getF7Phase() != M7Phases.P3) return
        profile("Inactive Waypoints") {
            inactiveList.forEach {
                var name = it.name.noControlCodes
                if ((name == "Inactive Terminal" && showTerminals) || (name == "Inactive" && showDevices) || (name == "Not Activated" && showLevers)) {
                    name = if (name == "Inactive Terminal") "Terminal" else if (name == "Inactive") "Device" else "Lever"
                    if (renderBox)
                        Renderer.drawStyledBox(it.positionVector.addVec(-0.5, z = -0.5).toAABB(), color, style, lineWidth, depthCheck)
                    if (renderText)
                        Renderer.drawStringInWorld(name, it.positionVector.add(Vec3(0.0, 2.0, 0.0)), depth = false, color = Colors.WHITE, scale = 0.05f)
                    if (renderBeacon)
                        RenderUtils.drawBeaconBeam(it.positionVector.addVec(-0.5, z = -0.5), color, true)
                }
                it.alwaysRenderNameTag = !hideDefault
            }
        }
    }
}