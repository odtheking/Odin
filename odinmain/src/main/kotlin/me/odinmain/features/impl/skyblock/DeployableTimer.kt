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
import me.odinmain.utils.render.RenderUtils.loadBufferedImage
import me.odinmain.utils.render.drawDynamicTexture
import me.odinmain.utils.render.getTextWidth
import me.odinmain.utils.render.text
import me.odinmain.utils.skyblock.getSkullValue
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.max

object DeployableTimer : Module(
    name = "Deployable Timer",
    description = "Shows the time left on deployables.",
    category = Category.SKYBLOCK
) {
    private val firework = DynamicTexture(loadBufferedImage("/assets/odinmain/deployable/firework.png"))
    private val hud: HudElement by HudSetting("Display", 10f, 10f, 1f, false) {
        if (it) {
            text("§l§5SOS Flare", 40f, 15f, Color.WHITE,12f, OdinFont.BOLD)
            text("§e179s", 40f, 32f, Color.WHITE,12f, OdinFont.BOLD)

            drawDynamicTexture(firework, -5f, -2f, 50f, 50f)

            max(getTextWidth("SOS Flare", 12f), getTextWidth("179s", 12f)) + 42f to 48f
        } else {
            val currentMillis = System.currentTimeMillis()
            val d = currentDeployables.firstOrNull { dep -> mc.thePlayer.getDistanceToEntity(dep.entity) <= dep.range }

            if (d == null) {
                resetLines()
                return@HudSetting 0f to 0f
            }
            val timeLeft = (d.timeAdded + d.duration - currentMillis) / 1000
            if (timeLeft <= 0 || d.entity.isDead) {
                currentDeployables.remove(d)
                currentDeployables.sortByDescending { dep -> dep.priority }
                resetLines()
                return@HudSetting 0f to 0f
            }
            toRender = RenderableDeployable(d.renderName, "§e${timeLeft}s", d.image)
            text(toRender.name, 40f, 15f, Color.WHITE,12f, OdinFont.BOLD)
            text(toRender.timeLeft, 40f, 32f, Color.WHITE,12f, OdinFont.BOLD)
            val isFlare = toRender.name.contains("Flare")
            drawDynamicTexture(toRender.image, if (isFlare) -5f else 0f, if (isFlare) -2f else 5f, 35f, 35f)
            max(getTextWidth(toRender.name.noControlCodes, 12f), getTextWidth(toRender.timeLeft.noControlCodes, 12f)) + 42f to 48f
        }
        0f to 0f
    }

    private enum class Deployables (
        val texture: String, val displayName: String, val renderName: String, val priority: Int, val duration: Int, val dynamicTexture: DynamicTexture, val range: Float
    ) {
        Warning (FlareTextures.warningFlareTexture, "Warning Flare", "§aWarning Flare", 3, 180000, firework, 40f),
        Alert   (FlareTextures.alertFlareTexture,   "Alert Flare",   "§9Alert Flare",   5, 180000, firework, 40f),
        SOS     (FlareTextures.sosFlareTexture,     "SOS Flare",     "§l§5SOS Flare",   7, 180000, firework, 40f),
        Radiant ("placeholder", "Radiant",  "§aRadiant Orb",    1, 30000, DynamicTexture(loadBufferedImage("/assets/odinmain/deployable/RADIANTPOWERORB.png")),  18f),
        Mana    ("placeholder", "Mana" ,    "§9Mana Flux Orb",  2, 30000, DynamicTexture(loadBufferedImage("/assets/odinmain/deployable/MANAFLUXPOWERORB.png")), 18f),
        Overflux("placeholder", "Overflux", "§5Overflux Orb",   4, 30000, DynamicTexture(loadBufferedImage("/assets/odinmain/deployable/OVERFLUXPOWERORB.png")), 18f),
        Plasma  ("placeholder", "Plasma",   "§dPlasmaflux",     5, 60000, DynamicTexture(loadBufferedImage("/assets/odinmain/deployable/PLASMAPOWERORB.png")),   20f),
    }

    class Deployable(val priority: Int, val duration: Int, val entity: EntityArmorStand, val renderName: String, val image: DynamicTexture, val range: Float, val timeAdded: Long = System.currentTimeMillis())
    private data class RenderableDeployable(val name: String, val timeLeft: String, val image: DynamicTexture)

    private val currentDeployables = mutableListOf<Deployable>()
    private val orbRegex = Regex("(.+) (\\d+)s")
    private var toRender = RenderableDeployable("", "", firework)

    @SubscribeEvent
    fun postMetadata(event: PostEntityMetadata) {
        if (mc.theWorld.getEntityByID(event.packet.entityId) !is EntityArmorStand) return

        val entity = mc.theWorld.getEntityByID(event.packet.entityId) as EntityArmorStand

        if (currentDeployables.any { it.entity == entity }) return
        val name = entity.name.noControlCodes
        val texture = getSkullValue(entity)
        val deployable = Deployables.entries.firstOrNull { it.texture == texture || name.startsWith(it.displayName)} ?: return
        val duration =
            if (deployable.texture == "placeholder")
                    (orbRegex.find(name)?.groupValues?.get(2)?.toInt() ?: return) * 1000
            else
                deployable.duration

        currentDeployables.add(Deployable(deployable.priority, duration, entity, deployable.renderName, deployable.dynamicTexture, deployable.range))
        currentDeployables.sortByDescending { it.priority }
        resetLines()
    }

    init {
        onWorldLoad{
            currentDeployables.clear()
            resetLines()
        }
    }

    private fun resetLines() {
        toRender = RenderableDeployable("", "", firework)
    }
}