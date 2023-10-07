package me.odinclient.features.impl.dungeon

import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.Setting.Companion.withDependency
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import me.odinclient.utils.skyblock.dungeon.DungeonUtils.getPhase
import me.odinclient.utils.skyblock.dungeon.DungeonUtils.inDungeons
import net.minecraft.init.Blocks
import net.minecraft.tileentity.TileEntitySkull
import net.minecraft.util.BlockPos

object GhostBlock : Module(
    name = "Ghost Blocks",
    description = "Creates ghost blocks on key press, and in specific locations.",
    category = Category.DUNGEON,
) {
    private val ghostBlockKey: Boolean by BooleanSetting("GKey", true)
    private val ghostBlockSpeed: Long by NumberSetting("Speed", 50L, 0.0, 300.0, 10.0)
        .withDependency { ghostBlockKey }
    private val ghostBlockSkulls: Boolean by BooleanSetting("Ghost Skulls", true, description = "If enabled skulls will also be turned into ghost blocks.")
        .withDependency { ghostBlockKey }
    private val ghostBlockRange: Double by NumberSetting("Range", 8.0, 4.5, 60.0, 0.5, description = "Maximum range at which ghost blocks will be created.")
        .withDependency { ghostBlockKey }
    private val onlyDungeon: Boolean by BooleanSetting("Only In Dungeon", false, description = "Will only work inside of a dungeon.")
        .withDependency { ghostBlockKey }
    private val preGhostBlock: Boolean by BooleanSetting("F7 Ghost blocks")

    override fun onKeybind() {
        if (!ghostBlockKey) super.onKeybind()
    }

    private val blacklist = arrayOf(
        Blocks.stone_button,
        Blocks.chest,
        Blocks.trapped_chest,
        Blocks.lever
    )

    init {
        execute({ ghostBlockSpeed }) {
            if (!ghostBlockKey || !enabled || mc.currentScreen != null || (onlyDungeon && !inDungeons)) return@execute
            if (!isKeybindDown()) return@execute

            val lookingAt = mc.thePlayer?.rayTrace(ghostBlockRange, 1f)
            toAir(lookingAt?.blockPos)
        }

        execute(500) {
            if (!DungeonUtils.isFloor(7) || !DungeonUtils.inBoss || !preGhostBlock || !enabled) return@execute
            for (i in blocks[getPhase()] ?: return@execute) {
                mc.theWorld?.setBlockToAir(i)
            }
            for (i in enderchests) {
                mc.theWorld?.setBlockState(i, Blocks.ender_chest.defaultState)
            }
            for (i in glass) {
                mc.theWorld?.setBlockState(i, Blocks.glass.defaultState)
            }
        }
    }

    private fun toAir(blockPos: BlockPos?): Boolean {
        if (blockPos != null) {
            val block = mc.theWorld.getBlockState(blockPos).block
            if (block !in blacklist && (block !== Blocks.skull || (ghostBlockSkulls && (mc.theWorld.getTileEntity(blockPos) as? TileEntitySkull)?.playerProfile?.id?.toString() != "26bb1a8d-7c66-31c6-82d5-a9c04c94fb02"))
            ) {
                mc.theWorld.setBlockToAir(blockPos)
                return true
            }
        }
        return false
    }

    private val enderchests = arrayOf(
        BlockPos(77, 221, 35),
        BlockPos(77, 221, 34),
        BlockPos(77, 221, 33)
    )

    private val glass = arrayOf(
        BlockPos(77, 221, 36),
        BlockPos(78, 221, 36),
    )

    private val blocks = mapOf(
        1 to arrayOf(
            BlockPos(88, 220, 61),
            BlockPos(88, 219, 61),
            BlockPos(88, 218, 61),
            BlockPos(88, 217, 61),
            BlockPos(88, 216, 61),
            BlockPos(88, 215, 61),
            BlockPos(88, 214, 61),
            BlockPos(88, 213, 61),
            BlockPos(88, 212, 61),
            BlockPos(88, 211, 61),
            BlockPos(88, 210, 61),
            BlockPos(77, 220, 35),
            BlockPos(78, 220, 35)
        ),

        2 to arrayOf(
            BlockPos(88, 167, 41),
            BlockPos(89, 167, 41),
            BlockPos(90, 167, 41),
            BlockPos(91, 167, 41),
            BlockPos(92, 167, 41),
            BlockPos(93, 167, 41),
            BlockPos(94, 167, 41),
            BlockPos(95, 167, 41),
            BlockPos(88, 166, 41),
            BlockPos(89, 166, 41),
            BlockPos(90, 166, 41),
            BlockPos(91, 166, 41),
            BlockPos(92, 166, 41),
            BlockPos(93, 166, 41),
            BlockPos(94, 166, 41),
            BlockPos(95, 166, 41),
            BlockPos(88, 165, 41),
            BlockPos(89, 165, 41),
            BlockPos(90, 165, 41),
            BlockPos(91, 165, 41),
            BlockPos(92, 165, 41),
            BlockPos(93, 165, 41),
            BlockPos(94, 165, 41),
            BlockPos(95, 165, 41),
            BlockPos(88, 167, 40),
            BlockPos(89, 167, 40),
            BlockPos(90, 167, 40),
            BlockPos(91, 167, 40),
            BlockPos(92, 167, 40),
            BlockPos(93, 167, 40),
            BlockPos(94, 167, 40),
            BlockPos(95, 167, 40),
            BlockPos(88, 166, 40),
            BlockPos(89, 166, 40),
            BlockPos(90, 166, 40),
            BlockPos(91, 166, 40),
            BlockPos(92, 166, 40),
            BlockPos(93, 166, 40),
            BlockPos(94, 166, 40),
            BlockPos(95, 166, 40),
            BlockPos(88, 165, 40),
            BlockPos(89, 165, 40),
            BlockPos(90, 165, 40),
            BlockPos(91, 165, 40),
            BlockPos(92, 165, 40),
            BlockPos(93, 165, 40),
            BlockPos(94, 165, 40),
            BlockPos(95, 165, 40),
        ),

        3 to arrayOf(
            BlockPos(51, 114, 52),
            BlockPos(51, 114, 53),
            BlockPos(51, 114, 54),
            BlockPos(51, 114, 55),
            BlockPos(51, 114, 56),
            BlockPos(51, 114, 57),
            BlockPos(51, 114, 58),
            BlockPos(51, 115, 52),
            BlockPos(51, 115, 53),
            BlockPos(51, 115, 54),
            BlockPos(51, 115, 55),
            BlockPos(51, 115, 56),
            BlockPos(51, 115, 57),
            BlockPos(51, 115, 58),
        ),

        4 to arrayOf(
            BlockPos(54, 64, 72),
            BlockPos(54, 64, 73),
            BlockPos(54, 63, 73),
            BlockPos(54, 64, 74),
            BlockPos(54, 63, 74)
        )
    )
}