package me.odinclient.features.impl.skyblock

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.lore
import net.minecraft.item.ItemSword
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object NoBlock : Module(
    name = "No Block",
    description = "Prevents you from blocking with items that have an ability, this is effectively NoSlow.",
    category = Category.SKYBLOCK
) {
    private val onlyBoss: Boolean by BooleanSetting("Only Boss", false, description = "Only prevent blocking in boss fights.")
    private var isRightClickKeyDown = false

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || !LocationUtils.inSkyblock) return
        isRightClickKeyDown = mc.gameSettings.keyBindUseItem.isKeyDown
    }

    @SubscribeEvent
    fun onInteract(event: PlayerInteractEvent) {
        if (!LocationUtils.inSkyblock || event.action != PlayerInteractEvent.Action.RIGHT_CLICK_AIR) return
        if (onlyBoss && !DungeonUtils.inBoss) return
        val item = mc.thePlayer?.heldItem ?: return
        if (item.item !is ItemSword) return

        if (!mc.thePlayer.heldItem.lore.any { it.contains("§6Ability: ") && it.endsWith("§e§lRIGHT CLICK") }) return
        event.isCanceled = true

        if (!isRightClickKeyDown)
            mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
    }
}