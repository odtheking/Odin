package com.odtheking.odin.utils.skyblock.dungeon.terminals.terminalhandler

import com.odtheking.odin.features.impl.boss.TerminalSolver
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.hasGlint
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class SelectAllHandler(private val color: DyeColor): TerminalHandler(TerminalTypes.SELECT) {

    private fun fixName(name: String): String {
        val replacements = mapOf(
            "light gray" to "silver",
            "wool" to "white",
            "bone" to "white",
            "ink" to "black",
            "lapis" to "blue",
            "cocoa" to "brown",
            "dandelion" to "yellow",
            "rose" to "red",
            "poppy" to "red",
            "cactus" to "green"
        )

        var fixed = name.lowercase()

        replacements.forEach { (k, v) ->
            if (fixed.startsWith(k)) {
                fixed = fixed.replaceFirst(k, v)
            }
        }

        return fixed
    }

    override fun solve(items: List<ItemStack>): List<Int> {

        val colorName = when (color) {
            DyeColor.LIGHT_GRAY -> "silver"
            else -> color.name.replace("_", " ").lowercase()
        }

        return items.mapIndexedNotNull { index, item ->

            val fixedName = fixName(item.hoverName.string)

            if (
                !item.hasGlint() &&
                item.item != Items.BLACK_STAINED_GLASS_PANE &&
                fixedName.startsWith(colorName, true)
            ) index else null
        }
    }

    override fun renderSlot(slotIndex: Int): Pair<Color, String?> = TerminalSolver.selectColor to null
}
