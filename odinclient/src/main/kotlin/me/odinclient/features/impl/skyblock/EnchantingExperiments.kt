package me.odinclient.features.impl.skyblock

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.skyblock.LocationUtils.inSkyblock
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemBlock
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object EnchantingExperiments : Module(
    "Auto Experiments",
    category = Category.SKYBLOCK,
    description = "Automatically click on the Chronomatron and Ultrasequencer experiments."
){

    private val delay: Long by NumberSetting("Click Delay", 200, 0, 1000, 10, description = "Time in ms between automatic test clicks.")

    private var currentType = Type.NONE
    private var lastAdded = 0
    private var lastClick = 0L

    private val clickOrder: MutableList<Slot> = mutableListOf()
    private var listenTime = 0L
    private var listening = false
        set(value) {
            if (!field && value) {
                clickOrder.clear()
            }
            field = value
            if (value){
                listenTime = System.currentTimeMillis() + 30
            }
        }
        get() {
            return field || System.currentTimeMillis() < listenTime
        }

    private val chronoIndices = listOf(
        19, 20, 21, 22, 23, 24, 25,
        37, 38, 39, 40, 41, 42, 43
    )

    @SubscribeEvent
    fun onGuiOpen(event: GuiOpenEvent) {
        if (event.gui !is GuiChest || !inSkyblock) return
        val container = (event.gui as GuiChest).inventorySlots
        if (container is ContainerChest) {
            val chestName = container.lowerChestInventory.displayName.unformattedText
            if (chestName.startsWith("Chronomatron (")) {
                currentType = Type.CHRONO
                lastAdded = 0
                clickOrder.clear()
            } else if(chestName.startsWith("Ultrasequencer (")) {
                currentType = Type.SEQUENCE
                lastAdded = 0
                clickOrder.clear()
            }
        }
    }

    @SubscribeEvent
    fun onTick(event: GuiScreenEvent.BackgroundDrawnEvent) {
        if (currentType == Type.NONE || mc.thePlayer == null) return
        val container = mc.thePlayer.openContainer ?: return
        if (container !is ContainerChest) return
        val inventoryName = container.inventorySlots?.get(0)?.inventory?.name
        if (inventoryName == null || !inventoryName.startsWith(if (currentType == Type.CHRONO)"Chronomatron (" else "Ultrasequencer (")) {
            currentType = Type.NONE
            return
        }

        listening = (container.inventorySlots[49].stack?.item as? ItemBlock)?.block === Blocks.glowstone
        if (listening) {
            if (currentType == Type.CHRONO) {
                if (lastAdded > 0) {
                    if ((container.inventorySlots[lastAdded].stack?.item as? ItemBlock)?.block === Blocks.stained_hardened_clay)
                        return
                    else
                        lastAdded = 0
                }
                for (ii in chronoIndices) {
                    val slot = container.inventorySlots[ii]
                    if ((slot.stack?.item as? ItemBlock)?.block === Blocks.stained_hardened_clay) {
                        clickOrder.add(slot)
                        lastAdded = slot.slotNumber
                        break
                    }
                }
            } else if (currentType == Type.SEQUENCE && clickOrder.isEmpty()) {
                clickOrder.addAll(
                    container.inventorySlots.subList(9,45)
                        .filter { it.stack?.item == Items.dye }
                        .sortedBy { it.stack.stackSize }
                )
            }
        } else {
            if (System.currentTimeMillis() < lastClick + delay) return
            val slot = clickOrder.firstOrNull() ?: return
            lastAdded = slot.slotIndex
            mc.playerController.windowClick(
                container.windowId,
                slot.slotNumber,
                2,
                3,
                mc.thePlayer
            )
            clickOrder.removeAt(0)
            lastClick = System.currentTimeMillis()
        }
    }

    private enum class Type {
        NONE,
        CHRONO,
        SEQUENCE
    }
}