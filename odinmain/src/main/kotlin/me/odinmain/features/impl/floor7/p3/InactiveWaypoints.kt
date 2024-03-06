package me.odinmain.features.impl.floor7.p3

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.toAABB
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent


object InactiveWaypoints : Module(
    name = "Inactive Waypoints",
    category = Category.FLOOR7,
    description = "Shows inactive terminals, devices and levers"
) {

    private val showTerminals: Boolean by BooleanSetting(name = "Show Terminals", default = true, description = "Shows inactive terminals")
    private val showDevices: Boolean by BooleanSetting(name = "Show Devices", default = true, description = "Shows inactive devices")
    private val showLevers: Boolean by BooleanSetting(name = "Show Levers", default = true, description = "Shows inactive levers")
    private val renderText: Boolean by BooleanSetting(name = "Render Text", default = true, description = "Renders the name of the inactive waypoint")
    private val renderMode: Int by SelectorSetting("Render", "Both", arrayListOf("Both", "Outline", "Filled"), description = "How to render the inactive waypoints")
    private val color: Color by ColorSetting("Color", Color.RED.withAlpha(.5f), true, description = "The color of the inactive waypoints")

    private var inactiveList = listOf<Entity>()

    init {
        execute(1000) {
            if (!enabled) return@execute
            inactiveList = mc.theWorld?.loadedEntityList?.filter { it is EntityArmorStand &&
                    (it.name.noControlCodes.contains("Inactive", true) ||
                    it.name.noControlCodes.contains("Not Activated", true)) } ?: emptyList()

        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        inactiveList.forEach {
            var name = it.name.noControlCodes
            if ((name == "Inactive Terminal" && showTerminals) || (name == "Inactive" && showDevices) || (name == "Not Activated" && showLevers)) {
                name = if (name == "Inactive Terminal") "Terminal" else if (name == "Inactive") "Device" else "Lever"
                Renderer.drawBox(it.position.toAABB(), color, 2f, depth = true, fillAlpha = if (renderMode == 2) color.alpha else 0f, outlineAlpha = if (renderMode == 1) color.alpha else 0f)
                if (renderText)
                    Renderer.drawStringInWorld(name, it.positionVector.add(Vec3(0.0, 2.0, 0.0)), depth = true, color = color, scale = 0.03f)

            }
        }
    }

}