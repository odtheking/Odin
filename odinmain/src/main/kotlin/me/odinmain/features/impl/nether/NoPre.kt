package me.odinmain.features.impl.nether

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.KuudraUtils.giantZombies
import net.minecraft.util.Vec3

object NoPre : Module(
    name = "Pre-Spot Alert",
    description = "Alerts the party about the state of a pre spot.",
    category = Category.NETHER
) {
    private val showAlert by BooleanSetting("Show Alert", false, description = "Shows an alert when you miss a pre spot.")

    private var preLoc = PreSpot("", Vec3(0.0, 0.0, 0.0))
    var missing = ""

    private data class PreSpot(val name: String, val location: Vec3)
    private val preSpots = listOf(
        PreSpot("Triangle", Vec3(-67.5, 77.0, -122.5)),
        PreSpot("X", Vec3(-142.5, 77.0, -151.0)),
        PreSpot("Equals", Vec3(-65.5, 76.0, -87.5)),
        PreSpot("Slash", Vec3(-113.5, 77.0, -68.5)),

        PreSpot("Shop", Vec3(-81.0, 76.0, -143.0)),
        PreSpot("xCannon", Vec3(-143.0, 76.0, -125.0)),
        PreSpot("Square", Vec3(-143.0, 76.0, -80.0))
    )

    private val partyChatRegex = Regex("^Party > (\\[[^]]*?])? ?(\\w{1,16}): No ?(Triangle|X|Equals|Slash)!\$")

    init {
        onMessage("[NPC] Elle: Head over to the main platform, I will join you when I get a bite!", false) {
            val playerLocation = mc.thePlayer?.positionVector ?: return@onMessage
            when {
                preSpots[0].location.distanceTo(playerLocation) < 15 -> preLoc = preSpots[0]

                preSpots[1].location.distanceTo(playerLocation) < 30 -> preLoc = preSpots[1]

                preSpots[2].location.distanceTo(playerLocation) < 15 -> preLoc = preSpots[2]

                preSpots[3].location.distanceTo(playerLocation) < 15 -> preLoc = preSpots[3]
            }
            modMessage("Pre-spot: ${preLoc.name.ifEmpty { "§cDidn't register your pre-spot because you didn't get there in time." }}")
        }

        onMessage("[NPC] Elle: Not again!", false) {
            var pre = false
            var second = false
            var msg = ""
            giantZombies.forEach { supply ->
                val supplyLoc = Vec3(supply.posX, 76.0, supply.posZ)
                when {
                    preLoc.location.distanceTo(supplyLoc) < 18 -> pre = true
                    preLoc.name == "Triangle" && preSpots[4].location.distanceTo(supplyLoc) < 18 -> second = true
                    preLoc.name == "X" && preSpots[5].location.distanceTo(supplyLoc) < 16 -> second = true
                    preLoc.name == "Slash" && preSpots[6].location.distanceTo(supplyLoc) < 20 -> second = true
                }
            }
            if (second && pre) return@onMessage
            if (!pre && preLoc.name.isNotEmpty()) msg = "No ${preLoc.name}!"
            else if (!second) {
                msg = when (preLoc.name) {
                    "Triangle" -> "No Shop!"
                    "X" -> "No X Cannon!"
                    "Slash" -> "No Square!"
                    else -> return@onMessage
                }
            }
            if (msg.isEmpty()) return@onMessage modMessage("§cYou didn't get to your pre spot in time")
            partyMessage(msg)
            if (showAlert) PlayerUtils.alert(msg, time = 10)
        }

        onMessage(partyChatRegex) {
            missing = partyChatRegex.find(it)?.groupValues?.lastOrNull() ?: return@onMessage
        }

        onWorldLoad { missing = "" }
    }
}