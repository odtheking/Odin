package me.odinclient.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import net.minecraft.block.BlockCarpet

object NoCarpet : Module(
    name = "No Carpet",
    category = Category.RENDER,
    description = "Removes nearby carpet hitboxes"
) {

    fun noCarpetHook(carpet: BlockCarpet): Boolean {
        if (!enabled) return false
        carpet.setBlockBounds(0f, 0f, 0f, 1f, 0f, 1f)
        return true
    }

}