package me.odinmain.features.impl.render

import me.odinmain.OdinMain.mc
import me.odinmain.config.MiscConfig
import me.odinmain.utils.VecUtils.noSqrt3DDistance
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.boss.EntityWither
import net.minecraft.entity.item.EntityArmorStand

object CommonESP {

    var currentEntities = mutableSetOf<Entity>()
    inline val espList get() = MiscConfig.espList

     fun getEntities() {
        mc.theWorld?.loadedEntityList?.forEach(::checkEntity)
    }

    fun checkEntity(entity: Entity) {
        if (entity !is EntityArmorStand || espList.none { entity.name.contains(it, true) } || entity in currentEntities) return
        currentEntities.add(
            mc.theWorld.getEntitiesWithinAABBExcludingEntity(entity, entity.entityBoundingBox.expand(1.0, 5.0, 1.0))
                .filter { it != null && it !is EntityArmorStand && it != mc.thePlayer }
                .minByOrNull { noSqrt3DDistance(it, entity) }
                .takeIf { it !is EntityWither || DungeonUtils.inBoss } ?: return
        )
    }
}