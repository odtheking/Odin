package me.odinmain.features.impl.nether

import me.odinmain.events.impl.GuiEvent
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.*
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object RemovePerks : Module(
    name = "Remove Perks",
    description = "Removes certain perks from the perk menu."
) {
    private val renderStun by BooleanSetting("Render Stun", false, description = "Renders the stun perk")

    @SubscribeEvent
    fun renderSlot(event: GuiEvent.DrawSlotEvent) {
        val container = event.gui.inventorySlots as? ContainerChest ?: return
        if (container.name != "Perk Menu") return

        val slot = event.slot.stack?.displayName?.noControlCodes ?: return
        if (slotCheck(slot)) event.isCanceled = true
    }

    @SubscribeEvent
    fun guiMouseClick(event: GuiEvent.GuiMouseClickEvent) {
        if (event.gui !is GuiChest) return

        val container = event.gui.inventorySlots as? ContainerChest ?: return
        if (container.name != "Perk Menu") return

        val slot = event.gui.slotUnderMouse?.stack?.displayName?.noControlCodes ?: return
        if (slotCheck(slot)) event.isCanceled = true
    }

    private fun slotCheck(slot: String): Boolean {
        return slot.containsOneOf("Steady Hands", "Bomberman") || slot == "Elle's Lava Rod" || slot == "Elle's Pickaxe" ||
            slot == "Auto Revive" || (!renderStun && slot.containsOneOf("Mining Frenzy", "Human Cannonball"))
    }
}