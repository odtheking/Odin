package com.odtheking.odin.utils.skyblock.dungeon.terminals.terminalhandler

import com.odtheking.odin.features.impl.boss.TerminalSolver
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.hasGlint
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class SelectAllHandler(color: DyeColor) : TerminalHandler(TerminalTypes.SELECT) {

    private val validPrefixes = when (color) {
        DyeColor.BLACK      -> setOf("black", "ink")
        DyeColor.BLUE       -> setOf("blue", "lapis")
        DyeColor.BROWN      -> setOf("brown", "cocoa")
        DyeColor.WHITE      -> setOf("white", "bone", "wool")
        DyeColor.GREEN      -> setOf("green", "cactus")
        DyeColor.RED        -> setOf("red", "rose")
        DyeColor.YELLOW     -> setOf("yellow", "dandelion")
        DyeColor.LIGHT_GRAY -> setOf("silver", "light gray")
        else                -> setOf(color.name.lowercase().replace('_', ' '))
    }

    override fun solve(items: List<ItemStack>): List<Int> =
        items.mapIndexedNotNull { index, item ->
            if (item.hasGlint() || item.item == Items.BLACK_STAINED_GLASS_PANE) return@mapIndexedNotNull null
            if (validPrefixes.any(item.hoverName.string.lowercase()::startsWith)) index else null
        }

    override fun renderSlot(slotIndex: Int): Pair<Color, String?> = TerminalSolver.selectColor to null
}