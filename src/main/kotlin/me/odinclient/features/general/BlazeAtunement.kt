package me.odinclient.features.general

import me.odinclient.OdinClient.Companion.config
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.events.ClientSecondEvent
import me.odinclient.events.RenderEntityModelEvent
import me.odinclient.utils.render.OutlineUtils
import me.odinclient.utils.VecUtils.xzDistance
import me.odinclient.utils.render.RenderUtils.bindColor
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityBlaze
import net.minecraft.entity.monster.EntityPigZombie
import net.minecraft.entity.monster.EntitySkeleton
import net.minecraft.util.StringUtils.stripControlCodes
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import java.awt.Color

object BlazeAtunement {

    private var currentBlazes = hashMapOf<Entity, Color>()

    @SubscribeEvent
    fun onSecond(event: ClientSecondEvent) {
        if (!config.atunementOutline) return
        currentBlazes.clear()
        mc.theWorld?.loadedEntityList?.filterIsInstance<EntityArmorStand>()?.forEach { entity ->
            if (currentBlazes.any { it.key == entity }) return@forEach
            val name = stripControlCodes(entity.name)
            val color = when {
                name.contains("CRYSTAL ♨") -> Color(85, 250, 236, 100)
                name.contains("ASHEN ♨") -> Color(45, 45, 45, 100)
                name.contains("AURIC ♨") -> Color(206, 219, 57, 100)
                name.contains("SPIRIT ♨") -> Color(255, 255, 255, 100)
                else -> return@forEach
            }

            val entities =
                mc.theWorld.getEntitiesWithinAABBExcludingEntity(entity, entity.entityBoundingBox.expand(0.0, 3.0, 0.0))
                .filter { it is EntityBlaze || it is EntitySkeleton || it is EntityPigZombie }
                .sortedByDescending { xzDistance(it, entity) }
            if (entities.isEmpty()) return@forEach
            currentBlazes[entities.first()] = color
        }
    }

    @SubscribeEvent
    fun onRenderEntityModel(event: RenderEntityModelEvent) {
        if (!config.atunementOutline || !currentBlazes.containsKey(event.entity)) return
        val color = currentBlazes[event.entity] ?: return
        OutlineUtils.outlineEntity(
            event,
            config.atunementOutlineThickness,
            color,
            config.atunementCancelHurt
        )
    }

    fun changeBlazeColor(entity: Entity, p_78088_2_: Float, p_78088_3_: Float, p_78088_4_: Float, p_78088_5_: Float, p_78088_6_: Float, scale: Float, ci: CallbackInfo) {
        if (currentBlazes.size == 0 || !config.atunementColored) return
        val color = currentBlazes[entity] ?: return
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        color.bindColor()
    }

    fun renderModelBlazePost(entityIn: Entity, p_78088_2_: Float, p_78088_3_: Float, p_78088_4_: Float, p_78088_5_: Float, p_78088_6_: Float, scale: Float, ci: CallbackInfo) {
        if (currentBlazes.size == 0 || !config.atunementColored) return
        GlStateManager.disableBlend()
        GlStateManager.enableTexture2D()
    }

    fun changeBipedColor(entity: Entity, p_78088_2_: Float, p_78088_3_: Float, p_78088_4_: Float, p_78088_5_: Float, p_78088_6_: Float, scale: Float, ci: CallbackInfo) {
        if (currentBlazes.size == 0 || !config.atunementColored) return
        val color = currentBlazes[entity] ?: return
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        color.bindColor()
    }

    fun renderModelBipedPost(entityIn: Entity, p_78088_2_: Float, p_78088_3_: Float, p_78088_4_: Float, p_78088_5_: Float, p_78088_6_: Float, scale: Float, ci: CallbackInfo) {
        if (currentBlazes.size == 0 || !config.atunementColored) return
        GlStateManager.disableBlend()
        GlStateManager.enableTexture2D()
    }
}
