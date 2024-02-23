package me.odinmain.features.impl.kuudra

import me.odinmain.OdinMain
import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.skyblock.KuudraUtils
import me.odinmain.utils.skyblock.KuudraUtils.giantZombies
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
    description = "Alerts the party about the state of a pre spot",
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
                when {
                    player.getDistance(-67.5, 77.0, -122.5) < 15 -> {
                        preSpot = "Triangle"
                        preLoc = Vec3(-67.5, 77.0, -122.5)
                    }
                    player.getDistance(-142.5, 77.0, -151.0) < 15 -> {
                        preSpot = "X"
                        preLoc = Vec3(-142.5, 77.0, -151.0)
                    }
                    player.getDistance(-65.5, 76.0, -87.5) < 15 -> {
                        preSpot = "Equals"
                        preLoc = Vec3(-65.5, 76.0, -87.5)
                    }
                    player.getDistance(-113.5, 77.0, -68.5) < 15 -> {
                        preSpot = "Slash"
                        preLoc = Vec3(-113.5, 77.0, -68.5)
                    }
                }
            }
            message.contains("[NPC] Elle: Not again!") -> {
                var pre = false
                var second = false
                var msg = ""
                giantZombies.forEach { supply ->
                    val supplyLoc = Vec3(supply.posX, 76.0, supply.posZ)
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