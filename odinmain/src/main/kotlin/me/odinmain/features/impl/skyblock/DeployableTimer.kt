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
            mcText("§l§5SOS Flare", 40, 15, 1 ,Color.WHITE, center = false)
            mcText("§e179s", 40, 30, 1 ,Color.WHITE, center = false)
            ItemStack(firework).drawItem(x= -10f, y= -4f, scale = 3.5f)
            getMCTextWidth("SOS Flare") + 45f to 52f
        } else {
            val activeDeployable = activeDeployables.firstOrNull { dep -> mc.thePlayer.getDistanceToEntity(dep.entity) <= dep.deployable.range } ?: return@HudSetting 0f to 0f
            val timeLeft = activeDeployable.deployable.duration - activeDeployable.entity.ticksExisted

            if (timeLeft <= 0 || activeDeployable.entity.isDead) {
                activeDeployables.remove(activeDeployable)
                activeDeployables.sortByDescending { dep -> dep.deployable.priority }
                return@HudSetting 0f to 0f
            }
            mcText(activeDeployable.deployable.displayName, 40, 15f, 1 ,Color.WHITE, center = false)
            mcText("§e${timeLeft}s", 40, 30f, 1 ,Color.WHITE, center = false)
            activeDeployable.entity.inventory?.get(4)?.drawItem(x= -10f, y= -4f, scale = 3.5f)
            getMCTextWidth(activeDeployable.deployable.displayName.noControlCodes) + 45f to 52f
        }
    }

    enum class Deployables (
        val texture: String, val displayName: String,
        val priority: Int, val duration: Int, val range: Float
    ) {
        Warning (FlareTextures.warningFlareTexture, "§aWarning Flare", 3, 180000, 40f),
        Alert   (FlareTextures.alertFlareTexture, "§9Alert Flare",   5, 180000, 40f),
        SOS     (FlareTextures.sosFlareTexture, "§l§5SOS Flare",   7, 180000, 40f),
        Radiant ("placeholder", "§aRadiant Orb",    1, 30000,  18f),
        Mana    ("placeholder", "§9Mana Flux Orb",  2, 30000, 18f),
        Overflux("placeholder", "§5Overflux Orb",   4, 30000, 18f),
        Plasma  ("placeholder", "§d§lPlasmaflux",   5, 60000,  20f),
    }

    data class Deployable(val deployable: Deployables, val entity: EntityArmorStand)

    private val activeDeployables = mutableListOf<Deployable>()
    private val orbRegex = Regex("(.+) (\\d+)s")

    @SubscribeEvent
    fun postMetadata(event: PostEntityMetadata) {
        if (mc.theWorld.getEntityByID(event.packet.entityId) !is EntityArmorStand) return

        var entity = mc.theWorld.getEntityByID(event.packet.entityId) as? EntityArmorStand ?: return

        if (activeDeployables.any { it.entity == entity }) return
        val name = entity.name.noControlCodes
        val texture = getSkullValue(entity)
        val deployable = Deployables.entries.firstOrNull { it.texture == texture || name.startsWith(it.displayName.noControlCodes)} ?: return
        if (texture == "placeholder") entity = (mc.theWorld.getEntitiesInAABBexcluding(entity, entity.entityBoundingBox.offset(0.0, -3.0, 0.0)) { it is EntityArmorStand }.firstOrNull() ?: return) as EntityArmorStand

        activeDeployables.add(Deployable(deployable, entity))
        activeDeployables.sortByDescending { it.deployable.priority }
    }

    init {
        onWorldLoad{
            activeDeployables.clear()
        }
    }
}