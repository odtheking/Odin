package me.odinmain.features.impl.floor7

import me.odinmain.events.impl.GuiClosedEvent
import me.odinmain.events.impl.GuiLoadedEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.StringSetting
import me.odinmain.utils.name
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.partyMessage
import me.odinmain.utils.skyblock.sendCommand
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object MelodyMessage : Module(
    name = "Melody Message",
    description = "Sends a message whenever you open the melody terminal.",
    category = Category.FLOOR7
) {
    private val melodyMessage: String by StringSetting("Melody Message", "Melody Terminal start!", 128, description = "Message sent when the melody terminal opens")
    private val melodyProgress: Boolean by BooleanSetting("Melody Progress", false, description = "Tells the party about melody terminal progress.")
    private var saidMelody = false
    @SubscribeEvent
    fun onGuiLoad(event: GuiLoadedEvent) {
        if (!DungeonUtils.inDungeons || saidMelody || !event.name.startsWith("Click the button on time!")) return

        partyMessage(melodyMessage)
        saidMelody = true
    }

    @SubscribeEvent
    fun onGuiClose(event: GuiClosedEvent) {
        saidMelody = false
    }

    @SubscribeEvent
    fun onWorldUnload(event: WorldEvent.Unload){
        saidMelody = false
    }

    private val claySlots = hashMapOf(
        25 to "pc is at 1/4",
        34 to "pc is at 2/4",
        43 to "pc is at 3/4"
    )

    init {
        execute(50){
            val containerChest = mc.thePlayer.openContainer as? ContainerChest ?: return@execute
            if (containerChest.name != "Click the button on time!" || melodyProgress) return@execute

            val greenClayIndices = claySlots.keys.filter { index -> containerChest.getSlot(index)?.stack?.metadata == 5 }
            if (greenClayIndices.isEmpty()) return@execute

            sendCommand(claySlots[greenClayIndices.last()] ?: return@execute)
            greenClayIndices.forEach { claySlots.remove(it) }
        }
    }
}