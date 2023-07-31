package me.odinclient.events

import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.Container
import net.minecraft.inventory.Slot
import net.minecraftforge.fml.common.eventhandler.Event

class DrawSlotEvent(container: Container, gui: GuiContainer, var slot: Slot) : Event()