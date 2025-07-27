package me.odinmain.features.impl.dungeon

import me.odinmain.OdinMain.isLegitVersion
import me.odinmain.clickgui.settings.Setting.Companion.withDependency
import me.odinmain.clickgui.settings.impl.*
import me.odinmain.events.impl.EntityLeaveWorldEvent
import me.odinmain.events.impl.RenderChestEvent
import me.odinmain.features.Module
import me.odinmain.utils.render.Color.Companion.withAlpha
import me.odinmain.utils.render.Colors
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonListener
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.getSkullValue
import me.odinmain.utils.skyblock.partyMessage
import me.odinmain.utils.toAABB
import me.odinmain.utils.writeToClipboard
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object Mimic : Module(
    name = "Mimic",
    description = "Highlights and announces mimic kills in dungeons."
) {
    private val mimicMessageToggle by BooleanSetting("Toggle Mimic Message", true, desc = "Toggles the mimic killed message.")
    val mimicMessage by StringSetting("Mimic Message", "Mimic Killed!", 128, desc = "Message sent when mimic is detected as killed.").withDependency { mimicMessageToggle }
    private val reset by ActionSetting("Mimic Killed", desc = "Sends Mimic killed message in party chat.") { mimicKilled() }
    private val mimicBox by BooleanSetting("Mimic Box", true, desc = "Draws a box around the mimic chest.")
    private val style by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, desc = Renderer.STYLE_DESCRIPTION).withDependency { mimicBox }
    private val color by ColorSetting("Color", Colors.MINECRAFT_RED.withAlpha(0.5f), allowAlpha = true, desc = "The color of the box.").withDependency { mimicBox }
    private val lineWidth by NumberSetting("Line Width", 2f, 0.1f, 10f, 0.1f, desc = "The width of the box's lines.").withDependency { mimicBox }

    private val princeDetection by BooleanSetting("Prince Detection", default = false, desc = "Enables prince detection. Only enable if you have 100% extra score chance from princes.")
    private val princeMessageToggle by BooleanSetting("Toggle Prince Message", false, desc = "Toggles the prince killed message.").withDependency { princeDetection }
    val princeMessage by StringSetting("Prince Message", "Prince Killed!", 128, desc = "Message sent when prince is detected as killed.").withDependency { princeDetection && princeMessageToggle }
    val range by NumberSetting("Range", 10f, 2f, 30f, 1f, desc = "The range at which princes will be detected.", unit = " Blocks").withDependency { princeDetection && princeMessageToggle }
    private val princeReset by ActionSetting("Prince Killed", desc = "Sends Prince killed message in party chat.") { princeKilled() }.withDependency { princeDetection }

    private const val MIMIC_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTY3Mjc2NTM1NTU0MCwKICAicHJvZmlsZUlkIiA6ICJhNWVmNzE3YWI0MjA0MTQ4ODlhOTI5ZDA5OTA0MzcwMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJXaW5zdHJlYWtlcnoiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTE5YzEyNTQzYmM3NzkyNjA1ZWY2OGUxZjg3NDlhZThmMmEzODFkOTA4NWQ0ZDRiNzgwYmExMjgyZDM1OTdhMCIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9"
    private const val PRINCE_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTU5MDE3Njk2NjUxNywKICAicHJvZmlsZUlkIiA6ICJhMmY4MzQ1OTVjODk0YTI3YWRkMzA0OTcxNmNhOTEwYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJiUHVuY2giLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDk5OTEyM2NmMGE0MTQ3N2Y3MDZmYzZhNTA4OTE0NjNlOGNiNTBkY2JkZDJjODFjNjUyZmNlZjZmZGJkZTIxYyIKICAgIH0KICB9Cn0="

    @SubscribeEvent
    fun onEntityLeaveWorld(event: EntityLeaveWorldEvent) {
        if (DungeonUtils.inDungeons && event.entity is EntityZombie && event.entity.isChild && !event.entity.isEntityAlive && getSkullValue(event.entity) == MIMIC_TEXTURE) mimicKilled()
    }

    @SubscribeEvent
    fun onEntityDeath(event: LivingDeathEvent) = with(event.entity) {
        if (!DungeonUtils.inDungeons) return@with
        when {
            princeDetection && this is EntityOtherPlayerMP && getEntityTexture(this) == PRINCE_TEXTURE && mc.thePlayer.getDistanceToEntity(this) <= range -> princeKilled()
            this is EntityZombie && isChild && (0..3).all { getCurrentArmor(it) == null } -> mimicKilled()
        }
    }

    @SubscribeEvent
    fun onRenderLast(event: RenderChestEvent.Post) {
        if (!mimicBox || !DungeonUtils.inDungeons || DungeonUtils.inBoss || event.chest.chestType != 1) return
        Renderer.drawStyledBox(event.chest.pos.toAABB(), color = color, style =  style, width = lineWidth, depth = isLegitVersion)
    }

    private fun mimicKilled() {
        if (DungeonUtils.mimicKilled || DungeonUtils.inBoss) return
        if (mimicMessageToggle) partyMessage(mimicMessage)
        DungeonListener.dungeonStats.mimicKilled = true
    }

    private fun princeKilled() {
        if (DungeonUtils.princeKilled || DungeonUtils.inBoss) return
        if (princeMessageToggle) partyMessage(princeMessage)
        DungeonListener.dungeonStats.princeKilled = true
    }

    private fun getEntityTexture(entity: EntityPlayer) = entity.gameProfile.properties.get("textures")?.firstOrNull()?.value
}