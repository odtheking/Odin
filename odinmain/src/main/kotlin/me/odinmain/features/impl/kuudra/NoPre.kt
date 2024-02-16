package me.odinmain.features.impl.kuudra

import me.odinmain.OdinMain
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.skyblock.KuudraUtils
import me.odinmain.utils.skyblock.partyMessage
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.Vec3
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.cos
import kotlin.math.sin

object NoPre : Module(
    name = "No Pre",
    description = "Alerts the party about the pre spot",
    category = Category.KUUDRA
) {

    @SubscribeEvent
    fun handleArmorStand(event: EntityJoinWorldEvent) {
        if (event.entity is EntityArmorStand || KuudraUtils.phase != 1 || event.entity.name.contains("Lv") || !event.entity.toString().contains("name=Armor Stand")) return
        if (!event.entity.name.contains("SUPPLIES RECEIVED")) return
        val x = event.entity.posX.toInt()
        val z = event.entity.posZ.toInt()

        if (x == -98 && z == -112) KuudraUtils.supplies[0] = false
        if (x == -98 && z == -99) KuudraUtils.supplies[1] = false
        if (x == -110 && z == -106) KuudraUtils.supplies[2] = false
        if (x == -106 && z == -112) KuudraUtils.supplies[3] = false
        if (x == -94 && z == -106) KuudraUtils.supplies[4] = false
        if (x == -106 && z == -99) KuudraUtils.supplies[5] = false
    }

    @SubscribeEvent
    fun cancelArmorStandEvent(event: EntityJoinWorldEvent) {
        if (event is EntityArmorStand && event.toString().noControlCodes.contains("[\"[Lv\"]"))
            mc.theWorld.removeEntity(event)
    }


    private val shop = Vec3(-81.0, 76.0, -143.0)
    private val xCannon = Vec3(-143.0, 76.0, -125.0)
    private val square = Vec3(-143.0, 76.0, -80.0)
    fun handleChatMessage(message: String) {
        when {
            message.contains("[NPC] Elle: Head over to the main platform, I will join you when I get a bite!") -> {
                val player = OdinMain.mc.thePlayer
                KuudraUtils.preSpot = when {
                    player.getDistanceSq(-67.5, 77.0, -122.5) < 15 -> "Triangle"
                    player.getDistanceSq(-142.5, 77.0, -151.0) < 30 -> "X"
                    player.getDistanceSq(-65.5, 76.0, -87.5) < 15 -> "Equals"
                    player.getDistanceSq(-113.5, 77.0, -68.5) < 15 -> "Slash"
                    else -> ""
                }
            }
            message.contains("[NPC] Elle: Not again!") -> {
                val xs = mutableListOf<Double>()
                val zs = mutableListOf<Double>()
                var pre = false
                var second = false
                var msg = ""
                KuudraUtils.giantZombies.forEach { giant ->
                    val yaw = giant.rotationYaw
                    val x = giant.posX + (3.7 * cos((yaw + 130) * (Math.PI / 180)))
                    val z = giant.posZ + (3.7 * sin((yaw + 130) * (Math.PI / 180)))
                    xs.add(x)
                    zs.add(z)
                }
                xs.forEachIndexed { index, supply ->
                    val preLoc = Vec3(supply, 76.0, zs[index])
                    when {
                        preLoc.distanceTo(preLoc) < 18 -> pre = true
                        KuudraUtils.preSpot == "Triangle" && shop.distanceTo(preLoc) < 18 -> second = true
                        KuudraUtils.preSpot == "X" && xCannon.distanceTo(preLoc) < 16 -> second = true
                        KuudraUtils.preSpot == "Slash" && square.distanceTo(preLoc) < 20 -> second = true
                    }
                }
                if (second && pre) return
                if (!pre && KuudraUtils.preSpot.isNotEmpty()) {
                    msg = "No ${KuudraUtils.preSpot}!"
                } else if (!second) {
                    val location = when (KuudraUtils.preSpot) {
                        "Triangle" -> "Shop"
                        "X" -> "X Cannon"
                        "Slash" -> "Square"
                        else -> return
                    }
                    msg = "No $location!"
                }
                partyMessage(msg)
            }
            message.startsWith("Party >") && message.contains(": No")  -> {
                KuudraUtils.missing = message.split("No ")[1].split("!")[0]
            }
        }
    }
}