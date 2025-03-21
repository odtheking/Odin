package me.odinmain.features.impl.skyblock

import com.github.stivais.aurora.dsl.at
import com.github.stivais.aurora.elements.impl.Text.Companion.textSupplied
import me.odinmain.events.impl.PostEntityMetadata
import me.odinmain.features.Module
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.skyblock.getSkullValue
import me.odinmain.utils.ui.TextHUD
import me.odinmain.utils.ui.image
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object DeployableTimer : Module(
    name = "Deployable Timer",
    description = "Displays the active deployable and it's time left."
) {
    private val HUD by TextHUD("Deployable HUD") { color, font, shadow ->
        needs { getRenderDeployable() != null }
        row {
            image(image = "clickgui/chevron.svg".image(), at())
            text("AA", font, color)
            column {
                textSupplied({ getRenderDeployable() ?: return@textSupplied "" }, font, color)
            }
        }

    }.setting(description = "Displays the cooldown.")

    private fun getRenderDeployable(): Deployable? {
        val activeDeployable = activeDeployables.firstOrNull { dep -> mc.thePlayer.getDistanceToEntity(dep.entity) <= dep.deployable.range } ?: return null
        val timeLeft = (activeDeployable.timeAdded + activeDeployable.duration - System.currentTimeMillis()) / 1000

        if (timeLeft <= 0 || activeDeployable.entity.isDead) {
            activeDeployables.remove(activeDeployable)
            activeDeployables.sortByDescending { dep -> dep.deployable.priority }
            return getRenderDeployable()
        }
        return activeDeployable
    }

    data class Deployable(val deployable: DeployableTypes, val entity: EntityArmorStand, val duration: Int, val timeAdded: Long = System.currentTimeMillis())

    private val activeDeployables = mutableListOf<Deployable>()
    private val orbRegex = Regex("(.+) (\\d+)s")

    @SubscribeEvent
    fun postMetadata(event: PostEntityMetadata) {
        val entity = mc.theWorld?.getEntityByID(event.packet.entityId) as? EntityArmorStand ?: return

        if (activeDeployables.any { it.entity == entity }) return
        val name = entity.name.noControlCodes
        val deployable = DeployableTypes.entries.firstOrNull { name.startsWith(it.displayName) || it.texture == getSkullValue(entity) } ?: return
        val duration =
            if (deployable.texture == FlareTextures.NONE) (orbRegex.find(name)?.groupValues?.get(2)?.toIntOrNull() ?: return) * 1000
            else deployable.duration
        activeDeployables.add(Deployable(deployable, entity, duration))
        activeDeployables.sortByDescending { it.deployable.priority }
    }

    init {
        onWorldLoad { activeDeployables.clear() }
    }

    enum class DeployableTypes (
        val texture: String, val displayName: String,
        val priority: Int, val duration: Int, val range: Float
    ) {
        Warning (FlareTextures.WARNING_FLARE_TEXTURE, "Warning Flare", 3, 180000, 40f),
        Alert   (FlareTextures.ALERT_FLARE_TEXTURE,   "Alert Flare",   5, 180000, 40f),
        SOS     (FlareTextures.SOS_FLARE_TEXTURE,     "SOS Flare",     7, 180000, 40f),
        Radiant (FlareTextures.NONE,                  "Radiant",       1, 30000,  18f),
        Mana    (FlareTextures.NONE,                  "Mana Flux",     2, 30000,  18f),
        Overflux(FlareTextures.NONE,                  "Overflux",      4, 30000,  18f),
        Plasma  (FlareTextures.NONE,                  "Plasmaflux",    5, 60000,  20f),
    }

    private data object FlareTextures {
        const val WARNING_FLARE_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTY2MjY4Mjg0NTU4NiwKICAicHJvZmlsZUlkIiA6ICIwODFiZTAxZmZlMmU0ODMyODI3MDIwMjBlNmI1M2ExNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJMeXJpY1BsYXRlMjUyNDIiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjJlMmJmNmMxZWMzMzAyNDc5MjdiYTYzNDc5ZTU4NzJhYzY2YjA2OTAzYzg2YzgyYjUyZGFjOWYxYzk3MTQ1OCIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9"
        const val ALERT_FLARE_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTcxOTg1MDQzMTY4MywKICAicHJvZmlsZUlkIiA6ICJmODg2ZDI3YjhjNzU0NjAyODYyYTM1M2NlYmYwZTgwZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJOb2JpbkdaIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzlkMmJmOTg2NDcyMGQ4N2ZkMDZiODRlZmE4MGI3OTVjNDhlZDUzOWIxNjUyM2MzYjFmMTk5MGI0MGMwMDNmNmIiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ=="
        const val SOS_FLARE_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTY2MjY4Mjc3NjUxNiwKICAicHJvZmlsZUlkIiA6ICI4YjgyM2E1YmU0Njk0YjhiOTE0NmE5MWRhMjk4ZTViNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTZXBoaXRpcyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9jMDA2MmNjOThlYmRhNzJhNmE0Yjg5NzgzYWRjZWYyODE1YjQ4M2EwMWQ3M2VhODdiM2RmNzYwNzJhODlkMTNiIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0="
        const val NONE = ""
    }
}