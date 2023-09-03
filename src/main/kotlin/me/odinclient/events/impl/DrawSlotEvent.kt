package me.odinclient.events.impl

import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.Container
import net.minecraft.inventory.Slot
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event

/**
 * @see me.odinclient.mixin.MixinGuiContainer.onDrawSlot
 */
@Cancelable
class DrawSlotEvent(val container: Container, val gui: GuiContainer, var slot: Slot, val x: Int, val y: Int) : Event()