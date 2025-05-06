package me.odinmain.features.impl.render

import me.odinmain.features.Module
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.skyblock.setLore
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import java.time.LocalDateTime
import java.time.Month
import java.time.format.TextStyle
import java.util.*

object SpaceHelmet : Module(
    name = "Space Helmet",
    desc = "Equips you with a space helmet."
) {
    private val speed by NumberSetting("Speed", 250L, 100, 1000, 10, desc = "The speed at which the color changes.", unit = "ms")
    private var edition = 0
    private val values = listOf(14, 1, 4, 5, 13, 9, 11, 10, 6)
    private var currentIndex = 0

    init {
        execute({ speed }) {
            if (mc.currentScreen !== null) return@execute

            currentIndex = (currentIndex + 1) % values.size
            val item = ItemStack(Item.getItemFromBlock(Blocks.stained_glass), 1, values[currentIndex]).apply { setStackDisplayName("§c§lSpace Helmet") }
                .setLore(listOf("§7A rare space helmet forged", "§7from shards of moon glass", "", "§7To: ${mc.thePlayer?.displayName?.siblings?.firstOrNull()?.formattedText}", "§7From: §6Odin", "", "§8Edition #${edition}", "§8${Month.entries[LocalDateTime.now().monthValue - 1].getDisplayName(TextStyle.FULL, Locale.getDefault())} 2024", "", "§8This item can be reforged!", "§c§lSPECIAL HELMET"))
            edition++
            mc.thePlayer?.inventory?.armorInventory?.set(3, item)
        }
    }
}