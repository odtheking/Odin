package me.odinmain.features.impl.kuudra

import me.odinmain.OdinMain
import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.skyblock.KuudraUtils
import me.odinmain.utils.skyblock.PlayerUtils
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
    private var preSpot = ""
    var missing = ""
    private var preLoc = Vec3(0.0, 0.0, 0.0)

    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        val message = event.message
        when {
            message.contains("[NPC] Elle: Head over to the main platform, I will join you when I get a bite!") -> {
                val player = mc.thePlayer
                preSpot = when {
                    player.getDistance(-67.5, 77.0, -122.5) < 15 -> "Triangle"
                    player.getDistance(-142.5, 77.0, -151.0) < 30 -> "X"
                    player.getDistance(-65.5, 76.0, -87.5) < 15 -> "Equals"
                    player.getDistance(-113.5, 77.0, -68.5) < 15 -> "Slash"
                    else -> ""
                }

                preLoc = when (preSpot) {
                    "Triangle" -> Vec3(-67.5, 77.0, -122.5)
                    "X" -> Vec3(-142.5, 77.0, -151.0)
                    "Equals" -> Vec3(-65.5, 76.0, -87.5)
                    "Slash" -> Vec3(-113.5, 77.0, -68.5)
                    else -> Vec3(0.0, 0.0, 0.0)
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
                    val supplyLoc = Vec3(supply, 76.0, zs[index])
                    when {
                        preLoc.distanceTo(supplyLoc) < 18 -> pre = true
                        preSpot == "Triangle" && shop.distanceTo(supplyLoc) < 18 -> second = true
                        preSpot == "X" && xCannon.distanceTo(supplyLoc) < 16 -> second = true
                        preSpot == "Slash" && square.distanceTo(supplyLoc) < 20 -> second = true
                    }
                }
                if (second && pre) return
                if (!pre && preSpot.isNotEmpty()) {
                    msg = "No ${preSpot}!"
                } else if (!second) {
                    val location = when (preSpot) {
                        "Triangle" -> "Shop"
                        "X" -> "X Cannon"
                        "Slash" -> "Square"
                        else -> return
                    }
                    msg = "No $location!"
                }
                partyMessage(msg)
                PlayerUtils.alert(msg)
            }
            message.startsWith("Party >") && message.contains(": No")  -> {
                missing = message.split("No ")[1].split("!")[0]
            }
        }
    }
}