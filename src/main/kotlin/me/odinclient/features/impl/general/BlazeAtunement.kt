package me.odinclient.features.impl.general

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.events.impl.ClientSecondEvent
import me.odinclient.events.impl.RenderEntityModelEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.ui.clickgui.util.ColorUtil.withAlpha
import me.odinclient.utils.Utils.noControlCodes
import me.odinclient.utils.VecUtils.xzDistance
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.world.OutlineUtils
import me.odinclient.utils.render.world.RenderUtils.bindColor
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityBlaze
import net.minecraft.entity.monster.EntityPigZombie
import net.minecraft.entity.monster.EntitySkeleton
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

object BlazeAtunement : Module(
    "Blaze Atunement",
    category = Category.GENERAL,
    description = "Displays what atunement a blaze boss needs.",
) {
    private val overlay: Boolean by BooleanSetting("Overlay Entities", false)
    private val thickness: Float by NumberSetting("Outline Thickness", 5f, 5f, 20f, 0.5f)
    private val cancelHurt: Boolean by BooleanSetting("Cancel Hurt", true)

    private var currentBlazes = hashMapOf<Entity, Color>()

    @SubscribeEvent
    fun onSecond(event: ClientSecondEvent) {
        currentBlazes.clear()
        mc.theWorld?.loadedEntityList?.filterIsInstance<EntityArmorStand>()?.forEach { entity ->
            if (currentBlazes.any { it.key == entity }) return@forEach
            val name = entity.name.noControlCodes

            val color = when {
                name.contains("CRYSTAL ♨") -> Color(85, 250, 236)
                name.contains("ASHEN ♨") -> Color(45, 45, 45)
                name.contains("AURIC ♨") -> Color(206, 219, 57)
                name.contains("SPIRIT ♨") -> Color(255, 255, 255)
                else -> return@forEach
            }.withAlpha(0.4f, true)

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
        if (!currentBlazes.containsKey(event.entity)) return
        val color = currentBlazes[event.entity] ?: return

        OutlineUtils.outlineEntity(
            event,
            thickness,
            color,
            cancelHurt
        )
    }

    fun changeBlazeColor(entity: Entity, p_78088_2_: Float, p_78088_3_: Float, p_78088_4_: Float, p_78088_5_: Float, p_78088_6_: Float, scale: Float, ci: CallbackInfo) {
        if (currentBlazes.size == 0 || !overlay) return
        val color = currentBlazes[entity] ?: return
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        color.bindColor()
    }

    fun renderModelBlazePost(entityIn: Entity, p_78088_2_: Float, p_78088_3_: Float, p_78088_4_: Float, p_78088_5_: Float, p_78088_6_: Float, scale: Float, ci: CallbackInfo) {
        if (currentBlazes.size == 0 || !overlay) return
        GlStateManager.disableBlend()
        GlStateManager.enableTexture2D()
    }

    fun changeBipedColor(entity: Entity, p_78088_2_: Float, p_78088_3_: Float, p_78088_4_: Float, p_78088_5_: Float, p_78088_6_: Float, scale: Float, ci: CallbackInfo) {
        if (currentBlazes.size == 0 || !overlay) return
        val color = currentBlazes[entity] ?: return
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        color.bindColor()
    }

    fun renderModelBipedPost(entityIn: Entity, p_78088_2_: Float, p_78088_3_: Float, p_78088_4_: Float, p_78088_5_: Float, p_78088_6_: Float, scale: Float, ci: CallbackInfo) {
        if (currentBlazes.size == 0 || !overlay) return
        GlStateManager.disableBlend()
        GlStateManager.enableTexture2D()
    }
}
