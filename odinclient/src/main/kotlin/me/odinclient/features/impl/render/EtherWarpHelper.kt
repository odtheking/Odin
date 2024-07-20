package me.odinclient.features.impl.render

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.color.multiplyAlpha
import me.odinclient.mixin.accessors.IEntityPlayerSPAccessor
import me.odinclient.utils.skyblock.PlayerUtils
import me.odinmain.events.impl.ClickEvent
import me.odinmain.events.impl.PacketReceivedEvent
import me.odinmain.features.Module
import me.odinmain.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.toBlockPos
import me.odinmain.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.toVec3
import me.odinmain.features.impl.render.DevPlayers.isDev
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.*
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.render.RenderUtils.renderVec
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.EtherWarpHelper
import me.odinmain.utils.skyblock.EtherWarpHelper.etherPos
import me.odinmain.utils.skyblock.PlayerUtils.playLoudSound
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.absoluteValue

object EtherWarpHelper : Module(
    name = "Ether Warp Helper",
    description = "Shows you where your etherwarp will teleport you."
) {
    private val zeroPing by BooleanSetting("Zero Ping", false).withDependency { isDev }
    private val render by BooleanSetting("Show Etherwarp Guess", true)
    private val useServerPosition by SelectorSetting("Positioning", "Server Pos", arrayListOf("Server Pos", "Player Pos"), description = "If etherwarp guess should use your server position or real position.").withDependency { render }
    private val renderFail by BooleanSetting("Show when failed", true).withDependency { render }
    private val wrongColor by ColorSetting("Wrong Color", Color.RED.multiplyAlpha(.5f), allowAlpha = true).withDependency { renderFail }
    private val style by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, description = Renderer.STYLE_DESCRIPTION).withDependency { render }
    private val color by ColorSetting("Color", Color.MINECRAFT_GOLD.multiplyAlpha(.5f), allowAlpha = true).withDependency { render }
    private val lineWidth by NumberSetting("Line Width", 2f, 0.1f, 10f, 0.1f, description = "The width of the box's lines.").withDependency { render }
    private val depthCheck by BooleanSetting("Depth check", false, description = "Boxes show through walls.").withDependency { render }
    private val etherWarpTriggerBot by BooleanSetting("Trigger Bot", false, description = "Uses Dungeon Waypoints to trigger bot to the closest waypoint.")
    private val etherWarpTBDelay by NumberSetting("Trigger Bot Delay", 200L, 0, 1000, 10).withDependency { etherWarpTriggerBot }
    private val etherWarpHelper by BooleanSetting("(MIGHT BAN) Rotator", false, description = "Rotates you to the closest waypoint when you left click with aotv.")
    private val rotTime by NumberSetting("Rotation Time", 150L, 10L, 600L, 1L).withDependency { etherWarpHelper }
    private val maxRot by NumberSetting("Max Rotation", 90f, 0f, 360f, 1f).withDependency { etherWarpHelper }
    private val sounds by BooleanSetting("Custom Sounds", default = false)
    private val defaultSounds = arrayListOf("mob.blaze.hit", "fire.ignite", "random.orb", "random.break", "mob.guardian.land.hit", "note.pling", "Custom")
    private val sound by SelectorSetting("Sound", "mob.blaze.hit", defaultSounds, description = "Which sound to play when you etherwarp.").withDependency { sounds }
    private val customSound by StringSetting("Custom Sound", "mob.blaze.hit",
        description = "Name of a custom sound to play. This is used when Custom is selected in the Sound setting.", length = 32
    ).withDependency { sound == defaultSounds.size - 1 && sounds}
    private val soundVolume by NumberSetting("Volume", 1f, 0, 1, .01f, description = "Volume of the sound.").withDependency { sounds }
    private val soundPitch by NumberSetting("Pitch", 2f, 0, 2, .01f, description = "Pitch of the sound.").withDependency { sounds }
    val reset by ActionSetting("Play sound") { playLoudSound(if (sound == defaultSounds.size - 1) customSound else defaultSounds[sound], soundVolume, soundPitch) }.withDependency { sounds }

    private val tbClock = Clock(etherWarpTBDelay)

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        if (
            etherWarpTriggerBot &&
            tbClock.hasTimePassed(etherWarpTBDelay) &&
            DungeonUtils.currentFullRoom?.waypoints?.any { etherPos.vec?.equal(it.toVec3()) == true } == true &&
            mc.thePlayer.isSneaking &&
            mc.thePlayer.holdingEtherWarp
        ) {
            tbClock.update()
            PlayerUtils.rightClick()
        }

        val player = mc.thePlayer as? IEntityPlayerSPAccessor ?: return
        val positionLook =
            if (useServerPosition == 0)
                PositionLook(Vec3(player.lastReportedPosX, player.lastReportedPosY, player.lastReportedPosZ), player.lastReportedYaw, player.lastReportedPitch)
            else
                PositionLook(mc.thePlayer.renderVec, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)

        etherPos = EtherWarpHelper.getEtherPos(positionLook)
        if (render && mc.thePlayer.isSneaking && mc.thePlayer.heldItem.extraAttributes?.getBoolean("ethermerge") == true && (etherPos.succeeded || renderFail)) {
            val pos = etherPos.pos ?: return
            val color = if (etherPos.succeeded) color else wrongColor
            Renderer.drawStyledBlock(pos, color, style, lineWidth, depthCheck)
        }
    }

    @SubscribeEvent
    fun onClick(event: ClickEvent.RightClickEvent) {
        if (
            zeroPing &&
            mc.thePlayer.holdingEtherWarp &&
            etherPos.succeeded &&
            mc.thePlayer.isSneaking &&
            LocationUtils.currentArea.isArea(Island.SinglePlayer)
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
            val waypoints = DungeonUtils.currentFullRoom?.waypoints ?: return
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

    @SubscribeEvent
    fun onSoundPacket(event: PacketReceivedEvent) {
        with(event.packet) {
            if (this !is S29PacketSoundEffect || soundName != "mob.enderdragon.hit" || !sounds || volume != 1f || pitch != 0.53968257f || customSound == "mob.enderdragon.hit") return
            mc.addScheduledTask { playLoudSound(if (sound == defaultSounds.size - 1) customSound else defaultSounds[sound], soundVolume, soundPitch, pos) }
            event.isCanceled = true
        }
    }
}