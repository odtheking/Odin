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
        return items.mapIndexedNotNull { index, item ->
            if (!item.hasGlint() &&
                item.item != Items.BLACK_STAINED_GLASS_PANE &&
                (item.hoverName.string.startsWith(color.name.replace("_", " "), true) ||
                when (color) {
                    DyeColor.BLACK -> item.item == Items.INK_SAC
                    DyeColor.BLUE -> item.item == Items.LAPIS_LAZULI
                    DyeColor.BROWN -> item.item == Items.COCOA_BEANS
                    DyeColor.WHITE -> item.item == Items.BONE_MEAL || item.item == Items.WHITE_WOOL
                    DyeColor.GREEN -> item.item == Items.CACTUS
                    DyeColor.RED -> item.item == Items.POPPY
                    DyeColor.YELLOW -> item.item == Items.DANDELION
                    else -> false
                })) index else null
        }
    }

    override fun renderSlot(slotIndex: Int): Pair<Color, String?> = TerminalSolver.selectColor to null
}