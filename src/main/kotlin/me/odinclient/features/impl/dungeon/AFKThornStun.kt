package me.odinclient.features.impl.dungeon

import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.Utils.equalsOneOf
import me.odinclient.utils.skyblock.ItemUtils.heldItem
import me.odinclient.utils.skyblock.ItemUtils.itemID
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraftforge.client.event.MouseEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ThornStun : Module(
    "AFK Thorn Stun",
    category = Category.DUNGEON,
    description = "Toggle right click while holding Tribal Spear or Bonemerang in F4/M4"
) {

    private var isClicking = false

    @SubscribeEvent
    fun onMouse(event: MouseEvent) {
        if (DungeonUtils.isFloor(4) || event.button != 1 || event.buttonState ||!heldItem?.itemID?.equalsOneOf("TRIBAL_SPEAR", "BONE_BOOMERANG")) return
        event.isCanceled = true
        isClicking = !isClicking
        ChatUtils.modMessage("Thorn Stun: ${if (isClicking) "§aClicking!" else "§cNot Clicking."}")
    }
    // TODO: Add an executor that fires every 250 ms that clicks right click if isClicking is true, I'm currently on laptop

} 
