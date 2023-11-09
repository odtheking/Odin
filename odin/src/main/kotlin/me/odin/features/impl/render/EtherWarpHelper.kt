package me.odin.features.impl.render

import me.odin.mixin.accessors.IEntityPlayerSPAccessor
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.DualSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.world.RenderUtils
import me.odinmain.utils.skyblock.EtherWarpHelper
import me.odinmain.utils.skyblock.EtherWarpHelper.etherPos
import me.odinmain.utils.skyblock.ItemUtils.extraAttributes
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object EtherWarpHelper : Module(
    name = "Ether Warp Helper",
    description = "Shows you where your etherwarp will teleport you.",
    category = Category.RENDER
) {

    private val color: Color by ColorSetting("Color", Color.ORANGE.withAlpha(.5f), allowAlpha = true)
    private val filled: Boolean by DualSetting("Type", "Outline", "Filled", default = false)
    private val thickness: Float by NumberSetting("Thickness", 3f, 1f, 10f, .1f).withDependency { !filled }
    private val phase: Boolean by BooleanSetting("Phase", false)

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        val player = mc.thePlayer as? IEntityPlayerSPAccessor ?: return
        etherPos = EtherWarpHelper.getEtherPos(Vec3(player.lastReportedPosX, player.lastReportedPosY, player.lastReportedPosZ), yaw = player.lastReportedYaw, pitch = player.lastReportedPitch)
        if (etherPos.succeeded && mc.thePlayer.isSneaking && mc.thePlayer.heldItem.extraAttributes?.getBoolean("ethermerge") == true) {
            val pos = etherPos.pos ?: return

            if (filled)
                RenderUtils.drawFilledBox(pos, color, phase = phase)
            else
                RenderUtils.drawCustomESPBox(pos, color, thickness = thickness, phase = phase)
        }
    }


}