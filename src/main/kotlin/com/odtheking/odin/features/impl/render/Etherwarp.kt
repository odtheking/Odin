package com.odtheking.odin.features.impl.render

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.clickgui.settings.impl.DropdownSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.core.EventPriority
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.events.core.onSend
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.*
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.skyblock.Island
import com.odtheking.odin.utils.skyblock.LocationUtils
import net.minecraft.core.BlockPos
import net.minecraft.core.SectionPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
import net.minecraft.network.protocol.game.ServerboundUseItemPacket
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.piston.PistonHeadBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sign

object Etherwarp : Module(
    name = "Etherwarp",
    description = "Provides configurable visual feedback for Etherwarp."
) {
    private val render by BooleanSetting("Show Guess", true, desc = "Shows where Etherwarp will take you.")
    private val color by ColorSetting("Color", Colors.MINECRAFT_GOLD.withAlpha(.85f), true, desc = "Color of the box.").withDependency { render }
    private val renderFail by BooleanSetting("Show when failed", true, desc = "Shows the box even when the guess failed.").withDependency { render }
    private val failColor by ColorSetting("Fail Color", Colors.MINECRAFT_RED.withAlpha(.85f), true, desc = "Color of the box if guess failed.").withDependency { renderFail }
    private val renderStyle by SelectorSetting("Render Style", "Outline", listOf("Filled", "Outline", "Filled Outline"), desc = "Style of the box.").withDependency { render }
    private val useServerPosition by BooleanSetting("Use Server Position", false, desc = "Uses the server position for etherwarp instead of the client position.").withDependency { render }
    private val fullBlock by BooleanSetting("Full Block", false, desc = "Renders the the 1x1x1 block instead of it's actual size.").withDependency { render }
    private val depth by BooleanSetting("Depth", false, desc = "Renders the box through walls.").withDependency { render }

    private val dropdown by DropdownSetting("Sounds", false)
    private val sounds by BooleanSetting("Custom Sounds", false, desc = "Plays the selected custom sound when you etherwarp.").withDependency { dropdown }
    private val soundSettings = createSoundSettings("Etherwarp Sound", "entity.experience_orb.pickup") { sounds && dropdown }
    private var etherPos: EtherPos? = null

    private var cachedMainHandItem: ItemStack? = null
    private var cachedEtherData: CompoundTag? = null

    init {
        onReceive<ClientboundSoundPacket> {
            if (!sounds || sound.value() != SoundEvents.ENDER_DRAGON_HURT || volume != 1f || pitch != 0.53968257f) return@onReceive
            playSoundSettings(soundSettings())
            it.cancel()
        }

        on<RenderEvent.Extract> (EventPriority.LOW) {
            if (mc.screen != null || !render) return@on

            val mainHandItem = mc.player?.mainHandItem ?: return@on

            if (cachedMainHandItem !== mainHandItem) {
                cachedMainHandItem = mainHandItem
                cachedEtherData = mainHandItem.isEtherwarpItem()
            }

            if (cachedEtherData == null || (mc.player?.isShiftKeyDown == false && cachedEtherData?.itemId != "ETHERWARP_CONDUIT")) return@on

            etherPos = getEtherPos(
                if (useServerPosition) mc.player?.oldPosition() else mc.player?.position(),
                57.0 + (cachedEtherData?.getInt("tuned_transmission")?.orElse(0) ?: 0),
                etherWarp = true
            )
            if (etherPos?.succeeded != true && !renderFail) return@on
            val color = if (etherPos?.succeeded == true) color else failColor
            etherPos?.pos?.let { pos ->
                val box = if (fullBlock) AABB(pos) else pos.getBlockBounds()?.move(pos) ?: AABB(pos)

                drawStyledBox(box, color, renderStyle, depth)
            }
        }

        onSend<ServerboundUseItemPacket> {
            if (!LocationUtils.isCurrentArea(Island.SinglePlayer)) return@onSend
            if (cachedEtherData == null || (mc.player?.isShiftKeyDown == false && cachedEtherData?.itemId != "ETHERWARP_CONDUIT")) return@onSend

            etherPos?.pos?.let {
                if (etherPos?.succeeded == false) return@onSend
                mc.player?.connection?.send(
                    ServerboundMovePlayerPacket.PosRot(
                        it.x + 0.5, it.y + 1.05, it.z + 0.5, mc.player?.yRot ?: 0f,
                        mc.player?.xRot ?: 0f, false, false
                    )
                )
                mc.player?.setPos(it.x + 0.5, it.y + 1.05, it.z + 0.5)
                mc.player?.setDeltaMovement(0.0, 0.0, 0.0)
                if (sounds) playSoundSettings(soundSettings())
                else playSoundAtPlayer(SoundEvents.ENDER_DRAGON_HURT, pitch = 0.53968257f)
            }
        }
    }

    data class EtherPos(val succeeded: Boolean, val pos: BlockPos?, val state: BlockState?) {
        val vec3: Vec3 by lazy { Vec3(pos ?: BlockPos.ZERO)  }

        companion object {
            val NONE = EtherPos(false, null, null)
        }
    }

    fun getEtherPos(
        position: Vec3?,
        distance: Double,
        returnEnd: Boolean = false,
        etherWarp: Boolean = false
    ): EtherPos {
        val player = mc.player ?: return EtherPos.NONE
        if (position == null) return EtherPos.NONE
        val eyeHeight = if (player.isCrouching) {
            if (LocationUtils.isCurrentArea(Island.Galatea, Island.ThePark, Island.Hub, Island.SpiderDen)) 1.27 else 1.54 // Use modern sneak height in Galatea
        } else 1.62

        val startPos = position.addVec(y = eyeHeight)
        val endPos = player.lookAngle.multiply(distance, distance, distance).add(startPos)
        return traverseVoxels(startPos, endPos, etherWarp).takeUnless { it == EtherPos.NONE && returnEnd } ?: EtherPos(true, BlockPos.containing(endPos), null)
    }

    /**
     * Traverses voxels from start to end and returns the first non-air block it hits.
     * @author Bloom
     */
    private fun traverseVoxels(start: Vec3, end: Vec3, etherWarp: Boolean): EtherPos {
        val (x0, y0, z0) = start
        val (x1, y1, z1) = end

        var (x, y, z) = start.floorVec()
        val (endX, endY, endZ) = end.floorVec()

        val dirX = x1 - x0
        val dirY = y1 - y0
        val dirZ = z1 - z0

        val stepX = sign(dirX).toInt()
        val stepY = sign(dirY).toInt()
        val stepZ = sign(dirZ).toInt()

        val invDirX = if (dirX != 0.0) 1.0 / dirX else Double.MAX_VALUE
        val invDirY = if (dirY != 0.0) 1.0 / dirY else Double.MAX_VALUE
        val invDirZ = if (dirZ != 0.0) 1.0 / dirZ else Double.MAX_VALUE

        val tDeltaX = abs(invDirX * stepX)
        val tDeltaY = abs(invDirY * stepY)
        val tDeltaZ = abs(invDirZ * stepZ)

        var tMaxX = abs((x + max(stepX, 0) - x0) * invDirX)
        var tMaxY = abs((y + max(stepY, 0) - y0) * invDirY)
        var tMaxZ = abs((z + max(stepZ, 0) - z0) * invDirZ)

        repeat(1000) {
            val blockPos = BlockPos(x.toInt(), y.toInt(), z.toInt())
            val chunk = mc.level?.getChunk(SectionPos.blockToSectionCoord(blockPos.x), SectionPos.blockToSectionCoord(blockPos.z)) ?: return EtherPos.NONE

            val state = chunk.getBlockState(blockPos)
            val id = Block.getId(state)

            val isPassable = (blockFlags[id] and PASSABLE) != 0
            val isSolid = !isPassable

            if ((etherWarp && isSolid) || (!etherWarp && id != 0)) {

                if (!etherWarp && isPassable) return EtherPos(false, blockPos, state)
                val feetState = chunk.getBlockState(blockPos.above())
                val feetFlags = blockFlags[Block.getId(feetState)]

                if ((feetFlags and PASSABLE) == 0 || (feetFlags and BLOCKS_FEET) != 0)
                    return EtherPos(false, blockPos, state)

                val headState = chunk.getBlockState(blockPos.above(2))
                val headFlags = blockFlags[Block.getId(headState)]

                if ((headFlags and PASSABLE) == 0 || (headFlags and BLOCKS_FEET) != 0)
                    return EtherPos(false, blockPos, state)

                return EtherPos(true, blockPos, state)
            }

            if (x == endX && y == endY && z == endZ) return EtherPos.NONE

            when {
                tMaxX <= tMaxY && tMaxX <= tMaxZ -> {
                    tMaxX += tDeltaX
                    x += stepX
                }
                tMaxY <= tMaxZ -> {
                    tMaxY += tDeltaY
                    y += stepY
                }
                else -> {
                    tMaxZ += tDeltaZ
                    z += stepZ
                }
            }
        }

        return EtherPos.NONE
    }

    private const val PASSABLE = 1        // ray passes through
    private const val BLOCKS_FEET = 2     // cannot stand inside

    private val blockFlags: IntArray = IntArray(Block.BLOCK_STATE_REGISTRY.size()).apply {
        Block.BLOCK_STATE_REGISTRY.forEach { state ->
            val block = state.block
            val id = Block.getId(state)

            val passable = when (block) {
                is AirBlock -> true

                is FlowerBlock, is TallGrassBlock, is BushBlock, is TallFlowerBlock, is ShortDryGrassBlock -> true
                is TorchBlock, is RedstoneTorchBlock -> true
                is TripWireBlock, is TripWireHookBlock -> true
                is RailBlock -> true
                is FireBlock -> true
                is VineBlock -> true
                is LiquidBlock -> true
                is SaplingBlock -> true
                is CropBlock, is StemBlock -> true
                is SeagrassBlock, is TallSeagrassBlock -> true
                is SugarCaneBlock -> true
                is MushroomBlock -> true
                is NetherWartBlock -> true
                is RedStoneWireBlock, is ComparatorBlock, is RepeaterBlock -> true
                is SmallDripleafBlock, is BigDripleafStemBlock -> true
                is DoublePlantBlock -> true
                is LeverBlock -> true
                is SnowLayerBlock -> true
                is BubbleColumnBlock -> true
                is GrowingPlantBlock -> true
                is PistonHeadBlock -> true
                is DryVegetationBlock -> true
                is ButtonBlock -> true
                is LanternBlock -> true
                is SkullBlock, is WallSkullBlock -> true
                is LadderBlock -> true
                is FlowerPotBlock -> true
                is WebBlock -> true
                is NetherPortalBlock -> true

                else -> false
            }

            val blocksFeet = when (block) {
                is SkullBlock, is WallSkullBlock -> true
                is FlowerPotBlock -> true
                is LadderBlock -> true
                is VineBlock -> true
                else -> false
            }

            var flags = 0
            if (passable) flags = flags or PASSABLE
            if (blocksFeet) flags = flags or BLOCKS_FEET

            this[id] = flags
        }
    }
}