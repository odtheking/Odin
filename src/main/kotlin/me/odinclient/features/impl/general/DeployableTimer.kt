package me.odinclient.features.impl.general

import cc.polyfrost.oneconfig.libs.universal.UMatrixStack
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.HudSetting
import me.odinclient.ui.hud.TextHud
import me.odinclient.utils.Utils.noControlCodes
import me.odinclient.utils.render.world.RenderUtils
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object DeployableTimer : Module(
    name = "Deployable Timer",
    description = "Shows the time left on deployables",
    category = Category.GENERAL
) {
    private enum class Deployables (
        val texture: String,
        val displayName: String,
        val renderName: String,
        val priority: Int,
        val duration: Int,
        val img: ResourceLocation,
        val range: Float
    )  {
        Warning(
            "ewogICJ0aW1lc3RhbXAiIDogMTY0NjY4NzMwNjIyMywKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjJlMmJmNmMxZWMzMzAyNDc5MjdiYTYzNDc5ZTU4NzJhYzY2YjA2OTAzYzg2YzgyYjUyZGFjOWYxYzk3MTQ1OCIKICAgIH0KICB9Cn0=",
            "Warning Flare",
            "§aWarning Flare",
            3,
            180000,
            ResourceLocation("OdinClient", "firework.png"),
            20f
        ),

        Alert(
            "ewogICJ0aW1lc3RhbXAiIDogMTY0NjY4NzMyNjQzMiwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWQyYmY5ODY0NzIwZDg3ZmQwNmI4NGVmYTgwYjc5NWM0OGVkNTM5YjE2NTIzYzNiMWYxOTkwYjQwYzAwM2Y2YiIKICAgIH0KICB9Cn0=",
            "Alert Flare",
            "§9Alert Flare",
            5,
            180000,
            ResourceLocation("OdinClient", "firework.png"),
            20f
        ),

        SOS(
            "ewogICJ0aW1lc3RhbXAiIDogMTY0NjY4NzM0NzQ4OSwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzAwNjJjYzk4ZWJkYTcyYTZhNGI4OTc4M2FkY2VmMjgxNWI0ODNhMDFkNzNlYTg3YjNkZjc2MDcyYTg5ZDEzYiIKICAgIH0KICB9Cn0=",
            "SOS Flare",
            "§5§lSOS Flare",
            7,
            180000,
            ResourceLocation("OdinClient", "firework.png"),
            20f
        ),

        Radiant(
            "RADIANTPLACEHOLDERTEXTURE",
            "Radiant",
            "§aRadiant Orb",
            1,
            30000,
            ResourceLocation("OdinClient", "RADIANTPOWERORB.png"),
            20f
        ),

        Mana(
            "MANAFLUXPLACEHOLDERTEXTURE",
            "Mana" ,
            "§9Mana Flux Orb",
            2,
            30000,
            ResourceLocation("OdinClient", "MANAFLUXPOWERORB.png"),
            20f
        ),

        Overflux(
            "OVERFLUXPLACEHOLDERTEXTURE",
            "Overflux",
            "§5Overflux Orb",
            4,
            30000,
            ResourceLocation("OdinClient", "OVERFLUXPOWERORB.png"),
            20f
        ),

        Plasma(
            "PLASMAFLUXPLACEHOLDERTEXTURE",
            "Plasma",
            "§d§lPlasmaflux",
            5,
            60000,
            ResourceLocation("OdinClient", "PLASMAPOWERORB.png"),
            20f
        ),
    }

    class Deployable(
        val priority: Int,
        val duration: Int,
        val entity: EntityArmorStand,
        val renderName: String,
        val img: ResourceLocation,
        val range: Float,
        val timeAdded: Long = System.currentTimeMillis()
    )

    private val hud: Boolean by HudSetting("Deployable Hud", DeployableHud)

    private val currentDeployables = mutableListOf<Deployable>()
    private val orbRegex = Regex("(.+) (\\d+)s")
    var lines: Triple<String?, String?, ResourceLocation?> = Triple(null, null, null)

    @OptIn(DelicateCoroutinesApi::class, DelicateCoroutinesApi::class)
    @SubscribeEvent
    fun onEntityJoinWorld(event: EntityJoinWorldEvent) {
        if (event.entity !is EntityArmorStand) return
        GlobalScope.launch {
            delay(100)
            val armorStand = event.entity as EntityArmorStand
            val name = armorStand.name.noControlCodes
            val texture =
                armorStand.inventory
                ?.get(4)
                ?.tagCompound
                ?.getCompoundTag("SkullOwner")
                ?.getCompoundTag("Properties")
                ?.getTagList("textures", 10)
                ?.getCompoundTagAt(0)
                ?.getString("Value")

            if (Deployables.values().any { it.texture == texture }) {
                val flare = Deployables.values().first { it.texture == texture }
                currentDeployables.add(Deployable(flare.priority, flare.duration, armorStand, flare.renderName, flare.img, flare.range))
                currentDeployables.sortByDescending { it.priority }
                resetLines()
            } else if (Deployables.values().any { name.startsWith(it.displayName)}) {
                val orb = Deployables.values().first { name.startsWith(it.displayName)}
                val time = orbRegex.find(name)?.groupValues?.get(2)?.toInt() ?: return@launch
                currentDeployables.add(Deployable(orb.priority, time * 1000, armorStand, orb.renderName, orb.img, orb.range))
                currentDeployables.sortByDescending { it.priority }
                resetLines()
            }
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        currentDeployables.clear()
        resetLines()
    }

    @SubscribeEvent
    fun onRenderOverlay(event: RenderGameOverlayEvent) {
        if (/*!config.deployableHud.isEnabled */ event.type != RenderGameOverlayEvent.ElementType.TEXT || currentDeployables.size == 0) return
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
        lines = Triple(d.renderName, "§e${timeLeft}s", d.img)
    }

    private fun resetLines() {
        lines = Triple(null, null, null)
    }

    object DeployableHud: TextHud(0f, 0f) {
        private val firework: ResourceLocation = ResourceLocation("odinclient", "firework.png")

        override fun draw(example: Boolean): Pair<Float, Float> {
            lines = getLines(example)
            if (lines.isEmpty()) return Pair(0f, 0f)

            var yOffset = 0f
            var width = 0f
            lines.forEach { line ->
                width = width.coerceAtLeast(mc.fontRendererObj.getStringWidth(line).toFloat())
                mc.fontRendererObj.drawStringWithShadow(line, 0f, yOffset, 0xffffff)
                yOffset += 12f
            }

            var renderSize = 0
            if (example) {
                renderSize = RenderUtils.renderImage(firework, -3.0 - scale * 20, -2.0, 50f)
            } else if (DeployableTimer.lines.third != null) {
                renderSize = RenderUtils.renderImage(DeployableTimer.lines.third!!, x.toDouble() - scale * 25, y.toDouble(), 20 * scale)
            }

            width += renderSize
            yOffset = yOffset.coerceAtLeast(renderSize.toFloat())

            return Pair(width, yOffset)
        }

        override fun getLines(example: Boolean): MutableList<String> {
            return if (example) {
                mutableListOf("§5§lSOS Flare", "§e179s")
            } else if (DeployableTimer.lines.first != null) {
                mutableListOf(DeployableTimer.lines.first!!, DeployableTimer.lines.second!!)
            } else mutableListOf()
        }
    }
}
