package me.odinclient.features.impl.render

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.features.settings.impl.ColorSetting
import me.odinclient.features.settings.impl.SelectorSetting
import me.odinclient.utils.skyblock.ChatUtils.modMessage
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.world.EntityUtils
import me.odinclient.utils.skyblock.Area
import me.odinclient.utils.skyblock.LocationUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityGuardian
import net.minecraft.entity.monster.EntityIronGolem
import net.minecraft.item.ItemSkull
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.roundToInt

object CrimsonSeaCreature : Module(
    name = "Crimson Fishing",
    category = Category.RENDER
) {

    private val crimsonNotify: Boolean by BooleanSetting("Crimson SC alert", description = "Sends a message in chat when a rare sea creature is caught (at render distance).")
    private val crimsonHighlight: Int by SelectorSetting("Crimson SC", "Highlight", arrayListOf("Off", "Highlight", "ESP"), description = "Highlight rare crimson sea creatures and thunder sparks.")
    private val crimsonColor: Color by ColorSetting("Crimson SC color", Color(0, 255, 255), description = "Color of crimson mobs.")

    private const val sparkTexture =
        "ewogICJ0aW1lc3RhbXAiIDogMTY0MzUwNDM3MjI1NiwKICAicHJvZmlsZUlkIiA6ICI2MzMyMDgwZTY3YTI0Y2MxYjE3ZGJhNzZmM2MwMGYxZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJUZWFtSHlkcmEiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2IzMzI4ZDNlOWQ3MTA0MjAzMjI1NTViMTcyMzkzMDdmMTIyNzBhZGY4MWJmNjNhZmM1MGZhYTA0YjVjMDZlMSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9"
    private val knownEntities = mutableSetOf<Entity>()

    private fun isSpark(entity: Entity): Boolean {
        return (entity is EntityArmorStand) && run {
            entity.heldItem?.let {
                if (it.item !is ItemSkull) return false
                val nbt = it.tagCompound ?: return false
                if (!nbt.hasKey("SkullOwner", 10)) return false
                sparkTexture == nbt
                    .getCompoundTag("SkullOwner")
                    .getCompoundTag("Properties")
                    .getTagList("textures", 10)
                    .getCompoundTagAt(0)
                    .getString("Value")
            } ?: false
        }
    }

    @SubscribeEvent
    fun onWorldUnload(event: WorldEvent.Unload) {
        knownEntities.clear()
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (LocationUtils.area != Area.CrimsonIsle) return
        mc.theWorld.loadedEntityList.forEach {
            if (it is EntityIronGolem || (it is EntityGuardian && it.isElder)) {
                if (crimsonHighlight != 0) {
                    EntityUtils.drawEntityBox(
                        entity = it,
                        color = crimsonColor,
                        outline = true,
                        fill = false,
                        esp = crimsonHighlight == 2,
                        partialTicks = event.partialTicks
                    )
                }
                if (crimsonNotify && !knownEntities.contains(it)) {
                    val distance = it.positionVector.distanceTo(mc.thePlayer.positionVector)
                    if (it is EntityIronGolem) {
                        modMessage("§c§lA legendary creature has been spotted §e§l${distance.roundToInt()} blocks §c§lfrom afar... Lord Jawbus has arrived.")
                        mc.thePlayer.playSound("random.orb", 1f, 0.5f)
                    } else {
                        modMessage("§c§lYou hear a massive rumble as thunder emerges §e§l${distance.roundToInt()} blocks §c§lfrom afar.")
                        mc.thePlayer.playSound("random.orb", 1f, 0.5f)
                    }
                }
                knownEntities.add(it)
            } else if (isSpark(it) && crimsonHighlight != 0) {
                EntityUtils.drawEntityBox(
                    entity = it,
                    color = crimsonColor,
                    outline = true,
                    fill = true,
                    esp = crimsonHighlight == 2,
                    partialTicks = event.partialTicks,
                    offset = Triple(-0.2F, -0.5F, -0.1F),
                    expansion = Triple(-0.1, -0.85, -0.1)
                )
            }
        }
    }
}