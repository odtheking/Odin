package me.odinclient.features.impl.render

import me.odinclient.mixin.accessors.IEntityPlayerSPAccessor
import me.odinclient.utils.skyblock.PlayerUtils
import me.odinmain.events.impl.ClickEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.dungeon.DungeonWaypoints.toBlockPos
import me.odinmain.features.impl.dungeon.DungeonWaypoints.toVec3
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.DualSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.PositionLook
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.equal
import me.odinmain.utils.etherwarpRotateTo
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.RenderUtils.renderVec
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.EtherWarpHelper
import me.odinmain.utils.skyblock.EtherWarpHelper.etherPos
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.extraAttributes
import me.odinmain.utils.skyblock.getBlockAt
import me.odinmain.utils.skyblock.holdingEtherWarp
import me.odinmain.utils.smoothRotateTo
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.absoluteValue

object EtherWarpHelper : Module(
    name = "Ether Warp Helper",
    description = "Shows you where your etherwarp will teleport you.",
    category = Category.RENDER
) {
    private val zeroPing: Boolean by BooleanSetting("Zero Ping", false)
    private val render: Boolean by BooleanSetting("Show Etherwarp Guess", true)
    private val useServerPosition: Boolean by DualSetting("Positioning", "Server Pos", "Player Pos", description = "If etherwarp guess should use your server position or real position.").withDependency { render }
    private val renderFail: Boolean by BooleanSetting("Show when failed", true).withDependency { render }
    private val renderColor: Color by ColorSetting("Color", Color.ORANGE.withAlpha(.5f), allowAlpha = true).withDependency { render }
    private val wrongColor: Color by ColorSetting("Wrong Color", Color.RED.withAlpha(.5f), allowAlpha = true).withDependency { renderFail }
    private val filled: Boolean by DualSetting("Type", "Outline", "Filled", default = false).withDependency { render }
    private val thickness: Float by NumberSetting("Thickness", 3f, 1f, 10f, .1f).withDependency { !filled && render }
    private val phase: Boolean by BooleanSetting("Phase", false).withDependency { render }
    private val etherWarpTriggerBot: Boolean by BooleanSetting("Trigger Bot", false, description = "Uses Dungeon Waypoints to trigger bot to the closest waypoint.")
    private val etherWarpTBDelay: Long by NumberSetting("Trigger Bot Delay", 200L, 0, 1000, 10).withDependency { etherWarpTriggerBot }
    private val etherWarpHelper: Boolean by BooleanSetting("(MIGHT BAN) Rotator", false, description = "Rotates you to the closest waypoint when you left click with aotv.")
    private val rotTime: Long by NumberSetting("Rotation Time", 150L, 10L, 600L, 1L).withDependency { etherWarpHelper }
    private val maxRot: Float by NumberSetting("Max Rotation", 90f, 0f, 360f, 1f).withDependency { etherWarpHelper }

    private val tbClock = Clock(etherWarpTBDelay)

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        if (
            etherWarpTriggerBot &&
            tbClock.hasTimePassed(etherWarpTBDelay) &&
            DungeonUtils.currentRoom?.waypoints?.any { etherPos.vec?.equal(it.toVec3()) == true } == true &&
            mc.thePlayer.isSneaking &&
            mc.thePlayer.holdingEtherWarp
        ) {
            tbClock.update()
            PlayerUtils.rightClick()
        }

        val player = mc.thePlayer as? IEntityPlayerSPAccessor ?: return
        val positionLook =
            if (useServerPosition)
                PositionLook(Vec3(player.lastReportedPosX, player.lastReportedPosY, player.lastReportedPosZ), player.lastReportedYaw, player.lastReportedPitch)
            else
                PositionLook(mc.thePlayer.renderVec, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)

        etherPos = EtherWarpHelper.getEtherPos(positionLook)
        if (render && mc.thePlayer.isSneaking && mc.thePlayer.heldItem.extraAttributes?.getBoolean("ethermerge") == true && (etherPos.succeeded || renderFail)) {
            val pos = etherPos.pos ?: return
            val color = if (etherPos.succeeded) renderColor else wrongColor
            getBlockAt(pos).setBlockBoundsBasedOnState(mc.theWorld, pos)
            val aabb = getBlockAt(pos).getSelectedBoundingBox(mc.theWorld, pos) ?: return

            Renderer.drawBox(aabb, color, outlineWidth = thickness, depth = phase, outlineAlpha = if (filled) 0 else 1, fillAlpha = if (filled) 1 else 0)
        }
    }

    @SubscribeEvent
    fun onClick(event: ClickEvent.RightClickEvent) {
        if (
            zeroPing &&
            mc.thePlayer.holdingEtherWarp &&
            etherPos.succeeded &&
            mc.thePlayer.isSneaking
        ) {
            val pos = etherPos.pos ?: return
            mc.thePlayer.setPosition(pos.x + .5, pos.y + 1.0, pos.z + .5)
            mc.thePlayer.setVelocity(.0, .0, .0)
        }
    }

    @SubscribeEvent
    fun onLeftClick(event: ClickEvent.LeftClickEvent) {
        if (
            etherWarpHelper &&
            mc.thePlayer.holdingEtherWarp &&
            mc.thePlayer.isSneaking
        ) {
            val waypoints = DungeonUtils.currentRoom?.waypoints ?: return
            val wp = waypoints.mapNotNull {
                etherwarpRotateTo(it.toBlockPos()) ?: return@mapNotNull null
            }.minByOrNull {
                val (_, yaw, pitch) = it

                (yaw - MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw)).absoluteValue +
                (pitch - MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationPitch)).absoluteValue
            } ?: return
            val (_, yaw, pitch) = wp
            if (
                (yaw - MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw)).absoluteValue +
                (pitch - MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationPitch)).absoluteValue > maxRot
            ) return
            smoothRotateTo(yaw, pitch, rotTime)
        }
    }
}