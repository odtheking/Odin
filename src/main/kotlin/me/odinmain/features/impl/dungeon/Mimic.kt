package me.odinmain.features.impl.dungeon

import me.odinmain.OdinMain.isLegitVersion
import me.odinmain.events.impl.EntityLeaveWorldEvent
import me.odinmain.events.impl.RenderChestEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.LocationUtils.currentDungeon
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.getSkullValue
import me.odinmain.utils.skyblock.partyMessage
import me.odinmain.utils.toAABB
import net.minecraft.entity.monster.EntityZombie
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object Mimic : Module(
    name = "Mimic",
    description = "Helpful mimic utilities.",
    category = Category.DUNGEON
) {
    private val mimicMessageToggle by BooleanSetting("Toggle Mimic Message", default = true, description = "Toggles the mimic killed message.")
    val mimicMessage by StringSetting("Mimic Message", "Mimic Killed!", 128, description = "Message sent when mimic is detected as killed.").withDependency { mimicMessageToggle }
    val reset by ActionSetting("Mimic Killed", description = "Sends Mimic killed message in party chat.") { mimicKilled() }
    private val mimicBox by BooleanSetting("Mimic Box", true, description = "Draws a box around the mimic chest.")
    private val style by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, description = Renderer.STYLE_DESCRIPTION).withDependency { mimicBox }
    private val color by ColorSetting("Color", Color.RED.withAlpha(0.5f), allowAlpha = true, description = "The color of the box.").withDependency { mimicBox }
    private val lineWidth by NumberSetting("Line Width", 2f, 0.1f, 10f, 0.1f, description = "The width of the box's lines.").withDependency { mimicBox }

    private const val MIMIC_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTY3Mjc2NTM1NTU0MCwKICAicHJvZmlsZUlkIiA6ICJhNWVmNzE3YWI0MjA0MTQ4ODlhOTI5ZDA5OTA0MzcwMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJXaW5zdHJlYWtlcnoiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTE5YzEyNTQzYmM3NzkyNjA1ZWY2OGUxZjg3NDlhZThmMmEzODFkOTA4NWQ0ZDRiNzgwYmExMjgyZDM1OTdhMCIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9"

    @SubscribeEvent
    fun onEntityLeaveWorld(event: EntityLeaveWorldEvent) {
        if (DungeonUtils.inDungeons && event.entity is EntityZombie && event.entity.isChild && getSkullValue(event.entity).equals(MIMIC_TEXTURE)) mimicKilled()
    }

    @SubscribeEvent
    fun onEntityDeath(event: LivingDeathEvent) = with(event.entity) {
        if (!DungeonUtils.inDungeons || this !is EntityZombie || !this.isChild || !(0..3).all { this.getCurrentArmor(it) == null }) return
        mimicKilled()
    }

    @SubscribeEvent
    fun onRenderLast(event: RenderChestEvent.Post) {
        if (!mimicBox || !DungeonUtils.inDungeons || DungeonUtils.inBoss || event.chest.chestType != 1) return
        Renderer.drawStyledBox(event.chest.pos.toAABB(), color = color, style =  style, width = lineWidth, depth = isLegitVersion)
    }

    private fun mimicKilled() {
        if (DungeonUtils.mimicKilled || DungeonUtils.inBoss) return
        if (mimicMessageToggle) partyMessage(mimicMessage)
        currentDungeon?.dungeonStats?.mimicKilled = true
    }
}