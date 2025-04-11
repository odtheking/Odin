package me.odinclient.features.impl.render

import me.odinclient.mixin.accessors.IEntityPlayerSPAccessor
import me.odinclient.utils.skyblock.PlayerUtils
import me.odinmain.events.impl.ClickEvent
import me.odinmain.events.impl.PacketEvent
import me.odinmain.features.Module
import me.odinmain.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.toBlockPos
import me.odinmain.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.toVec3
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
import me.odinmain.utils.ui.Colors
import me.odinmain.utils.ui.clickgui.util.ColorUtil.withAlpha
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.network.play.client.C03PacketPlayer.C05PacketPlayerLook
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.abs
import kotlin.math.absoluteValue

object EtherWarpHelper : Module(
    name = "Etherwarp Helper",
    description = "Provides configurable visual and audio feedback for etherwarp."
) {
    private val zeroPing by BooleanSetting("Zero Ping", false, description = "Teleports you to the exact position of the etherwarp.").withDependency { !LocationUtils.isOnHypixel }
    private val keepMotion by BooleanSetting("Keep Motion", true, description = "If you should keep your motion after zero ping etherwarp.").withDependency { zeroPing }
    private val render by BooleanSetting("Show Etherwarp Guess", true, description = "Shows where etherwarp will take you.")
    private val color by ColorSetting("Color", Colors.MINECRAFT_GOLD.withAlpha(.5f), allowAlpha = true, description = "Color of the box.").withDependency { render }
    private val renderFail by BooleanSetting("Show when failed", true, description = "Shows the box even when the guess failed.").withDependency { render }
    private val wrongColor by ColorSetting("Wrong Color", Colors.MINECRAFT_RED.withAlpha(.5f), allowAlpha = true, description = "Color of the box if guess failed.").withDependency { renderFail }

    private val style by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, description = Renderer.STYLE_DESCRIPTION).withDependency { render }
    private val lineWidth by NumberSetting("Line Width", 2f, 0.1f, 10f, 0.1f, description = "The width of the box's lines.").withDependency { render }
    private val depthCheck by BooleanSetting("Depth check", false, description = "Boxes show through walls.").withDependency { render }
    private val fullBlock by BooleanSetting("Full Block", false, description = "If the box should be a full block.").withDependency { render }
    private val expand by NumberSetting("Expand", 0.0, -1, 1, 0.01, description = "Expands the box by this amount.").withDependency { render }
    private val useServerPosition by BooleanSetting("Use Server Position", true, description = "If etherwarp guess should use your server position or real position.").withDependency { render }

    private val etherwarpTBDropDown by DropdownSetting("Trigger Bot")
    private val etherWarpTriggerBot by BooleanSetting("Trigger Bot", false, description = "Uses Dungeon Waypoints to trigger bot to the closest waypoint.").withDependency { etherwarpTBDropDown }
    private val etherWarpTBDelay by NumberSetting("Trigger Bot Delay", 200L, 0, 1000, 10, description = "Delay between each trigger bot click.").withDependency { etherWarpTriggerBot && etherwarpTBDropDown }
    private val etherWarpHelper by BooleanSetting("(MIGHT BAN) Rotator", false, description = "Rotates you to the closest waypoint when you left click with aotv.").withDependency { etherwarpTBDropDown }
    private val rotTime by NumberSetting("Rotation Time", 150L, 10L, 600L, 1L, description = "Time it takes to rotate to the closest waypoint.").withDependency { etherWarpHelper && etherwarpTBDropDown }
    private val maxRot by NumberSetting("Max Rotation", 90f, 0f, 360f, 1f, description = "Max rotation difference to rotate to a waypoint.").withDependency { etherWarpHelper && etherwarpTBDropDown }

    private val dropdown by DropdownSetting("Sounds", false)
    private val sounds by BooleanSetting("Custom Sounds", false, description = "Plays the selected custom sound when you etherwarp.").withDependency { dropdown }
    private val defaultSounds = arrayListOf("mob.blaze.hit", "fire.ignite", "random.orb", "random.break", "mob.guardian.land.hit", "note.pling", "Custom")
    private val sound by SelectorSetting("Sound", "mob.blaze.hit", defaultSounds, description = "Which sound to play when you etherwarp.").withDependency { sounds && dropdown }
    private val customSound by StringSetting("Custom Sound", "mob.blaze.hit",
        description = "Name of a custom sound to play. This is used when Custom is selected in the Sound setting.", length = 32
    ).withDependency { sound == defaultSounds.size - 1 && sounds && dropdown }
    private val soundVolume by NumberSetting("Volume", 1f, 0, 1, .01f, description = "Volume of the sound.").withDependency { sounds && dropdown }
    private val soundPitch by NumberSetting("Pitch", 2f, 0, 2, .01f, description = "Pitch of the sound.").withDependency { sounds && dropdown }
    private val reset by ActionSetting("Play sound", description = "Plays the selected sound.") { playLoudSound(if (sound == defaultSounds.size - 1) customSound else defaultSounds[sound], soundVolume, soundPitch) }.withDependency { sounds && dropdown }

    private val tbClock = Clock(etherWarpTBDelay)

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        if (
            etherWarpTriggerBot &&
            tbClock.hasTimePassed(etherWarpTBDelay) &&
            DungeonUtils.currentRoom?.waypoints?.any { etherPos.vec?.equal(it.toVec3()) == true } == true &&
            mc.thePlayer.usingEtherWarp
        ) {
            tbClock.update()
            PlayerUtils.rightClick()
        }
        if (!mc.thePlayer.usingEtherWarp) return

        val player = mc.thePlayer as? IEntityPlayerSPAccessor ?: return
        val positionLook =
            if (useServerPosition)
                PositionLook(Vec3(player.lastReportedPosX, player.lastReportedPosY, player.lastReportedPosZ), player.lastReportedYaw, player.lastReportedPitch)
            else
                PositionLook(mc.thePlayer.renderVec, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)

        etherPos = EtherWarpHelper.getEtherPos(positionLook)
        if (render && (etherPos.succeeded || renderFail))
            if (!fullBlock)
                Renderer.drawStyledBlock(etherPos.pos ?: return, if (etherPos.succeeded) color else wrongColor, style, lineWidth, depthCheck, true, expand)
            else
                Renderer.drawStyledBox(etherPos.pos?.toAABB()?.expand(expand, expand, expand) ?: return, if (etherPos.succeeded) color else wrongColor, style, lineWidth, depthCheck)
    }

    @SubscribeEvent
    fun onLeftClick(event: ClickEvent.Left) {
        if (etherWarpHelper && mc.thePlayer.usingEtherWarp) {
            val (_, yaw, pitch) = DungeonUtils.currentRoom?.waypoints?.mapNotNull {
                etherwarpRotateTo(it.toBlockPos()) ?: return@mapNotNull null
            }?.minByOrNull {
                val (_, yaw, pitch) = it

                (yaw - MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw)).absoluteValue +
                (pitch - MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationPitch)).absoluteValue
            } ?: return
            if (
                (yaw - MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw)).absoluteValue +
                (pitch - MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationPitch)).absoluteValue > maxRot
            ) return
            smoothRotateTo(yaw, pitch, rotTime)
        }
    }

    @SubscribeEvent
    fun onSoundPacket(event: PacketEvent.Receive) = with(event.packet) {
        if (this !is S29PacketSoundEffect || soundName != "mob.enderdragon.hit" || !sounds || volume != 1f || pitch != 0.53968257f || customSound == "mob.enderdragon.hit") return
        playLoudSound(if (sound == defaultSounds.size - 1) customSound else defaultSounds[sound], soundVolume, soundPitch, positionVector)
        event.isCanceled = true
    }

    private var lastSentLook: Pair<Float, Float>? = null
    private const val FAIL_WATCH_PERIOD = 20
    private const val MAX_FAIL_PER_FAIL_PERIOD = 3
    private const val MAX_QUEUED_PACKETS = 3
    private val recentFails = mutableListOf<Long>()
    private val recentlySentC06s = mutableListOf<PacketData>()
    private var isSneaking = false

    data class PacketData(val pitch: Float, val yaw: Float, val x: Double, val y: Double, val z: Double)

    @SubscribeEvent
    fun onPacketSent(event: PacketEvent.Send) = with(event.packet) {
        when (this) {
            is C0BPacketEntityAction -> {
                when (action) {
                    C0BPacketEntityAction.Action.START_SNEAKING -> isSneaking = true
                    C0BPacketEntityAction.Action.STOP_SNEAKING -> isSneaking = false
                    else -> {}
                }
            }

            is C08PacketPlayerBlockPlacement -> {
                if (LocationUtils.isOnHypixel) return@with
                if (!zeroPing || placedBlockDirection != 255 || !mc.thePlayer.isHoldingEtherwarp() || lastSentLook == null ||
                    !isSneaking && mc.thePlayer.heldItem?.skyblockID != "ETHERWARP_CONDUIT" || getBlockIdAt(mc.objectMouseOver.blockPos).equalsOneOf(54, 146)) return@with

                if (!checkAllowedFails()) {
                    modMessage("Failed to etherwarp, too many failed attempts. ${recentFails.size} fails, ${recentlySentC06s.size} packets queued.")
                    return@with
                }
                doZeroPingEtherwarp()
            }

            is C05PacketPlayerLook -> lastSentLook = pitch to yaw
            is C06PacketPlayerPosLook -> lastSentLook = pitch to yaw
        }
    }

    @SubscribeEvent
    fun onPacketReceived(event: PacketEvent.Receive) = with(event.packet) {
        if (this !is S08PacketPlayerPosLook || recentlySentC06s.isEmpty()) return
        val (oldPitch, oldYaw, oldX, oldY, oldZ) = recentlySentC06s.removeFirst()

        val wasPredictionCorrect = isWithinTolerance(oldPitch, pitch) && isWithinTolerance(oldYaw, yaw) && oldX == x && oldY == y && oldZ == z

        if (wasPredictionCorrect || !LocationUtils.isOnHypixel) event.isCanceled = true
        else {
            recentFails.add(System.currentTimeMillis())
            recentlySentC06s.clear()
        }
    }

    private fun isWithinTolerance(n1: Float, n2: Float) = abs(n1 - n2) < 1e-4

    private fun checkAllowedFails(): Boolean {
        if (recentlySentC06s.size >= MAX_QUEUED_PACKETS) return false
        recentFails.removeIf { System.currentTimeMillis() - it > FAIL_WATCH_PERIOD * 1000 }
        return recentFails.size < MAX_FAIL_PER_FAIL_PERIOD
    }

    private fun doZeroPingEtherwarp() {
        if (!etherPos.succeeded) return
        var (pitch, yaw) = lastSentLook ?: return
        val (x, y, z) = etherPos.vec ?: return
        yaw %= 360
        if (yaw < 0) yaw += 360
        if (LocationUtils.isOnHypixel) recentlySentC06s.add(PacketData(pitch, yaw, x + 0.5, y + 1.05, z + 0.5))

        mc.addScheduledTask {
            mc.thePlayer.sendQueue.addToSendQueue(C06PacketPlayerPosLook(x + 0.5, y + 1.05, z + 0.5, yaw, pitch, mc.thePlayer.onGround))
            mc.thePlayer.setPosition(x + 0.5, y + 1.05, z + 0.5)
            if (!keepMotion) mc.thePlayer.setVelocity(0.0, 0.0, 0.0)
        }
    }

    private fun EntityPlayerSP.isHoldingEtherwarp(): Boolean =
        heldItem?.skyblockID == "ETHERWARP_CONDUIT" || heldItem?.extraAttributes?.getBoolean("ethermerge") == true
}