package com.odtheking.odin.features.impl.render

import com.mojang.blaze3d.platform.InputConstants
import com.odtheking.odin.clickgui.settings.RenderableSetting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.clickgui.settings.impl.DropdownSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.events.InputEvent
import com.odtheking.odin.events.RenderExtractEvent
import com.odtheking.odin.events.core.EventPriority
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.*
import com.odtheking.odin.utils.render.BoxStyle
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.skyblock.Island
import com.odtheking.odin.utils.skyblock.LocationUtils
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.Pose
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.EmptyBlockGetter
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.piston.PistonHeadBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import kotlin.math.*

object Etherwarp : Module(
    name = "Etherwarp",
    description = "Provides configurable visual feedback for Etherwarp."
) {
    private val render by BooleanSetting("Show Guess", true, desc = "Shows where Etherwarp will take you.")
    private val color by ColorSetting("Success Color", Colors.MINECRAFT_GREEN, true, desc = "Color of the box.").withDependency { render }
    private val renderFail by BooleanSetting("Show when failed", true, desc = "Shows the box even when the guess failed.").withDependency { render }
    private val failColor by ColorSetting("Fail Color", Colors.MINECRAFT_RED, true, desc = "Color of the box if guess failed.").withDependency { renderFail }
    private val renderStyle by SelectorSetting("Box Style", BoxStyle.FILLED_OUTLINE, desc = "Style of the box.").withDependency { render }
    private val useServerPosition by BooleanSetting("Use Server Position", false, desc = "Uses the server position for etherwarp instead of the client position.").withDependency { render }
    private val fullBlock by BooleanSetting("Full Block", false, desc = "Renders the the 1x1x1 block instead of it's actual size.").withDependency { render }
    private val depth by BooleanSetting("Depth", false, desc = "Renders the box through walls.").withDependency { render }

    private val dropdown by DropdownSetting("Sounds", desc = "Shows settings for playing a custom sound when you etherwarp.")
    private val sounds by BooleanSetting("Custom Sounds", false, desc = "Plays the selected custom sound when you etherwarp.").withDependency { dropdown }
    private val soundSettings = createSoundSettings("Etherwarp Sound", "entity.experience_orb.pickup") { sounds && dropdown }
    private var etherPos: EtherPos? = null

    private var cachedMainHandItem: ItemStack? = null
    private var cachedEtherDistance: Double? = null
    private var cachedIsConduit = false

    init {
        onReceive<ClientboundSoundPacket> {
            if (!sounds || sound.value() != SoundEvents.ENDER_DRAGON_HURT || volume != 1f || pitch != 0.53968257f) return@onReceive
            playSoundSettings(soundSettings())
            it.cancel()
        }

        on<RenderExtractEvent> (EventPriority.LOW) {
            if (mc.gui.screen() != null || !render) return@on
            val player = mc.player ?: return@on

            val mainHandItem = player.mainHandItem
            if (cachedMainHandItem !== mainHandItem) {
                cachedMainHandItem = mainHandItem
                val data = mainHandItem.isEtherwarpItem()
                cachedEtherDistance = data?.let(::getEtherwarpDistance)
                cachedIsConduit = data?.itemId == "ETHERWARP_CONDUIT"
            }

            val distance = cachedEtherDistance ?: return@on
            if (!player.isShiftKeyDown && !cachedIsConduit) return@on

            val etherPos = (
                if (useServerPosition) getEtherPos(Vec3(player.xLast, player.yLast, player.zLast), distance, etherWarp = true, sneaking = player.lastSentInput.shift())
                else getEtherPos(player.position(), distance, etherWarp = true)
            ).also { etherPos = it }
            if (!etherPos.succeeded && !renderFail) return@on
            val pos = etherPos.pos ?: return@on

            val box = if (fullBlock) AABB(pos) else pos.getBlockBounds()?.move(pos) ?: AABB(pos)
            drawStyledBox(box, if (etherPos.succeeded) color else failColor, renderStyle, depth)
        }

        on<InputEvent> {
            if (key.type != InputConstants.Type.MOUSE || key.value != InputConstants.MOUSE_BUTTON_RIGHT || !LocationUtils.isCurrentArea(Island.SinglePlayer)) return@on
            if (cachedEtherDistance == null || (mc.player?.isShiftKeyDown == false && !cachedIsConduit)) return@on

            etherPos?.pos?.let {
                if (etherPos?.succeeded == false) return@on
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
        val vec3: Vec3 get() = Vec3(pos ?: BlockPos.ZERO)

        companion object {
            val NONE = EtherPos(false, null, null)
        }
    }

    fun getEtherwarpDistance(data: CompoundTag): Double =
        57.0 + data.getInt("tuned_transmission").orElse(0)

    fun getEtherPos(
        position: Vec3?,
        distance: Double,
        returnEnd: Boolean = false,
        etherWarp: Boolean = false,
        sneaking: Boolean = mc.player?.isCrouching == true
    ): EtherPos {
        val player = mc.player ?: return EtherPos.NONE
        if (position == null) return EtherPos.NONE
        val eyeHeight = when {
            player.pose == Pose.SWIMMING -> 0.4
            sneaking -> 1.27
            else -> 1.62
        }

        val startPos = position.addVec(y = eyeHeight)
        val endPos = startPos.add(player.lookAngle.scale(distance))
        val result = traverseVoxels(startPos, endPos, etherWarp)
        return if (result === EtherPos.NONE && returnEnd) EtherPos(true, BlockPos.containing(endPos), null) else result
    }

    /**
     * Traverses voxels from start to end and returns the first non-air block it hits.
     * @author Bloom
     */
    private fun traverseVoxels(start: Vec3, end: Vec3, etherWarp: Boolean): EtherPos {
        val level = mc.level ?: return EtherPos.NONE
        val (x0, y0, z0) = start
        val (dirX, dirY, dirZ) = end.subtract(start)

        var x = floor(x0).toInt()
        var y = floor(y0).toInt()
        var z = floor(z0).toInt()

        val stepX = sign(dirX).toInt()
        val stepY = sign(dirY).toInt()
        val stepZ = sign(dirZ).toInt()

        val tDeltaX = abs(1.0 / dirX)
        val tDeltaY = abs(1.0 / dirY)
        val tDeltaZ = abs(1.0 / dirZ)

        var tMaxX = (if (stepX < 0) x0 - x else x + 1 - x0) * tDeltaX
        var tMaxY = (if (stepY < 0) y0 - y else y + 1 - y0) * tDeltaY
        var tMaxZ = (if (stepZ < 0) z0 - z else z + 1 - z0) * tDeltaZ

        var remaining = abs(floor(end.x).toInt() - x) + abs(floor(end.y).toInt() - y) + abs(floor(end.z).toInt() - z)

        val pos = BlockPos.MutableBlockPos()
        while (true) {
            val chunk = level.chunkSource.getChunkNow(x shr 4, z shr 4) ?: return EtherPos.NONE
            val state = chunk.getBlockState(pos.set(x, y, z))
            val flags = flagsOf(state)

            if (if (etherWarp) flags and PASSABLE == 0 else flags and AIR == 0) {
                if (flags and PASSABLE != 0) return EtherPos(false, pos.immutable(), state)

                val feetY = y + (flags ushr STAND_OFFSET_SHIFT)
                return EtherPos(!chunk.obstructs(BlockPos(x, feetY, z)) && !chunk.obstructs(BlockPos(x, feetY + 1, z)), pos.immutable(), state)
            }

            if (remaining-- == 0) return if (flags and AIR != 0) EtherPos.NONE else EtherPos(false, pos.immutable(), state)

            if (tMaxX <= tMaxY && tMaxX <= tMaxZ) {
                tMaxX += tDeltaX
                x += stepX
            } else if (tMaxY <= tMaxZ) {
                tMaxY += tDeltaY
                y += stepY
            } else {
                tMaxZ += tDeltaZ
                z += stepZ
            }
        }
    }

    private fun LevelChunk.obstructs(pos: BlockPos): Boolean = flagsOf(getBlockState(pos)) and OBSTRUCTS != 0
    private fun flagsOf(state: BlockState): Int = blockFlags[Block.getId(state)]

    private const val STAND_OFFSET_SHIFT = 3
    private const val OBSTRUCTS = 4
    private const val PASSABLE = 2
    private const val AIR = 1

    private fun computeFlags(state: BlockState): Int {
        val block = state.block
        val collision = state.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO)

        val noHitbox = block is SignBlock || block is AbstractBannerBlock || block is TripWireHookBlock || block is BasePressurePlateBlock
        val passable = !noHitbox && when (block) {
            is ButtonBlock, is SkullBlock, is WallSkullBlock, is LadderBlock,
            is BubbleColumnBlock, is FlowerPotBlock, is PistonHeadBlock, is LeverBlock,
            is NetherWartBlock, is ComparatorBlock, is RedstoneTorchBlock, is RepeaterBlock -> true
            else -> collision.isEmpty
        }
        val blocksFeet = when (block) {
            is SkullBlock, is WallSkullBlock, is ComparatorBlock, is RepeaterBlock, is FlowerPotBlock, is LadderBlock -> true
            else -> false
        }
        val standOffset = max(1, ceil(collision.max(Direction.Axis.Y)).toInt())

        return (standOffset shl STAND_OFFSET_SHIFT) or
            (if (state.isAir) AIR else 0) or
            (if (passable) PASSABLE else 0) or
            (if ((!passable && !noHitbox) || blocksFeet) OBSTRUCTS else 0)
    }

    private val blockFlags = IntArray(Block.BLOCK_STATE_REGISTRY.size()) { id ->
        Block.BLOCK_STATE_REGISTRY.byId(id)?.let(::computeFlags) ?: 0
    }
}