package me.odinclient.features.impl.dungeon

import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.utils.Utils.equalsOneOf
import me.odinclient.utils.skyblock.ChatUtils
import me.odinclient.utils.skyblock.ItemUtils.heldItem
import me.odinclient.utils.skyblock.ItemUtils.itemID
import me.odinclient.utils.skyblock.PlayerUtils.rightClick
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ThornStun : Module(
    "AFK Thorn Stun",
    category = Category.DUNGEON,
    description = "Holds right click while holding Tribal Spear or Bonemerang in F4/M4",
    tag = TagType.NEW
) {

    private var isClicking = false
    private val delay: Long by NumberSetting("Delay", 250, 100, 1000, 10)

    @SubscribeEvent
    fun onInteract(event: PlayerInteractEvent) {
        if (
            DungeonUtils.isFloor(4) ||
            !event.action.equalsOneOf(PlayerInteractEvent.Action.RIGHT_CLICK_AIR, PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) ||
            !heldItem?.itemID.equalsOneOf("TRIBAL_SPEAR", "BONE_BOOMERANG")
        ) return
        event.isCanceled = true
        isClicking = !isClicking
        ChatUtils.modMessage("Thorn Stun: ${if (isClicking) "§aClicking!" else "§cNot Clicking."}")
    }

    init {
        execute(delay = { delay }) {
            if (!isClicking || !heldItem?.itemID.equalsOneOf("TRIBAL_SPEAR", "BONE_BOOMERANG")) return@execute
            rightClick()
        }
    }
} 
