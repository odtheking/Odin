package me.odin.features.impl.render

import me.odin.mixin.accessors.IEntityPlayerSPAccessor
import me.odinmain.clickgui.settings.Setting.Companion.withDependency
import me.odinmain.clickgui.settings.impl.*
import me.odinmain.events.impl.PacketEvent
import me.odinmain.features.Module
import me.odinmain.utils.PositionLook
import me.odinmain.utils.positionVector
import me.odinmain.utils.render.Color.Companion.withAlpha
import me.odinmain.utils.render.Colors
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.EtherWarpHelper
import me.odinmain.utils.skyblock.EtherWarpHelper.etherPos
import me.odinmain.utils.skyblock.PlayerUtils.playLoudSound
import me.odinmain.utils.skyblock.usingEtherWarp
import me.odinmain.utils.toAABB
import net.minecraft.block.Block.getIdFromBlock
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.util.MovingObjectPosition.MovingObjectType
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*

object EtherWarpHelper : Module(
    name = "Etherwarp Helper",
    description = "Provides configurable visual and audio feedback for etherwarp."
) {
    private val render by BooleanSetting("Show Etherwarp Guess", true, desc = "Shows where etherwarp will take you.")
    private val color by ColorSetting("Color", Colors.MINECRAFT_GOLD.withAlpha(.5f), allowAlpha = true, desc = "Color of the box.").withDependency { render }
    private val renderFail by BooleanSetting("Show when failed", true, desc = "Shows the box even when the guess failed.").withDependency { render }
    private val wrongColor by ColorSetting("Wrong Color", Colors.MINECRAFT_RED.withAlpha(.5f), allowAlpha = true, desc = "Color of the box if guess failed.").withDependency { renderFail }

    private val style by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, desc = Renderer.STYLE_DESCRIPTION).withDependency { render }
    private val lineWidth by NumberSetting("Line Width", 2f, 0.1f, 10f, 0.1f, desc = "The width of the box's lines.").withDependency { render }
    private val depthCheck by BooleanSetting("Depth check", false, desc = "Boxes show through walls.").withDependency { render }
    private val fullBlock by BooleanSetting("Full Block", false, desc = "If the box should be a full block.").withDependency { render }
    private val expand by NumberSetting("Expand", 0.0, -1, 1, 0.01, desc = "Expands the box by this amount.").withDependency { render }
    private val useServerPosition by BooleanSetting("Use Server Position", true, desc = "If etherwarp guess should use your server position or real position.").withDependency { render }
    private val interactBlocks by BooleanSetting("Fail on Interactable", true, desc = "If the guess should fail if you are looking at an interactable block.").withDependency { render }

    private val dropdown by DropdownSetting("Sounds", false)
    private val sounds by BooleanSetting("Custom Sounds", false, desc = "Plays the selected custom sound when you etherwarp.").withDependency { dropdown }
    private val defaultSounds = arrayListOf("mob.blaze.hit", "fire.ignite", "random.orb", "random.break", "mob.guardian.land.hit", "note.pling", "Custom")
    private val sound by SelectorSetting("Sound", "mob.blaze.hit", defaultSounds, desc = "Which sound to play when you etherwarp.").withDependency { sounds && dropdown }
    private val customSound by StringSetting("Custom Sound", "mob.blaze.hit",
        desc = "Name of a custom sound to play. This is used when Custom is selected in the Sound setting.", length = 32
    ).withDependency { sound == defaultSounds.size - 1 && sounds && dropdown }
    private val soundVolume by NumberSetting("Volume", 1f, 0, 1, .01f, desc = "Volume of the sound.").withDependency { sounds && dropdown }
    private val soundPitch by NumberSetting("Pitch", 2f, 0, 2, .01f, desc = "Pitch of the sound.").withDependency { sounds && dropdown }
    private val reset by ActionSetting("Play sound", desc = "Plays the selected sound.") { playLoudSound(if (sound == defaultSounds.size - 1) customSound else defaultSounds[sound], soundVolume, soundPitch) }.withDependency { sounds && dropdown }

    @SubscribeEvent(priority = EventPriority.LOW)
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        if (mc.thePlayer?.usingEtherWarp == false || !render) return
        val player = mc.thePlayer as? IEntityPlayerSPAccessor ?: return
        val positionLook =
            if (useServerPosition)
                PositionLook(Vec3(player.lastReportedPosX, player.lastReportedPosY, player.lastReportedPosZ), player.lastReportedYaw, player.lastReportedPitch)
            else
                PositionLook(mc.thePlayer.positionVector, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)

        etherPos = EtherWarpHelper.getEtherPos(positionLook)
        val succeeded =
            etherPos.succeeded && (!interactBlocks || mc.objectMouseOver?.typeOfHit != MovingObjectType.BLOCK || etherPos.state?.block?.let { invalidBlocks.get(getIdFromBlock(it)) } != true)

        if (succeeded || renderFail)
            if (!fullBlock)
                Renderer.drawStyledBlock(etherPos.pos ?: return, if (succeeded) color else wrongColor, style, lineWidth, depthCheck, true, expand)
            else
                Renderer.drawStyledBox(etherPos.pos?.toAABB()?.expand(expand, expand, expand) ?: return, if (succeeded) color else wrongColor, style, lineWidth, depthCheck)
    }

    @SubscribeEvent
    fun onSoundPacket(event: PacketEvent.Receive) = with(event.packet) {
        if (this !is S29PacketSoundEffect || soundName != "mob.enderdragon.hit" || !sounds || volume != 1f || pitch != 0.53968257f || customSound == "mob.enderdragon.hit") return
        playLoudSound(if (sound == defaultSounds.size - 1) customSound else defaultSounds[sound], soundVolume, soundPitch, positionVector)
        event.isCanceled = true
    }

    private val invalidBlocks = BitSet().apply {
        setOf(
            Blocks.hopper, Blocks.chest, Blocks.ender_chest, Blocks.furnace, Blocks.crafting_table, Blocks.cauldron,
            Blocks.enchanting_table, Blocks.dispenser, Blocks.dropper, Blocks.brewing_stand, Blocks.trapdoor,
        ).forEach { set(getIdFromBlock(it)) }
    }
}