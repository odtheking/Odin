package me.odinclient.events.impl

import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.Container
import net.minecraft.inventory.Slot
import net.minecraftforge.fml.common.eventhandler.Event

/**
 * @see me.odinclient.mixin.MixinGuiContainer.onDrawSlot
 */
class DrawSlotEvent(container: Container, gui: GuiContainer, var slot: Slot) : Event()