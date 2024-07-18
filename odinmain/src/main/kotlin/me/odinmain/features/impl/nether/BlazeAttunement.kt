package me.odinmain.features.impl.nether

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.color.multiplyAlpha
import me.odinmain.events.impl.RenderEntityModelEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.OutlineUtils
import me.odinmain.utils.render.RenderUtils.bind
import me.odinmain.utils.xzDistance
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.*
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object BlazeAttunement : Module(
    name = "Blaze Attunement",
    category = Category.NETHER,
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
                    name.contains("CRYSTAL ♨") -> Color.RGB(85, 250, 236)
                    name.contains("ASHEN ♨") -> Color.RGB(45, 45, 45)
                    name.contains("AURIC ♨") -> Color.RGB(206, 219, 57)
                    name.contains("SPIRIT ♨") -> Color.RGB(255, 255, 255)
                    else -> return@forEach
                }.multiplyAlpha(.4f)

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
    fun onRenderEntityModel(event: RenderEntityModelEvent) {
        val color = currentBlazes[event.entity] ?: return

        OutlineUtils.outlineEntity(event, thickness, color, cancelHurt)
    }

    fun changeBlazeColor(entity: Entity) {
        if (currentBlazes.size == 0 || !overlay) return
        val color = currentBlazes[entity] ?: return
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        color.bind()
    }

    fun renderModelBlazePost() {
        if (currentBlazes.size == 0 || !overlay) return
        GlStateManager.disableBlend()
        GlStateManager.enableTexture2D()
    }

    fun changeBipedColor(entity: Entity) {
        if (currentBlazes.size == 0 || !overlay) return
        val color = currentBlazes[entity] ?: return
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        color.bind()
    }

    fun renderModelBipedPost() {
        if (currentBlazes.size == 0 || !overlay) return
        GlStateManager.disableBlend()
        GlStateManager.enableTexture2D()
    }
}
