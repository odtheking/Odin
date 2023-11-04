package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.DrawGuiScreenEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.item.ItemSkull
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object LeapMenu : Module(
    name = "Leap Menu",
    description = "Renders a custom leap menu when in the Spirit Leap gui.",
    category = Category.DUNGEON,
    tag = TagType.NEW
) {
    @SubscribeEvent
    fun onDrawScreen(event: DrawGuiScreenEvent) {
        if (event.gui.inventorySlots.inventorySlots.first().inventory.name != "Spirit Leap") return
        //event.isCanceled = true
        val playerHeads = event.container.inventory?.subList(11, 15)?.filter { it?.item is ItemSkull }?: emptyList()
        playerHeads.forEach {
            val skull = it.item as ItemSkull

            val owner = it.tagCompound?.getCompoundTag("SkullOwner")?.getString("Name") ?: "Unknown"


            val dungeonPlayer = DungeonUtils.teammates.find { player -> player.entity?.name == owner } ?: return@forEach


        }
    }
}