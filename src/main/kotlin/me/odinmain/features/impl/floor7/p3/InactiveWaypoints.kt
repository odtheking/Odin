package me.odinmain.features.impl.floor7.p3

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.*
import me.odinmain.utils.render.*
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.M7Phases
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object InactiveWaypoints : Module(
    name = "Inactive Waypoints",
    category = Category.FLOOR7,
    description = "Shows inactive terminals, devices and levers."
) {
    private val showTerminals by BooleanSetting(name = "Show Terminals", default = true, description = "Shows inactive terminals.")
    private val showDevices by BooleanSetting(name = "Show Devices", default = true, description = "Shows inactive devices.")
    private val showLevers by BooleanSetting(name = "Show Levers", default = true, description = "Shows inactive levers.")
    private val renderText by BooleanSetting(name = "Render Text", default = true, description = "Renders the name of the inactive waypoint.")
    private val renderBeacon by BooleanSetting(name = "Render Beacon", default = true, description = "Renders a beacon beam on the inactive waypoint.")
    private val renderBox by BooleanSetting(name = "Render Box", default = true, description = "Renders a box around the inactive waypoint.")
    private val hideDefault by BooleanSetting(name = "Hide Default", default = true, description = "Hide the Hypixel names of Inactive Terminals.")
    private val style by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, description = Renderer.STYLE_DESCRIPTION)
    private val color by ColorSetting("Color", Color(0, 0, 0, 0.4f), allowAlpha = true, description = "The color of the box.")
    private val lineWidth by NumberSetting("Line Width", 2f, 0.1f, 10f, 0.1f, description = "The width of the box's lines.")
    private val depthCheck by BooleanSetting("Depth check", false, description = "Boxes show through walls.")

    private var inactiveList = listOf<Entity>()

    init {
        execute(500) {
            if (DungeonUtils.getF7Phase() != M7Phases.P3) return@execute
            inactiveList = mc.theWorld?.loadedEntityList?.filter {
                it is EntityArmorStand && it.name.noControlCodes.containsOneOf("Inactive", "Not Activated", "CLICK HERE", ignoreCase = true) }.orEmpty()
        }

        onWorldLoad {
            inactiveList = emptyList()
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (inactiveList.isEmpty()) return
        profile("Inactive Waypoints") {
            inactiveList.forEach {
                var name = it.name.noControlCodes
                if ((name == "Inactive Terminal" && showTerminals) || (name == "Inactive" && showDevices) || (name == "Not Activated" && showLevers)) {
                    name = if (name == "Inactive Terminal") "Terminal" else if (name == "Inactive") "Device" else "Lever"
                    if (renderBox)
                        Renderer.drawStyledBox(it.positionVector.addVec(-0.5, z = -0.5).toAABB(), color, style, lineWidth, depthCheck)
                    if (renderText)
                        Renderer.drawStringInWorld(name, it.positionVector.add(Vec3(0.0, 2.0, 0.0)), depth = false, color = Color.WHITE, scale = 0.05f)
                    if (renderBeacon)
                        RenderUtils.drawBeaconBeam(it.positionVector.addVec(-0.5, z = -0.5), color, true)
                }
                it.alwaysRenderNameTag = !hideDefault
            }
        }
    }
}