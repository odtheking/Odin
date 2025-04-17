package me.odinmain.features.impl.nether

import me.odinmain.events.impl.RenderEntityModelEvent
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.OutlineUtils
import me.odinmain.utils.render.RenderUtils.bind
import me.odinmain.utils.ui.Colors
import me.odinmain.utils.ui.clickgui.util.ColorUtil.withAlpha
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityBlaze
import net.minecraft.entity.monster.EntityPigZombie
import net.minecraft.entity.monster.EntitySkeleton
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.collections.set

object BlazeAttunement : Module(
    name = "Blaze Attunement",
    desc = "Displays what attunement a blaze boss currently requires."
) {
    private val overlay by BooleanSetting("Overlay Entities", false, desc = "Overlay the entities with the color of the attunement.")
    private val thickness by NumberSetting("Outline Thickness", 5f, 5f, 20f, 0.5f, desc = "The thickness of the outline.")

    private var currentBlazes = hashMapOf<Entity, Color>()

    init {
        execute(250) {
            if (!overlay) return@execute
            currentBlazes.clear()
            mc.theWorld?.loadedEntityList?.forEach { entity ->
                if (entity !is EntityArmorStand || currentBlazes.any { it.key == entity }) return@forEach
                val name = entity.name.noControlCodes

                val color = when {
                    name.contains("CRYSTAL ♨") -> Colors.MINECRAFT_AQUA
                    name.contains("ASHEN ♨") -> Colors.MINECRAFT_GRAY
                    name.contains("AURIC ♨") -> Colors.MINECRAFT_YELLOW
                    name.contains("SPIRIT ♨") -> Colors.WHITE
                    else -> return@forEach
                }.withAlpha(.4f)

                currentBlazes[mc.theWorld?.getEntitiesWithinAABBExcludingEntity(entity, entity.entityBoundingBox.offset(0.0, -1.0, 0.0))
                    ?.filter { it is EntityBlaze || it is EntitySkeleton || it is EntityPigZombie }
                    ?.sortedByDescending { it.positionVector.squareDistanceTo(entity.positionVector) }
                    ?.takeIf { it.isNotEmpty() }?.firstOrNull() ?: return@execute] = color
            }
        }
    }

    @SubscribeEvent
    fun onRenderEntityModel(event: RenderEntityModelEvent) {
        OutlineUtils.outlineEntity(event, currentBlazes[event.entity] ?: return, thickness)
    }

    @JvmStatic
    fun changeModelColor(entity: Entity) {
        if (!enabled || currentBlazes.isEmpty() || !overlay) return
        val color = currentBlazes[entity] ?: return
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        color.bind()
    }

    @JvmStatic
    fun renderModelPost() {
        if (!enabled || currentBlazes.isEmpty() || !overlay) return
        GlStateManager.disableBlend()
        GlStateManager.enableTexture2D()
    }
}
