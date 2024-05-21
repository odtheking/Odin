package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.RenderChestEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
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
    private val mimicMessage: String by StringSetting("Mimic Message", "Mimic Killed!", 128, description = "Message sent when mimic is detected as killed")
    val reset: () -> Unit by ActionSetting("Send message", description = "Sends Mimic killed message in party chat.") {
        partyMessage(mimicMessage)
    }
    private val mimicBox: Boolean by BooleanSetting("Mimic Box", false, description = "Draws a box around the mimic chest.")
    private val style: Int by SelectorSetting("Style", "Filled", arrayListOf("Filled", "Outline", "Filled Outline"), description = "Whether or not the box should be filled.").withDependency { mimicBox }
    private val color: Color by ColorSetting("Color", Color.ORANGE.withAlpha(.4f), allowAlpha = true, description = "The color of the box.").withDependency { mimicBox }

    @SubscribeEvent
    fun onEntityDeath(event: LivingDeathEvent) {
        if (!DungeonUtils.inDungeons || event.entity !is EntityZombie) return
        val entity = event.entity as EntityZombie
        if (entity.isChild && (0..3).all { entity.getCurrentArmor(it) == null })
            partyMessage(mimicMessage)
    }

    @SubscribeEvent
    fun onRenderLast(event: RenderChestEvent.Post) {
        if (event.chest.chestType != 1) return
        Renderer.drawBox(
            event.chest.pos.toAABB(), color, depth = true,
            outlineAlpha = if (style == 0) 0 else color.alpha, fillAlpha = if (style == 1) 0 else color.alpha)
    }

    override fun onKeybind() {
        reset()
    }
}