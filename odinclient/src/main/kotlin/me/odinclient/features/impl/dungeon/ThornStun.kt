package me.odinclient.features.impl.dungeon

import me.odinclient.utils.skyblock.PlayerUtils.rightClick
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.skyblock.ChatUtils
import me.odinmain.utils.skyblock.ItemUtils.heldItem
import me.odinmain.utils.skyblock.ItemUtils.itemID
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ThornStun : Module(
    "AFK Thorn Stun",
    category = Category.DUNGEON,
    description = "Toggle right click while holding Tribal Spear or Bonemerang in F4/M4",
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