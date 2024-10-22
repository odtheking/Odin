package me.odinmain.features.impl.nether

import me.odinmain.events.impl.GuiEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.*
import me.odinmain.utils.skyblock.unformattedName
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object RemovePerks : Module(
    name = "Remove Perks",
    description = "Removes certain perks from the perk menu.",
    category = Category.NETHER
) {
    private val renderStun by BooleanSetting("Render Stun", false, description = "Renders the stun role perks.")

    @SubscribeEvent
    fun renderSlot(event: GuiEvent.DrawSlotEvent) {
        if ((event.gui.inventorySlots as? ContainerChest)?.name != "Perk Menu") return

        if (slotCheck(event.slot.stack?.unformattedName ?: return)) event.isCanceled = true
    }

    @SubscribeEvent
    fun guiMouseClick(event: GuiEvent.GuiMouseClickEvent) {
        if (event.gui !is GuiChest || (event.gui.inventorySlots as? ContainerChest)?.name != "Perk Menu") return
        if (slotCheck(event.gui.slotUnderMouse?.stack?.unformattedName ?: return)) event.isCanceled = true
    }

    private fun slotCheck(slot: String): Boolean {
        return slot.containsOneOf("Steady Hands", "Bomberman") || slot == "Elle's Lava Rod" || slot == "Elle's Pickaxe" ||
            slot == "Auto Revive" || (!renderStun && slot.containsOneOf("Mining Frenzy", "Human Cannonball"))
    }
}