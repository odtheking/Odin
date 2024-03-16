package me.odinmain.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.getCurrentMonthName
import me.odinmain.utils.skyblock.setLore
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

object SpaceHelmet : Module(
    name = "Space Helmet",
    category = Category.RENDER,
    description = "Equips you with a space helmet."
) {
    private val speed: Long by NumberSetting("Speed", 250, 100, 1000, 10, description = "The speed at which the color changes.")
    private var edition = 0
    private val values = listOf(14, 1, 4, 5, 13, 9, 11, 10, 6)
    private var currentIndex = 0

    init {
        execute({ speed }) {
            if (mc.thePlayer == null || mc.currentScreen !== null) return@execute

            val color = values[currentIndex]
            currentIndex = (currentIndex + 1) % values.size
            val item = ItemStack(Item.getItemFromBlock(Blocks.stained_glass), 1, color).apply {
                setStackDisplayName("§c§lSpace Helmet")
            }.setLore(listOf("§7A rare space helmet forged", "§7from shards of moon glass", "", "§7To: ${mc.thePlayer.displayName.siblings.firstOrNull()?.formattedText}", "§7From: §6Odin", "", "§8Edition #${edition}", "§8${getCurrentMonthName()} 2024", "", "§8This item can be reforged!", "§c§lSPECIAL HELMET"))
            edition += 1
            mc.thePlayer?.inventory?.armorInventory?.set(3, item)
        }
    }
}