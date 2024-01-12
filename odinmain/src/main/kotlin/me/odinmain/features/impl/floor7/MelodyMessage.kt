package me.odinmain.features.impl.floor7

import me.odinmain.events.impl.GuiClosedEvent
import me.odinmain.events.impl.GuiLoadedEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.StringSetting
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.partyMessage
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object MelodyMessage : Module(
    name = "Melody Message",
    description = "Sends a message whenever you open the melody terminal.",
    category = Category.FLOOR7,
    tag = TagType.NEW
) {
    private val melodyMessage: String by StringSetting("Melody Message", "Melody Terminal start!", 128, description = "Message sent when the melody terminal opens")

    private var saidMelody = false
    @SubscribeEvent
    fun onGuiLoad(event: GuiLoadedEvent) {
        if (!DungeonUtils.inDungeons) return
        if (saidMelody) return
        if (!event.name.startsWith("Click the button on time!")) return

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

}