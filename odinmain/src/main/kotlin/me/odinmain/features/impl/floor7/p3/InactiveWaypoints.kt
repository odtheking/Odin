package me.odinmain.features.impl.floor7.p3

import me.odinmain.OdinMain
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.utils.VecUtils.toAABB
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.world.RenderUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object InactiveWaypoints : Module(
    name = "Inactive Waypoints",
    category = Category.FLOOR7,
    description = "Shows inactive terminals, devices and levers"
) {

    private val showTerminals: Boolean by BooleanSetting(name = "Show Terminals")
    private val showDevices: Boolean by BooleanSetting(name = "Show Devices")
    private val showLevers: Boolean by BooleanSetting(name = "Show Levers")
    private val renderMode: Int by SelectorSetting("Render", "Outline", arrayListOf("Outline", "Filled", "Both"))

    private var inactiveList = listOf<Entity>()

    init {
        execute(1000) {
            inactiveList = mc.theWorld?.loadedEntityList?.filter { it is EntityArmorStand && it.name.noControlCodes.contains("Inactive", true) || it.name.noControlCodes.contains("Not Activated", true) } ?: emptyList()
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        inactiveList.forEach {
            val name = it.name.noControlCodes
            if ((name == "Inactive Terminal" && showTerminals) || (name == "Inactive" && showDevices) || (name == "Not Activated" && showLevers)) {
                if (renderMode == 0) RenderUtils.drawCustomBox(it.position.toAABB(), Color.RED, 1f, phase = !OdinMain.onLegitVersion)
                if (renderMode == 1) RenderUtils.drawFilledBox(it.position.toAABB(), Color.RED, phase = !OdinMain.onLegitVersion)
                else {
                    RenderUtils.drawCustomBox(it.position.toAABB(), Color.RED, 1f, phase = !OdinMain.onLegitVersion)
                    RenderUtils.drawFilledBox(it.position.toAABB(), Color.RED, phase = !OdinMain.onLegitVersion)
                }
            }
        }
    }

}