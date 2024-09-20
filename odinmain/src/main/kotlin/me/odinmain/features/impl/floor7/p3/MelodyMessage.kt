package me.odinmain.features.impl.floor7.p3

import me.odinmain.events.impl.GuiEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.StringSetting
import me.odinmain.utils.name
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.partyMessage
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object MelodyMessage : Module(
    name = "Melody Message",
    description = "Helpful messages for the melody terminal in floor 7.",
    category = Category.FLOOR7
) {
    private val sendMelodyMessage: Boolean by BooleanSetting("Send Melody Message", true, description = "Sends a message when the melody terminal opens.")
    private val melodyMessage: String by StringSetting("Melody Message", "Melody Terminal start!", 128, description = "Message sent when the melody terminal opens.").withDependency { sendMelodyMessage }
    private val melodyProgress: Boolean by BooleanSetting("Melody Progress", false, description = "Tells the party about melody terminal progress.")

    private var saidMelody = false
    private var claySlots = hashMapOf(25 to "Melody terminal is at 25%", 34 to "Melody terminal is at 50%", 43 to "Melody terminal is at 75%")

    @SubscribeEvent
    fun onGuiLoad(event: GuiEvent.GuiLoadedEvent) {
        if (!DungeonUtils.inDungeons || saidMelody || !event.name.startsWith("Click the button on time!")) return
        if (sendMelodyMessage) partyMessage(melodyMessage)

        claySlots = hashMapOf(25 to "Melody terminal is at 25%", 34 to "Melody terminal is at 50%", 43 to "Melody terminal is at 75%")
        saidMelody = true
    }

    @SubscribeEvent
    fun onGuiClose(event: GuiEvent.GuiClosedEvent) {
        saidMelody = false
    }

    init {
        onWorldLoad { saidMelody = false }
    }

    init {
        execute(50){
            val containerChest = mc.thePlayer.openContainer as? ContainerChest ?: return@execute
            if (containerChest.name != "Click the button on time!" || !melodyProgress) return@execute

            val greenClayIndices = claySlots.keys.filter { index -> containerChest.getSlot(index)?.stack?.metadata == 5 }.ifEmpty { return@execute }

            partyMessage(claySlots[greenClayIndices.last()] ?: return@execute)
            greenClayIndices.forEach { claySlots.remove(it) }
        }
    }
}