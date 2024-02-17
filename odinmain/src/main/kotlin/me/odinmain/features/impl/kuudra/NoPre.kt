package me.odinmain.features.impl.kuudra

import me.odinmain.OdinMain
import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.skyblock.KuudraUtils
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.skyblock.partyMessage
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.Vec3
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.cos
import kotlin.math.sin

object NoPre : Module(
    name = "Pre-Spot Alert",
    description = "Alerts the party about the pre spot",
    category = Category.KUUDRA
) {
    private val shop = Vec3(-81.0, 76.0, -143.0)
    private val xCannon = Vec3(-143.0, 76.0, -125.0)
    private val square = Vec3(-143.0, 76.0, -80.0)
    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        val message = event.message
        when {
            message.contains("[NPC] Elle: Head over to the main platform, I will join you when I get a bite!") -> {
                val player = mc.thePlayer
                KuudraUtils.preSpot = when {
                    player.getDistanceSq(-67.5, 77.0, -122.5) < 15 -> "Triangle"
                    player.getDistanceSq(-142.5, 77.0, -151.0) < 30 -> "X"
                    player.getDistanceSq(-65.5, 76.0, -87.5) < 15 -> "Equals"
                    player.getDistanceSq(-113.5, 77.0, -68.5) < 15 -> "Slash"
                    else -> ""
                }
                modMessage(KuudraUtils.preSpot)
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