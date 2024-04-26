package me.odinmain.events.impl

import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.*
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event

@Cancelable
class DrawGuiContainerScreenEvent(val container: Container, val gui: GuiContainer, val xSize: Int, val ySize: Int, val guiLeft: Int, val guiTop: Int) : Event()

class GuiLoadedEvent(val name: String, val gui: ContainerChest) : Event()

@Cancelable
class GuiClickEvent(val container: Container, val gui: GuiContainer, val x: Int, val y: Int, val button: Int) : Event()

@Cancelable
class PreGuiClickEvent(val button: Int) : Event()

@Cancelable
class GuiKeyPressEvent(val container: Container, val gui: GuiContainer, val keyCode: Int) : Event()

@Cancelable
class GuiClosedEvent(val gui: GuiContainer) : Event()

@Cancelable
class DrawSlotOverlayEvent(val stack: ItemStack?, val x: Int?, val y: Int?, val text: String?) : Event()

@Cancelable
class DrawSlotEvent(val container: Container, val gui: GuiContainer, var slot: Slot, val x: Int, val y: Int) : Event()

@Cancelable
class DrawItemStackEvent(val container: Container, val gui: GuiContainer, val stack: ItemStack, val x: Int, val y: Int) : Event()

