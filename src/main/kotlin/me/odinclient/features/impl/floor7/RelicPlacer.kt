package me.odinclient.features.impl.floor7

import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.skyblock.ItemUtils.unformattedName
import me.odinclient.utils.skyblock.PlayerUtils
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.util.BlockPos

object RelicPlacer : Module(
    name = "Relic Placer WIP",
    description = "Places the relic if you hover over the correct cauldron",
    category = Category.FLOOR7,
    tag = TagType.NEW
) {
    private val relicMap: Map<String, BlockPos> = mapOf(
        "Green" to BlockPos(45, 169, 44),
        "Blue" to BlockPos(46, 169, 44),
        "Red" to BlockPos(47, 169, 44),
        "Orange" to BlockPos(48, 169, 44),
        "Purple" to BlockPos(49, 169, 44)
    )

    init {
        execute(50) {
            if (DungeonUtils.getPhase() != 5) return@execute
            val pos = mc.objectMouseOver?.blockPos ?: return@execute
            val relic = relicMap.keys.find { mc.thePlayer.inventory.mainInventory[8].unformattedName.contains(it) } ?: return@execute
            if (pos == relicMap[relic]) {
                PlayerUtils.rightClick()
            }
        }
    }
}