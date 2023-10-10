package me.odinmain.events.impl

import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.Container
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event

/**
 * @see me.odinmain.mixin.MixinGuiContainer.onDrawSlot
 */
@Cancelable
class DrawGuiEvent(val container: Container, val gui: GuiContainer, val xSize: Int, val ySize: Int) : Event()