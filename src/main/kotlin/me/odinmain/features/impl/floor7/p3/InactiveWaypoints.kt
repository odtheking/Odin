package me.odinmain.features.impl.floor7.p3

import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.utils.*
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.M7Phases
import me.odinmain.utils.ui.Colors
import me.odinmain.utils.ui.clickgui.util.ColorUtil.withAlpha
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object InactiveWaypoints : Module(
    name = "Inactive Waypoints",
    desc = "Shows inactive terminals, devices and levers."
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

    private var inactiveList = setOf<Entity>()

    init {
        execute(500) {
            if (DungeonUtils.getF7Phase() != M7Phases.P3) return@execute
            inactiveList = mc.theWorld?.loadedEntityList?.filter {
                it is EntityArmorStand && it.name.noControlCodes.containsOneOf("Inactive", "Not Activated", "CLICK HERE", ignoreCase = true) }?.toSet().orEmpty()
        }

        onWorldLoad { inactiveList = emptySet() }
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