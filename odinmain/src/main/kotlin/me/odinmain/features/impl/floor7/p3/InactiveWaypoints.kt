package me.odinmain.features.impl.floor7.p3

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.*
import me.odinmain.utils.render.*
import me.odinmain.utils.skyblock.Island
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
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
    private val showTerminals: Boolean by BooleanSetting(name = "Show Terminals", default = true, description = "Shows inactive terminals")
    private val showDevices: Boolean by BooleanSetting(name = "Show Devices", default = true, description = "Shows inactive devices")
    private val showLevers: Boolean by BooleanSetting(name = "Show Levers", default = true, description = "Shows inactive levers")
    private val renderText: Boolean by BooleanSetting(name = "Render Text", default = true, description = "Renders the name of the inactive waypoint")
    private val renderBeacon: Boolean by BooleanSetting(name = "Render Beacon", default = true, description = "Renders a beacon beam on the inactive waypoint")
    private val renderBox: Boolean by BooleanSetting(name = "Render Box", default = true, description = "Renders a box around the inactive waypoint")
    private val hideDefault: Boolean by BooleanSetting(name = "Hide Default", default = true, description = "Hide the Hypixel names of Inactive Terminals")
    private val style: Int by SelectorSetting("Style", "Filled", arrayListOf("Filled", "Outline", "Filled Outline"), description = "Whether or not the box should be filled.").withDependency { renderBox }
    private val color: Color by OldColorSetting("Color", Color.RED.withAlpha(.5f), true, description = "The color of the inactive waypoints")

    private var inactiveList = listOf<Entity>()

    init {
        execute(500) {
            if (DungeonUtils.getPhase() != Island.M7P3) return@execute
            inactiveList = mc.theWorld?.loadedEntityList?.filter {
                it is EntityArmorStand && it.name.noControlCodes.containsOneOf("Inactive", "Not Activated", "CLICK HERE", ignoreCase = true)
            } ?: emptyList()
        }

        onWorldLoad {
            inactiveList = emptyList()
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (inactiveList.isEmpty()) return
        profile("Inactive Waypoints") { inactiveList.forEach {
            var name = it.name.noControlCodes
            if ((name == "Inactive Terminal" && showTerminals) || (name == "Inactive" && showDevices) || (name == "Not Activated" && showLevers)) {
                name = if (name == "Inactive Terminal") "Terminal" else if (name == "Inactive") "Device" else "Lever"
                if (renderBox)
                    Renderer.drawBox(it.position.toAABB(), color, 2f, depth = false, outlineAlpha = if (style == 0) 0 else color.alpha, fillAlpha = if (style == 1) 0 else color.alpha)
                if (renderText)
                    Renderer.drawStringInWorld(name, it.positionVector.add(Vec3(0.0, 2.0, 0.0)), depth = false, color = Color.WHITE, scale = 0.05f)
                if (renderBeacon)
                    RenderUtils.drawBeaconBeam(it.positionVector.addVec(-0.5, z = -0.5), color, false)
            }
            it.alwaysRenderNameTag = !hideDefault
        }}
    }
}