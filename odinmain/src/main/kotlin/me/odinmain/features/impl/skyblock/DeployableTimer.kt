package me.odinmain.features.impl.skyblock

import me.odinmain.events.impl.PostEntityMetadata
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.gui.nvg.Fonts
import me.odinmain.utils.render.gui.nvg.drawBufferedImage
import me.odinmain.utils.render.gui.nvg.getTextWidth
import me.odinmain.utils.render.gui.nvg.textWithControlCodes
import me.odinmain.utils.skyblock.getSkullValue
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.max

object DeployableTimer : Module(
    name = "Deployable Timer",
    description = "Shows the time left on deployables.",
    category = Category.SKYBLOCK
) {
    private val hud: HudElement by HudSetting("Display", 10f, 10f, 1f, false) {
        if (it) {
            textWithControlCodes("§5SOS Flare", 40f, 15f, 16f, Fonts.MEDIUM)
            textWithControlCodes("§e179s", 40f, 32f, 16f, Fonts.MEDIUM)

            drawBufferedImage(
                Deployables.SOS.imgPath, -5f, -2f, 50f, 50f
            )

            max(
                getTextWidth("SOS Flare", 16f, Fonts.MEDIUM),
                getTextWidth("179s", 16f, Fonts.MEDIUM)
            ) + 42f to 48f
        } else if (toRender.name != "") {
            textWithControlCodes(toRender.name, 40f, 15f, 16f, Fonts.MEDIUM)
            textWithControlCodes(toRender.timeLeft, 40f, 32f, 16f, Fonts.MEDIUM)

            if (toRender.name.contains("Flare", true)) drawBufferedImage(toRender.imgPath, -5f, -2f, 50f, 50f)
            else drawBufferedImage(toRender.imgPath, 0f, 5f, 35f, 35f)

            max(
                getTextWidth(toRender.name.noControlCodes, 16f, Fonts.MEDIUM),
                getTextWidth(toRender.timeLeft.noControlCodes, 16f, Fonts.MEDIUM)
            ) + 42f to 48f
        } else 0f to 0f
    }

    private enum class Deployables (val texture: String, val displayName: String, val renderName: String, val priority: Int, val duration: Int, val imgPath: String, val range: Float)  {
        Warning("ewogICJ0aW1lc3RhbXAiIDogMTY0NjY4NzMwNjIyMywKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjJlMmJmNmMxZWMzMzAyNDc5MjdiYTYzNDc5ZTU4NzJhYzY2YjA2OTAzYzg2YzgyYjUyZGFjOWYxYzk3MTQ1OCIKICAgIH0KICB9Cn0=", "Warning Flare", "§aWarning Flare", 3, 180000, "/assets/deployable/firework.png", 40f),

        Alert("ewogICJ0aW1lc3RhbXAiIDogMTY0NjY4NzMyNjQzMiwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWQyYmY5ODY0NzIwZDg3ZmQwNmI4NGVmYTgwYjc5NWM0OGVkNTM5YjE2NTIzYzNiMWYxOTkwYjQwYzAwM2Y2YiIKICAgIH0KICB9Cn0=", "Alert Flare", "§9Alert Flare", 5, 180000, "/assets/deployable/firework.png", 40f),

        SOS("ewogICJ0aW1lc3RhbXAiIDogMTY0NjY4NzM0NzQ4OSwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzAwNjJjYzk4ZWJkYTcyYTZhNGI4OTc4M2FkY2VmMjgxNWI0ODNhMDFkNzNlYTg3YjNkZjc2MDcyYTg5ZDEzYiIKICAgIH0KICB9Cn0=", "SOS Flare", "§5SOS Flare", 7, 180000, "/assets/deployable/firework.png", 40f),

        Radiant("RADIANTPLACEHOLDERTEXTURE", "Radiant", "§aRadiant Orb", 1, 30000, "/assets/deployable/RADIANTPOWERORB.png", 18f),

        Mana("MANAFLUXPLACEHOLDERTEXTURE", "Mana" , "§9Mana Flux Orb", 2, 30000, "/assets/deployable/MANAFLUXPOWERORB.png", 18f),

        Overflux("OVERFLUXPLACEHOLDERTEXTURE", "Overflux", "§5Overflux Orb", 4, 30000, "/assets/deployable/OVERFLUXPOWERORB.png", 18f),

        Plasma("PLASMAFLUXPLACEHOLDERTEXTURE", "Plasma", "§dPlasmaflux", 5, 60000, "/assets/deployable/PLASMAPOWERORB.png", 20f),
    }

    class Deployable(val priority: Int, val duration: Int, val entity: EntityArmorStand, val renderName: String, val imgPath: String, val range: Float, val timeAdded: Long = System.currentTimeMillis())
    private data class RenderableDeployable(val name: String, val timeLeft: String, val imgPath: String)

    private val currentDeployables = mutableListOf<Deployable>()
    private val orbRegex = Regex("(.+) (\\d+)s")

    private var toRender = RenderableDeployable("", "", "")

    @SubscribeEvent
    fun postMetadata(event: PostEntityMetadata) {
        if (mc.theWorld.getEntityByID(event.packet.entityId) !is EntityArmorStand) return

        val entity = mc.theWorld.getEntityByID(event.packet.entityId) as EntityArmorStand
        if (currentDeployables.any { it.entity == entity }) return
        val name = entity.name.noControlCodes
        val texture = getSkullValue(entity)

        if (Deployables.entries.any { it.texture == texture }) {
            val flare = Deployables.entries.first { it.texture == texture }
            currentDeployables.add(Deployable(flare.priority, flare.duration, entity, flare.renderName, flare.imgPath, flare.range))
            currentDeployables.sortByDescending { it.priority }
            resetLines()
        } else if (Deployables.entries.any { name.startsWith(it.displayName)}) {
            val orb = Deployables.entries.first { name.startsWith(it.displayName)}
            val time = orbRegex.find(name)?.groupValues?.get(2)?.toInt() ?: return
            currentDeployables.add(Deployable(orb.priority, time * 1000, entity, orb.renderName, orb.imgPath, orb.range))
            currentDeployables.sortByDescending { it.priority }
            resetLines()
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        currentDeployables.clear()
        resetLines()
    }

    @SubscribeEvent
    fun onRenderOverlay(event: RenderGameOverlayEvent) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT || currentDeployables.size == 0) return
        val currentMillis = System.currentTimeMillis()
        val d = currentDeployables.firstOrNull { mc.thePlayer.getDistanceToEntity(it.entity) <= it.range }

        if (d == null) {
            resetLines()
            return
        }
        val timeLeft = (d.timeAdded + d.duration - currentMillis) / 1000
        if (timeLeft <= 0 || d.entity.isDead) {
            currentDeployables.remove(d)
            currentDeployables.sortByDescending { it.priority }
            resetLines()
            return
        }
        toRender = RenderableDeployable(d.renderName, "§e${timeLeft}s", d.imgPath)
    }

    private fun resetLines() {
        toRender = RenderableDeployable("", "", "")
    }
}