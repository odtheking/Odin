package me.odinmain.features.impl.skyblock

import me.odinmain.events.impl.PostEntityMetadata
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.FlareTextures
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.*
import me.odinmain.utils.skyblock.drawItem
import me.odinmain.utils.skyblock.getSkullValue
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object DeployableTimer : Module(
    name = "Deployable Timer",
    description = "Displays the active deployable and it's time left",
    category = Category.SKYBLOCK
) {
    private val firework = Item.getByNameOrId("minecraft:fireworks")
    private val hud: HudElement by HudSetting("Display", 10f, 10f, 1f, false) {
        if (it) {
            mcText("§l§5SOS Flare", 40, 10, 1.35 ,Color.WHITE, center = false)
            mcText("§e179s", 40, 25, 1.2 ,Color.WHITE, center = false)
            ItemStack(firework).drawItem(x= -10f, y= -4f, scale = 3.5f)
            getMCTextWidth("SOS Flare") + 45f to 52f
        } else {
            val activeDeployable = activeDeployables.firstOrNull { dep -> mc.thePlayer.getDistanceToEntity(dep.entity) <= dep.deployable.range } ?: return@HudSetting 0f to 0f
            val timeLeft = (activeDeployable.timeAdded + activeDeployable.duration - System.currentTimeMillis()) / 1000

            if (timeLeft <= 0 || activeDeployable.entity.isDead) {
                activeDeployables.remove(activeDeployable)
                activeDeployables.sortByDescending { dep -> dep.deployable.priority }
                return@HudSetting 0f to 0f
            }
            mcText(activeDeployable.deployable.displayName, 40, 10, 1.35 ,Color.WHITE, center = false)
            mcText("§e${timeLeft}s", 40, 25, 1.2 ,Color.WHITE, center = false)
            activeDeployable.entity.inventory?.get(4)?.drawItem(x= -10f, y= -4f, scale = 3.5f)
            getMCTextWidth(activeDeployable.deployable.displayName.noControlCodes) + 45f to 52f
        }
    }

    enum class DeployableTypes (
        val texture: String, val displayName: String,
        val priority: Int, val duration: Int, val range: Float
    ) {
        Warning (FlareTextures.WARNING_FLARE_TEXTURE, "§aWarning Flare", 3, 180000, 40f),
        Alert   (FlareTextures.ALERT_FLARE_TEXTURE, "§9Alert Flare",   5, 180000, 40f),
        SOS     (FlareTextures.SOS_FLARE_TEXTURE, "§l§5SOS Flare",   7, 180000, 40f),
        Radiant ("placeholder", "§aRadiant",    1, 30000,  18f),
        Mana    ("placeholder", "§9Mana Flux",  2, 30000, 18f),
        Overflux("placeholder", "§5Overflux",   4, 30000, 18f),
        Plasma  ("placeholder", "§d§lPlasmaflux",   5, 60000,  20f),
    }

    data class Deployable(val deployable: DeployableTypes, val entity: EntityArmorStand, val duration: Int, val timeAdded: Long = System.currentTimeMillis())

    private val activeDeployables = mutableListOf<Deployable>()
    private val orbRegex = Regex("(.+) (\\d+)s")

    @SubscribeEvent
    fun postMetadata(event: PostEntityMetadata) {
        var entity = mc.theWorld?.getEntityByID(event.packet.entityId) as? EntityArmorStand ?: return

        if (activeDeployables.any { it.entity == entity }) return
        val name = entity.name.noControlCodes
        val deployable = DeployableTypes.entries.firstOrNull { name.startsWith(it.displayName.noControlCodes) || it.texture == getSkullValue(entity) } ?: return
        val duration =
            if (deployable.texture == "placeholder") {

                entity = mc.theWorld.getEntitiesWithinAABBExcludingEntity(entity, entity.entityBoundingBox.offset(0.0, -3.0, 0.0)).filterIsInstance<EntityArmorStand>().firstOrNull() ?: return
                (orbRegex.find(name)?.groupValues?.get(2)?.toIntOrNull() ?: return) * 1000
            }
            else deployable.duration

        activeDeployables.add(Deployable(deployable, entity, duration))
        activeDeployables.sortByDescending { it.deployable.priority }
    }

    init {
        onWorldLoad{
            activeDeployables.clear()
        }
    }
}