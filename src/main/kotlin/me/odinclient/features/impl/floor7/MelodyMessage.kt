package me.odinclient.features.impl.floor7

import me.odinclient.events.impl.GuiClosedEvent
import me.odinclient.events.impl.GuiLoadedEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.StringSetting
import me.odinclient.utils.skyblock.ChatUtils
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object MelodyMessage : Module(
    name = "Melody Message",
    description = "Sends a message whenever you open the melody terminal.",
    category = Category.FLOOR7,
    tag = TagType.NEW
) {
    private val melodyMessage: String by StringSetting("Melody Message", "Mimic Killed", 40, description = "Message sent when the melody terminal opens")

    private var saidMelody = false
    @SubscribeEvent
    fun onGuiLoad(event: GuiLoadedEvent) {
        if (!DungeonUtils.inDungeons) return
        if (saidMelody) return
        if (!event.name.startsWith("Click the button on time!")) return

        ChatUtils.partyMessage(melodyMessage)
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