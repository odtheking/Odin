package me.odinclient.features.impl.general

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityFallingBlock
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object FPS : Module(
    "FPS",
    category = Category.GENERAL
) {
    private val fallingBlocks: Boolean by BooleanSetting("Remove falling blocks", true)
    private val crimsonIsle: Boolean by BooleanSetting("Memory leak", true)

    @SubscribeEvent
    fun onFallingBlock(event: EntityJoinWorldEvent) {
        if (event.entity !is EntityFallingBlock || !fallingBlocks) return
        event.entity.setDead()
    }

    init {
        executor(30000) {
            if (!crimsonIsle || mc.theWorld == null) return@executor
            mc.theWorld.playerEntities.removeAll { it.isDead || isNullVec(it) && it != mc.thePlayer }
        }
    }

    private fun isNullVec(entity: Entity): Boolean = entity.posX == 0.0 && entity.posY == 0.0 && entity.posZ == 0.0
}
