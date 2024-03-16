package me.odinclient.features.impl.floor7

import me.odinclient.utils.skyblock.PlayerUtils.rightClick
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.Vec2
import me.odinmain.utils.skyblock.itemID
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object Relics : Module(
    name = "Relics",
    category = Category.FLOOR7,
    description = "Various features for relics in m7 p5"
) {
    private val blockWrongClicks: Boolean by BooleanSetting("Block Wrong Clicks")
    private val triggerBot: Boolean by BooleanSetting("Triggerbot")

    private val currentRelic get() = "PURPLE_KING_RELIC"//mc.thePlayer.inventory?.mainInventory?.get(8)?.itemID
    private val cauldronMap = mapOf(
        "GREEN_KING_RELIC" to Vec2(49, 44),
        "Red_KING_RELIC" to Vec2(51, 42),
        "PURPLE_KING_RELIC" to Vec2(54, 41),
        "ORANGE_KING_RELIC" to Vec2(57, 42),
        "BLUE_KING_RELIC" to Vec2(59, 44)
    )
    private val tbClock = Clock(1000)

    @SubscribeEvent
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (
            event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK ||
            currentRelic == "" ||
            !blockWrongClicks ||
            Vec2(event.pos.x, event.pos.z) !in cauldronMap.values ||
            Vec2(event.pos.x, event.pos.z) == cauldronMap[currentRelic] ||
            !event.pos.y.equalsOneOf(6, 7)
        ) return
        event.isCanceled = true
    }

    @SubscribeEvent
    fun onClientTickEvent(event: ClientTickEvent) {
        if (!triggerBot || !tbClock.hasTimePassed()) return
        val obj = mc.objectMouseOver ?: return
        if (obj.entityHit is EntityArmorStand && obj.entityHit?.inventory?.get(4)?.itemID in cauldronMap.keys) {
            rightClick()
            tbClock.update()
        }

        if (
            Vec2(obj.blockPos?.x ?: 0, obj.blockPos?.z ?: 0) != cauldronMap[currentRelic] ||
            !obj.blockPos?.y.equalsOneOf(6, 7)
        ) return
        rightClick()
        tbClock.update()
    }
}