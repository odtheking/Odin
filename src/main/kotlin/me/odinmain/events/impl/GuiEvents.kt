package me.odinmain.events.impl

import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event


abstract class GuiEvent : Event() {

    @Cancelable
    class DrawGuiBackground(val gui: GuiContainer, val xSize: Int, val ySize: Int, val guiLeft: Int, val guiTop: Int) : GuiEvent()

    @Cancelable
    class DrawGuiForeground(val gui: GuiContainer, val xSize: Int, val ySize: Int, val guiLeft: Int, val guiTop: Int, val mouseX: Int, val mouseY: Int) : GuiEvent()

    @Cancelable
    class DrawSlot(val gui: GuiContainer, val slot: Slot, val x: Int, val y: Int) : GuiEvent()

    @Cancelable
    class DrawSlotOverlay(val stack: ItemStack?, val x: Int?, val y: Int?, val text: String?) : GuiEvent()
    
    @Cancelable
    class MouseClick(val gui: GuiScreen, val button: Int, val x: Int, val y: Int) : GuiEvent()

    @Cancelable
    class MouseRelease(val gui: GuiScreen, val button: Int, val x: Int, val y: Int) : GuiEvent()

    @Cancelable
    class KeyPress(val gui: GuiScreen, val key: Int, val char: Char) : GuiEvent()

    @Cancelable
    class CustomTermGuiClick(val slot: Int, val button: Int) : GuiEvent()
}







