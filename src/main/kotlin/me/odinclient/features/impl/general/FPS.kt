package me.odinclient.features.impl.general

import me.odinclient.OdinClient.Companion.config
import me.odinclient.OdinClient.Companion.mc
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityFallingBlock
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object FPS {
    @SubscribeEvent
    fun onFallingBlock(event: EntityJoinWorldEvent) {
        if (event.entity !is EntityFallingBlock || !config.fps) return
        event.entity.setDead()
    }

    private var lastClear = System.currentTimeMillis()
    
    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (System.currentTimeMillis() - lastClear < 30000L) return
        val world = mc.theWorld ?: return
        world.playerEntities.removeAll { it.isDead || isNullVec(it) && it != mc.thePlayer }
        lastClear = System.currentTimeMillis()
    }

    private fun isNullVec(entity: Entity): Boolean = entity.posX == 0.0 && entity.posY == 0.0 && entity.posZ == 0.0
}
