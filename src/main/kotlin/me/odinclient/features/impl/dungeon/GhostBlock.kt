package me.odinclient.features.impl.dungeon

import me.odinclient.OdinClient.Companion.display
import me.odinclient.OdinClient.Companion.mc
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

//TODO: Rename settings
object GhostBlock : Module(
    name = "Ghost Blocks",
    description = "Creates ghost blocks on key press, and in specific locations",
    category = Category.DUNGEON,
) {
    private val gkey: Boolean by BooleanSetting("GKey", true)
    private val gkeySpeed: Long by NumberSetting("Speed", 50L, 0.0, 300.0, 10.0)
        .withDependency { gkey }
    private val ghostBlockSkulls: Boolean by BooleanSetting("Ghost Skulls", true, description = "If enabled skulls will also be turned into ghost blocks.")
        .withDependency { gkey }
    private val gbRange: Double by NumberSetting("Range", 8.0, 4.5, 60.0, 0.5, description = "Maximum range at which ghost blocks will be created.")
        .withDependency { gkey }
    private val onlyDungeon: Boolean by BooleanSetting("Only In Dungeon", false, description = "Will only work inside of a dungeon.")
        .withDependency { gkey }
    private val preGhostBlock: Boolean by BooleanSetting("F7 Ghost blocks")

    override fun onKeybind() {
        if (!gkey) super.onKeybind()
    }

    private val blacklist = arrayOf(
        Blocks.stone_button,
        Blocks.chest,
        Blocks.trapped_chest,
        Blocks.lever
    )

    init {
        execute({ gkeySpeed }) {
            if (!gkey || display != null || (onlyDungeon && !inDungeons)) return@execute
            if (!isKeybindDown()) return@execute

            val lookingAt = mc.thePlayer?.rayTrace(gbRange, 1f)
            toAir(lookingAt?.blockPos)
        }

        execute(500) {
            if (!DungeonUtils.isFloor(7) || !DungeonUtils.inBoss || preGhostBlock) return@execute
            for (i in blocks[getPhase()] ?: return@execute) {
                mc.theWorld?.setBlockToAir(i)
            }
        }
    }

    private fun toAir(blockPos: BlockPos?): Boolean {
        if (blockPos != null) {
            val block = mc.theWorld.getBlockState(blockPos).block
            if (!blacklist.contains(block) && (block !== Blocks.skull || (ghostBlockSkulls && (mc.theWorld.getTileEntity(blockPos) as? TileEntitySkull)?.playerProfile?.id?.toString() != "26bb1a8d-7c66-31c6-82d5-a9c04c94fb02"))
            ) {
                mc.theWorld.setBlockToAir(blockPos)
                return true
            }
        }
        return false
    }

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