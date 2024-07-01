package me.odin.features.impl.render

import me.odin.mixin.accessors.IEntityPlayerSPAccessor
import me.odinmain.events.impl.PacketReceivedEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.PositionLook
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.RenderUtils.renderVec
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.EtherWarpHelper
import me.odinmain.utils.skyblock.EtherWarpHelper.etherPos
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object EtherWarpHelper : Module(
    name = "Ether Warp Helper",
    description = "Shows you where your etherwarp will teleport you.",
    category = Category.RENDER
) {
    private val render: Boolean by BooleanSetting("Show Etherwarp Guess", true)
    private val useServerPosition: Boolean by DualSetting("Positioning", "Server Pos", "Player Pos", description = "If etherwarp guess should use your server position or real position.").withDependency { render }
    private val renderFail: Boolean by BooleanSetting("Show when failed", true)
    private val wrongColor: Color by ColorSetting("Wrong Color", Color.RED.withAlpha(.5f), allowAlpha = true).withDependency { renderFail }
    private val style: Int by SelectorSetting("Style", Renderer.defaultStyle, Renderer.styles, description = Renderer.styleDesc)
    private val color: Color by ColorSetting("Color", Color.ORANGE.withAlpha(.5f), allowAlpha = true)
    private val lineWidth: Float by NumberSetting("Line Width", 2f, 0.1f, 10f, 0.1f, description = "The width of the box's lines.")
    private val depthCheck: Boolean by BooleanSetting("Depth check", false, description = "Boxes show through walls.")
    private val sounds: Boolean by BooleanSetting("Custom Sounds", default = false)
    private val defaultSounds = arrayListOf("mob.blaze.hit", "fire.ignite", "random.orb", "random.break", "mob.guardian.land.hit", "note.pling", "Custom")
    private val sound: Int by SelectorSetting("Sound", "mob.blaze.hit", defaultSounds, description = "Which sound to play when you get a secret.").withDependency { sounds }
    private val customSound: String by StringSetting("Custom Sound", "mob.blaze.hit",
        description = "Name of a custom sound to play. This is used when Custom is selected in the Sound setting. Do not use the bat death sound or your game will freeze!", length = 32
    ).withDependency { sound == defaultSounds.size - 1 && sounds}
    private val volume: Float by NumberSetting("Volume", 1f, 0, 1, .01f, description = "Volume of the sound.").withDependency { sounds }
    private val pitch: Float by NumberSetting("Pitch", 2f, 0, 2, .01f, description = "Pitch of the sound.").withDependency { sounds }
    val reset: () -> Unit by ActionSetting("Play sound") { playEtherwarpSound() }.withDependency { sounds }

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        val player = mc.thePlayer as? IEntityPlayerSPAccessor ?: return
        val positionLook =
            if (useServerPosition)
                PositionLook(Vec3(player.lastReportedPosX, player.lastReportedPosY, player.lastReportedPosZ), player.lastReportedYaw, player.lastReportedPitch)
            else
                PositionLook(mc.thePlayer.renderVec, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)

        etherPos = EtherWarpHelper.getEtherPos(positionLook)
        if (render && mc.thePlayer.isSneaking && mc.thePlayer.heldItem.extraAttributes?.getBoolean("ethermerge") == true && (etherPos.succeeded || renderFail)) {
            val pos = etherPos.pos ?: return
            val color = if (etherPos.succeeded) color else wrongColor

            getBlockAt(pos).setBlockBoundsBasedOnState(mc.theWorld, pos)
            val aabb = getBlockAt(pos).getSelectedBoundingBox(mc.theWorld, pos).expand(0.002, 0.002, 0.002) ?: return

            Renderer.drawStyledBox(aabb, color, style, lineWidth, depthCheck)
        }
    }

    @SubscribeEvent
    fun onSoundPacket(event: PacketReceivedEvent) {
        with(event.packet) {
            if (this !is S29PacketSoundEffect || this.soundName != "mob.enderdragon.hit" || !sounds || customSound == "mob.enderdragon.hit") return
            modMessage("${this.volume}, ${this.pitch}")
            playEtherwarpSound()
        }
    }


    private fun playEtherwarpSound() {
        val sound = if (sound == defaultSounds.size - 1) customSound else defaultSounds[sound]
        PlayerUtils.playLoudSound(sound, volume, pitch)
    }
}