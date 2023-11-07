package me.odinmain.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.NumberSetting
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

object SpaceHelmet : Module(
    name = "Space Helmet",
    category = Category.RENDER,
    description = "Equips you with a space helmet.",
    tag = TagType.NEW
) {
    private val values = listOf(14, 1, 4, 5, 13, 9, 11, 10, 6) // Define the values you want to cycle through
    private var currentIndex = 0 // Initialize the counter
    private val speed: Long by NumberSetting("Speed", 250, 100, 1000, 10)

    init {
        execute({ speed }) {
            if (mc.thePlayer == null || mc.currentScreen !== null) return@execute


            val color = values[currentIndex]
            currentIndex = (currentIndex + 1) % values.size
            val item = ItemStack(Item.getItemFromBlock(Blocks.stained_glass), 1, color).apply {
                setStackDisplayName("§c§lSpace Helmet")
            }

            mc.thePlayer?.inventory?.armorInventory?.set(3, item)

        }
    }
}