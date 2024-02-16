package me.odinmain.features.impl.kuudra

import me.odinmain.events.impl.DrawSlotEvent
import me.odinmain.events.impl.GuiClickEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.containsOneOf
import me.odinmain.utils.name
import me.odinmain.utils.noControlCodes
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent


object RemovePerks : Module(
    name = "Remove Perks",
    description = "Various kuudra utilities",
    category = Category.KUUDRA
) {
    private val renderStun: Boolean by BooleanSetting("Render Stun", false, description = "Renders the stun perk")
    @SubscribeEvent
    fun renderSlot(event: DrawSlotEvent) {
        if (event.gui !is GuiChest || event.gui.inventorySlots !is ContainerChest || (event.gui.inventorySlots as ContainerChest).name != "Perk Menu") return
        val slot = event.slot.stack?.displayName.noControlCodes
        if (slotCheck(slot)) event.isCanceled = true

    }
    @SubscribeEvent
    fun guiMouseClick(event: GuiClickEvent) {
        if (event.gui !is GuiChest || event.gui.inventorySlots !is ContainerChest || (event.gui.inventorySlots as ContainerChest).name != "Perk Menu") return
        val slot = event.gui.slotUnderMouse?.stack?.displayName.noControlCodes
        if (slotCheck(slot)) event.isCanceled = true
    }

    private fun slotCheck(slot: String): Boolean {
        return slot.containsOneOf("Steady Hands", "Bomberman") || slot == "Elle's Lava Rod" || slot == "Elle's Pickaxe" ||
            slot == "Auto Revive" || (!renderStun && slot.containsOneOf("Mining Frenzy", "Human Cannonball"))
    }
}