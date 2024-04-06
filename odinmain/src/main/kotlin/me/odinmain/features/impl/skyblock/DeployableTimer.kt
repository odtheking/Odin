package me.odinmain.features.impl.skyblock

import me.odinmain.events.impl.PostEntityMetadata
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.font.OdinFont
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.FlareTextures
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.getTextWidth
import me.odinmain.utils.render.text
import me.odinmain.utils.skyblock.drawItem
import me.odinmain.utils.skyblock.getSkullValue
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object DeployableTimer : Module(
    name = "Deployable Timer",
    description = "Shows the time left on deployables.",
    category = Category.SKYBLOCK
) {
    private val firework = Item.getByNameOrId("minecraft:fireworks")
    private val hud: HudElement by HudSetting("Display", 10f, 10f, 1f, false) {
        if (it) {
            text("§l§5SOS Flare", 45f, 17f, Color.WHITE,12f, OdinFont.BOLD)
            text("§e179s", 45f, 35f, Color.WHITE,12f, OdinFont.BOLD)
            ItemStack(firework).drawItem(x= -12f, y= -8f, scale = 4f)
            getTextWidth("SOS Flare", 12f) + 45f to 52f
        } else {
            val d = currentDeployables.firstOrNull { dep -> mc.thePlayer.getDistanceToEntity(dep.entity) <= dep.range } ?: return@HudSetting 0f to 0f

            val timeLeft = (d.timeAdded + d.duration - System.currentTimeMillis()) / 1000
            if (timeLeft <= 0 || d.entity.isDead) {
                currentDeployables.remove(d)
                currentDeployables.sortByDescending { dep -> dep.priority }
                return@HudSetting 0f to 0f
            }
            text(d.renderName, 45f, 17f, Color.WHITE,12f, OdinFont.BOLD)
            text("§e${timeLeft}s", 45f, 35f, Color.WHITE,12f, OdinFont.BOLD)
            d.entity.inventory?.get(4)?.drawItem(x= -12f, y= -8f, scale = 4f)
            getTextWidth(d.renderName.noControlCodes, 12f) + 45f to 52f
        }
    }

    private enum class Deployables (
        val texture: String, val displayName: String, val renderName: String,
        val priority: Int, val duration: Int, val range: Float
    ) {
        Warning (FlareTextures.warningFlareTexture, "Warning Flare", "§aWarning Flare", 3, 180000, 40f),
        Alert   (FlareTextures.alertFlareTexture,   "Alert Flare",   "§9Alert Flare",   5, 180000, 40f),
        SOS     (FlareTextures.sosFlareTexture,     "SOS Flare",     "§l§5SOS Flare",   7, 180000, 40f),
        Radiant ("placeholder", "Radiant",  "§aRadiant Orb",    1, 30000,  18f),
        Mana    ("placeholder", "Mana" ,    "§9Mana Flux Orb",  2, 30000, 18f),
        Overflux("placeholder", "Overflux", "§5Overflux Orb",   4, 30000, 18f),
        Plasma  ("placeholder", "Plasma",   "§d§lPlasmaflux",   5, 60000,  20f),
    }

    data class Deployable(val priority: Int, val duration: Int, val entity: EntityArmorStand, val renderName: String, val range: Float, val timeAdded: Long = System.currentTimeMillis())

    private val currentDeployables = mutableListOf<Deployable>()
    private val orbRegex = Regex("(.+) (\\d+)s")

    @SubscribeEvent
    fun postMetadata(event: PostEntityMetadata) {
        if (mc.theWorld.getEntityByID(event.packet.entityId) !is EntityArmorStand) return

        var entity = mc.theWorld.getEntityByID(event.packet.entityId) as EntityArmorStand

        if (currentDeployables.any { it.entity == entity }) return
        val name = entity.name.noControlCodes
        val texture = getSkullValue(entity)
        val deployable = Deployables.entries.firstOrNull { it.texture == texture || name.startsWith(it.displayName)} ?: return
        val duration =
            if (deployable.texture == "placeholder") {
                entity = (mc.theWorld.getEntitiesInAABBexcluding(entity, entity.entityBoundingBox.offset(0.0, -3.0, 0.0)) { it is EntityArmorStand }.firstOrNull() ?: return) as EntityArmorStand
                (orbRegex.find(name)?.groupValues?.get(2)?.toInt() ?: return) * 1000
            }
            else
                deployable.duration

        currentDeployables.add(Deployable(deployable.priority, duration, entity, deployable.renderName, deployable.range))
        currentDeployables.sortByDescending { it.priority }
    }

    init {
        onWorldLoad{
            currentDeployables.clear()
        }
    }
}