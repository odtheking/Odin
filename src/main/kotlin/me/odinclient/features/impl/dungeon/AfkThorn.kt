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
        if (DungeonUtils.isFloor(4) || event.button != 1) return
        event.isCanceled = isClicking
        if (heldItem?.itemID?.equalsOneOf("TRIBAL_SPEAR", "BONE_BOOMERANG") == true || isClicking) {
            if (event.buttonstate) {
                isClicking = !isClicking
            }
        }
    }
}