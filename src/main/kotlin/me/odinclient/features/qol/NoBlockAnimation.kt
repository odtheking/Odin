package me.odinclient.features.qol

import me.odinclient.OdinClient.Companion.config
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.utils.skyblock.LocationUtils
import net.minecraft.item.ItemSword
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import me.odinclient.utils.skyblock.ItemUtils.lore

object NoBlockAnimation {
    private var isRightClickKeyDown = false

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || !config.noBlockAnimation || !LocationUtils.inSkyblock) return
        isRightClickKeyDown = mc.gameSettings.keyBindUseItem.isKeyDown
    }

    @SubscribeEvent
    fun onInteract(event: PlayerInteractEvent) {
        if (!config.noBlockAnimation || !LocationUtils.inSkyblock || event.action != PlayerInteractEvent.Action.RIGHT_CLICK_AIR) return
        val item = mc.thePlayer.heldItem ?: return
        if (item.item !is ItemSword) return
        if (!mc.thePlayer.heldItem.lore.any { it.contains("§6Ability: ") && it.endsWith("§e§lRIGHT CLICK") }) return
        event.isCanceled = true
        if (isRightClickKeyDown) return
        mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
    }
}