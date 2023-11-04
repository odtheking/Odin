package me.odinmain.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.skyblock.ChatCommands.private
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.DualSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.VecUtils
import me.odinmain.utils.VecUtils.get
import me.odinmain.utils.VecUtils.multiply
import me.odinmain.utils.VecUtils.toDoubleArray
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.world.RenderUtils
import me.odinmain.utils.skyblock.ItemUtils.extraAttributes
import me.odinmain.utils.skyblock.ItemUtils.itemID
import me.odinmain.utils.skyblock.WorldUtils
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.lang.reflect.Field
import kotlin.math.*

object EtherWarpHelper : Module(
    name = "Ether Warp Helper",
    description = "Shows you where your etherwarp will teleport you.",
    category = Category.RENDER,
    tag = TagType.NEW
) {
    private data class EtherPos(val succeeded: Boolean, val pos: BlockPos?) {
        companion object {
            val NONE = EtherPos(false, null)
        }
    }
    private var etherPos: EtherPos = EtherPos.NONE

    private val color: Color by ColorSetting("Color", Color.ORANGE.withAlpha(.5f), allowAlpha = true)
    private val filled: Boolean by DualSetting("Type", "Outline", "Filled", default = false)
    private val thickness: Float by NumberSetting("Thickness", 3f, 1f, 10f, .1f).withDependency { !filled }
    private val phase: Boolean by BooleanSetting("Phase", false)

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        etherPos = getEtherPos()
        if (etherPos.succeeded && mc.thePlayer.isSneaking && mc.thePlayer.heldItem.extraAttributes?.getBoolean("etherMerge") == true) {
            val pos = etherPos.pos ?: return

            if (filled)
                RenderUtils.drawFilledBox(pos, color, phase = phase)
            else
                RenderUtils.drawCustomESPBox(pos, color, thickness = thickness, phase = phase)
        }
    }

    private fun getEtherPos(): EtherPos {
        val p = mc.thePlayer ?: return EtherPos.NONE

        val (yaw, pitch) = Pair(serverYaw.get(p) as Float, serverPitch.get(p) as Float)
        val lookVec = VecUtils.getLook(yaw = yaw, pitch = pitch).normalize().multiply(60.0)
        val startPos: Vec3 = VecUtils.getPositionEyes(Vec3(serverPosX.get(p) as Double, serverPosY.get(p) as Double, serverPosZ.get(p) as Double))

        val endPos = lookVec.add(startPos)

        return traverseVoxels(startPos, endPos)
    }

    /**
     * Traverses voxels from start to end and returns the first non-air block it hits.
     * @author Bloom
     */
    private fun traverseVoxels(start: Vec3, end: Vec3): EtherPos {
        val direction = end.subtract(start)
        val step = direction.toDoubleArray().map { sign(it) }
        val thing = direction.toDoubleArray().map { 1 / it }
        val tDelta = thing.mapIndexed { index, value -> min(value * step[index], 1.0) }
        val tMax = thing.mapIndexed { index, value -> abs((floor(start.get(index)) + max(step[index], .0) - start.get(index)) * value) }.toMutableList()

        val currentPos = start.toDoubleArray().map { floor(it) }.toDoubleArray()
        val endPos = end.toDoubleArray().map { floor(it) }.toDoubleArray()
        var iters = 0
        while (iters < 1000) {
            iters++

            val pos = BlockPos(currentPos[0].toInt(), currentPos[1].toInt(), currentPos[2].toInt())
            val currentBlock = WorldUtils.getBlockIdAt(pos)

            if (currentBlock != 0) {
                return EtherPos(isValidEtherWarpBlock(pos), pos)
            }

            if (currentPos.contentEquals(endPos)) break // reached end

            val minIndex = tMax.indexOf(min(tMax[0], min(tMax[1], tMax[2])))
            tMax[minIndex] += tDelta[minIndex]
            currentPos[minIndex] += step[minIndex]
        }

        return EtherPos.NONE
    }

    /**
     * Checks if the block at the given position is a valid block to etherwarp onto.
     * @author Bloom
     */
    private fun isValidEtherWarpBlock(pos: BlockPos): Boolean {
        // Checking the actual block to etherwarp ontop of
        // Can be at foot level, but not etherwarped onto directly.
        if (mc.theWorld.getBlockState(pos).block.registryName in validEtherwarpFeetBlocks) return false

        // The block at foot level
        if (mc.theWorld.getBlockState(pos.up(1)).block.registryName !in validEtherwarpFeetBlocks) return false

        return mc.theWorld.getBlockState(pos.up(2)).block.registryName in validEtherwarpFeetBlocks
    }

    private val validEtherwarpFeetBlocks = setOf(
        "minecraft:air",
        "minecraft:fire",
        "minecraft:carpet",
        "minecraft:skull",
        "minecraft:lever",
        "minecraft:stone_button",
        "minecraft:wooden_button",
        "minecraft:torch",
        "minecraft:string",
        "minecraft:tripwire_hook",
        "minecraft:tripwire",
        "minecraft:rail",
        "minecraft:activator_rail",
        "minecraft:snow_layer",
        "minecraft:carrots",
        "minecraft:wheat",
        "minecraft:potatoes",
        "minecraft:nether_wart",
        "minecraft:pumpkin_stem",
        "minecraft:melon_stem",
        "minecraft:redstone_torch",
        "minecraft:redstone_wire",
        "minecraft:red_flower",
        "minecraft:yellow_flower",
        "minecraft:sapling",
        "minecraft:flower_pot",
        "minecraft:deadbush",
        "minecraft:tallgrass",
        "minecraft:ladder",
        "minecraft:double_plant",
        "minecraft:unpowered_repeater",
        "minecraft:powered_repeater",
        "minecraft:unpowered_comparator",
        "minecraft:powered_comparator",
        "minecraft:web",
        "minecraft:waterlily",
        "minecraft:water",
        "minecraft:lava",
        "minecraft:torch",
        "minecraft:vine",
    )

}