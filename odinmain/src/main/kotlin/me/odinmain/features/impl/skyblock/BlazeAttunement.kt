package me.odinmain.features.impl.skyblock

import me.odinmain.events.impl.RenderEntityOutlineEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.world.RenderUtils.bindColor
import me.odinmain.utils.xzDistance
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityBlaze
import net.minecraft.entity.monster.EntityPigZombie
import net.minecraft.entity.monster.EntitySkeleton
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

object BlazeAttunement : Module(
    "Blaze Attunement",
    category = Category.SKYBLOCK,
    description = "Displays what attunement a blaze boss currently needs."
) {
    private val overlay: Boolean by BooleanSetting("Overlay Entities", false)
    private val thickness: Float by NumberSetting("Outline Thickness", 5f, 5f, 20f, 0.5f)
    private val cancelHurt: Boolean by BooleanSetting("Cancel Hurt", true)

    private var currentBlazes = hashMapOf<Entity, Color>()

    init {
        execute(1000) {
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
                }.withAlpha(.4f)

                val entities =
                    mc.theWorld.getEntitiesWithinAABBExcludingEntity(entity, entity.entityBoundingBox.expand(0.0, 3.0, 0.0))
                        .filter { it is EntityBlaze || it is EntitySkeleton || it is EntityPigZombie }
                        .sortedByDescending { xzDistance(it, entity) }
                if (entities.isEmpty()) return@forEach
                currentBlazes[entities.first()] = color
            }
        }
    }

    @SubscribeEvent
    fun onRenderEntityModel(event: RenderEntityOutlineEvent) {
        if (event.type !== RenderEntityOutlineEvent.Type.XRAY) return

        event.queueEntitiesToOutline { entity -> getMob(entity) }
    }

    private fun getMob(entity: Entity): Int? {
        val color = currentBlazes[entity] ?: return null
        return color.rgba
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
