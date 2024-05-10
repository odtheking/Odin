package me.odinmain.events.impl

import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.*
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event


abstract class GuiEvent : Event() {

    data class GuiLoadedEvent(val name: String, val gui: ContainerChest) : GuiEvent()

    @Cancelable
    data class DrawGuiContainerScreenEvent(val container: Container, val gui: GuiContainer, val xSize: Int, val ySize: Int, val guiLeft: Int, val guiTop: Int) : GuiEvent()

    @Cancelable
    class DrawSlotEvent(val container: Container, val gui: GuiContainer, var slot: Slot, val x: Int, val y: Int) : GuiEvent()

    @Cancelable
    class DrawSlotOverlayEvent(val stack: ItemStack?, val x: Int?, val y: Int?, val text: String?) : GuiEvent()

    @Cancelable
    data class GuiMouseClickEvent(val gui: GuiScreen, val button: Int, val x: Int, val y: Int) : GuiEvent()

    @Cancelable
    class GuiKeyPressEvent(val container: Container, val gui: GuiContainer, val keyCode: Int) : Event()


    class GuiClosedEvent(val gui: GuiContainer) : GuiEvent()

    @Cancelable
    class GuiWindowClickEvent(val windowId: Int, val slotId: Int, val mouseButtonClicked: Int, val mode: Int, val playerIn: EntityPlayer) : GuiEvent()
}







