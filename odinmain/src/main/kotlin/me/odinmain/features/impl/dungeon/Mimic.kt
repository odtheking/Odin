package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.RenderChestEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.partyMessage
import me.odinmain.utils.toAABB
import net.minecraft.entity.monster.EntityZombie
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object Mimic : Module(
    "Mimic",
    description = "Helpful mimic utilities.",
    category = Category.DUNGEON
) {
    val mimicMessage: String by StringSetting("Mimic Message", "Mimic Killed!", 128, description = "Message sent when mimic is detected as killed")
    val reset: () -> Unit by ActionSetting("Send message", description = "Sends Mimic killed message in party chat.") { partyMessage(mimicMessage) }
    private val mimicBox: Boolean by BooleanSetting("Mimic Box", false, description = "Draws a box around the mimic chest.")
    private val style: Int by SelectorSetting("Style", Renderer.defaultStyle, Renderer.styles, description = Renderer.styleDesc).withDependency { mimicBox }
    private val color: Color by ColorSetting("Color", Color(0, 0, 0, 0.4f), allowAlpha = true, description = "The color of the box.").withDependency { mimicBox }
    private val lineWidth: Float by NumberSetting("Line Width", 2f, 0.1f, 10f, 0.1f, description = "The width of the box's lines.").withDependency { mimicBox }
    private val depthCheck: Boolean by BooleanSetting("Depth check", false, description = "Boxes show through walls.").withDependency { mimicBox }

    @SubscribeEvent
    fun onEntityDeath(event: LivingDeathEvent) {
        if (!DungeonUtils.inDungeons || event.entity !is EntityZombie) return
        val entity = event.entity as EntityZombie
        if (entity.isChild && (0..3).all { entity.getCurrentArmor(it) == null })
            partyMessage(mimicMessage)
    }

    @SubscribeEvent
    fun onRenderLast(event: RenderChestEvent.Post) {
        if (event.chest.chestType != 1 || !DungeonUtils.inDungeons || DungeonUtils.inBoss || !mimicBox) return
        Renderer.drawStyledBox(event.chest.pos.toAABB(), color, style, lineWidth, depthCheck)
    }

    override fun onKeybind() {
        reset()
    }
}