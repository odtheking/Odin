package me.odinmain.features.impl.kuudra

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.skyblock.KuudraUtils.giantZombies
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.skyblock.partyMessage
import net.minecraft.util.Vec3
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object NoPre : Module(
    name = "Pre-Spot Alert",
    description = "Alerts the party about the state of a pre spot.",
    category = Category.KUUDRA
) {
    private val shop = Vec3(-81.0, 76.0, -143.0)
    private val xCannon = Vec3(-143.0, 76.0, -125.0)
    private val square = Vec3(-143.0, 76.0, -80.0)
    private val triangle = Vec3(-67.5, 77.0, -122.5)
    private val X = Vec3(-142.5, 77.0, -151.0)
    private val equals = Vec3(-65.5, 76.0, -87.5)
    private val slash = Vec3(-113.5, 77.0, -68.5)
    private var preSpot = ""
    var missing = ""
    private var preLoc = Vec3(0.0, 0.0, 0.0)

    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        val message = event.message
        when {
            message.contains("[NPC] Elle: Head over to the main platform, I will join you when I get a bite!") -> {
                val playerLocation = mc.thePlayer.positionVector
                when {
                    triangle.distanceTo(playerLocation) < 15 -> {
                        preSpot = "Triangle"
                        preLoc = triangle
                    }
                    X.distanceTo(playerLocation) < 30 -> {
                        preSpot = "X"
                        preLoc = X
                    }
                    equals.distanceTo(playerLocation) < 15 -> {
                        preSpot = "Equals"
                        preLoc = equals
                    }
                    slash.distanceTo(playerLocation) < 15 -> {
                        preSpot = "Slash"
                        preLoc = slash
                    }
                }
                modMessage("Pre-spot: $preSpot")
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
                PlayerUtils.alert(msg, time = 30)
            }
            message.startsWith("Party >") && message.contains(": No")  -> {
                missing = message.split("No ")[1].split("!")[0]
            }
        }
    }
}