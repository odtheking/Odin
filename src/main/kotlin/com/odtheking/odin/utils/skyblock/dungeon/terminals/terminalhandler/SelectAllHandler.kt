package com.odtheking.odin.utils.skyblock.dungeon.terminals.terminalhandler

import com.odtheking.odin.features.impl.boss.TerminalSolver
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.hasGlint
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class SelectAllHandler(private val color: DyeColor): TerminalHandler(TerminalTypes.SELECT) {

    override fun solve(items: List<ItemStack>): List<Int> {
        val colorName = color.name.replace("_", " ")

        return items.mapIndexedNotNull { index, item ->
            val nameMatches = item.hoverName.string.startsWith(colorName, true)
            val itemMatches = when (color) {
                DyeColor.BLACK  -> item.item == Items.INK_SAC
                DyeColor.BLUE   -> item.item == Items.LAPIS_LAZULI
                DyeColor.BROWN  -> item.item == Items.COCOA_BEANS
                DyeColor.WHITE  -> item.item == Items.BONE_MEAL || item.item == Items.WHITE_WOOL
                DyeColor.GREEN  -> item.item == Items.CACTUS
                DyeColor.RED    -> item.item == Items.POPPY || item.item == Items.ROSE_BUSH
                DyeColor.YELLOW -> item.item == Items.DANDELION
                DyeColor.LIGHT_GRAY -> item.hoverName.string.startsWith("silver", true)
                else -> false
            }

            if (!item.hasGlint() && item.item != Items.BLACK_STAINED_GLASS_PANE && (nameMatches || itemMatches)) index else null
        }
    }

    override fun renderSlot(slotIndex: Int): Pair<Color, String?> = TerminalSolver.selectColor to null
}