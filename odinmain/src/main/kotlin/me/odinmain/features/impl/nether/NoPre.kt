package me.odinmain.features.impl.nether

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.KuudraUtils.PreSpot
import me.odinmain.utils.skyblock.KuudraUtils.giantZombies
import net.minecraft.util.Vec3

object NoPre : Module(
    name = "Pre-Spot Alert",
    description = "Alerts the party about the state of a pre spot.",
    category = Category.NETHER
) {
    private val showAlert by BooleanSetting("Show Alert", false, description = "Shows an alert when you miss a pre spot.")
    private val showCratePriority by BooleanSetting("Show Crate Priority", false, description = "Shows the crate priority alert.")
    private val advanced by BooleanSetting("Advanced Mode", false, description = "Enables pro mode for the crate priority alert.").withDependency { showCratePriority }

    private var preSpot = PreSpot.None
    var missing = PreSpot.None

    private val partyChatRegex = Regex("^Party > (\\[[^]]*?])? ?(\\w{1,16}): No ?(Triangle|X|Equals|Slash)!\$")

    init {
        onMessage("[NPC] Elle: Head over to the main platform, I will join you when I get a bite!", false) {
            val playerLocation = mc.thePlayer?.positionVector ?: return@onMessage
            preSpot = when {
                PreSpot.Triangle.location.distanceTo(playerLocation) < 15 -> PreSpot.Triangle
                PreSpot.X.location.distanceTo(playerLocation) < 30 -> PreSpot.X
                PreSpot.Equals.location.distanceTo(playerLocation) < 15 -> PreSpot.Equals
                PreSpot.Slash.location.distanceTo(playerLocation) < 15 -> PreSpot.Slash
                else -> PreSpot.None
            }
            modMessage("Pre-spot: ${if (preSpot == PreSpot.None) "§cDidn't register your pre-spot because you didn't get there in time." else preSpot.name}")
        }

        onMessage("[NPC] Elle: Not again!", false) {
            var pre = false
            var second = false
            var msg = ""
            giantZombies.forEach { supply ->
                val supplyLoc = Vec3(supply.posX, 76.0, supply.posZ)
                when {
                    preSpot.location.distanceTo(supplyLoc) < 18 -> pre = true
                    preSpot == PreSpot.Triangle && PreSpot.Shop.location.distanceTo(supplyLoc) < 18 -> second = true
                    preSpot == PreSpot.X && PreSpot.xCannon.location.distanceTo(supplyLoc) < 16 -> second = true
                    preSpot == PreSpot.Slash && PreSpot.Square.location.distanceTo(supplyLoc) < 20 -> second = true
                }
            }
            if (second && pre) return@onMessage
            if (!pre && preSpot != PreSpot.None) msg = "No ${preSpot.name}!"
            else if (!second) {
                msg = when (preSpot) {
                    PreSpot.Triangle -> "No Shop!"
                    PreSpot.X -> "No X Cannon!"
                    PreSpot.Slash -> "No Square!"
                    else -> return@onMessage
                }
            }
            if (msg.isEmpty()) return@onMessage modMessage("§cYou didn't get to your pre spot in time")
            partyMessage(msg)
            if (showAlert) PlayerUtils.alert(msg, time = 10)
        }

        onMessage(partyChatRegex) {
            missing = PreSpot.valueOf(partyChatRegex.find(it)?.groupValues?.lastOrNull() ?: return@onMessage)
            if (!showCratePriority) return@onMessage
            val cratePriority = cratePriority().ifEmpty { return@onMessage }
            PlayerUtils.alert(cratePriority, time = 15)
            modMessage("Crate Priority: $cratePriority")
        }

        onWorldLoad { missing = PreSpot.None }
    }
    
    fun cratePriority(): String {
        return when {
            // Shop Missing
            missing == PreSpot.Shop -> when (preSpot) {
                PreSpot.Triangle, PreSpot.X -> "Go X Cannon"
                PreSpot.Equals -> if (advanced) "Go X Cannon" else "Go Shop"
                PreSpot.Slash -> "Go Square"
                else -> ""
            }

            // Triangle Missing
            missing == PreSpot.Triangle -> when (preSpot) {
                PreSpot.Triangle -> if (advanced) "Pull Square and X Cannon. Next: collect Shop" else "Pull Square. Next: collect Shop"
                PreSpot.X, PreSpot.Equals -> "Go X Cannon"
                PreSpot.Slash -> "Go Square, place on Triangle"
                else -> ""
            }

            // Equals Missing
            missing == PreSpot.Equals -> when (preSpot) {
                PreSpot.Triangle -> if (advanced) "Go Shop" else "Go X Cannon"
                PreSpot.X -> "Go X Cannon"
                PreSpot.Equals -> if (advanced) "Pull Square and X Cannon. Next: collect Shop" else "Pull Square. Next: collect Shop"
                PreSpot.Slash -> "Go Square, place on Equals"
                else -> ""
            }

            // Slash Missing
            missing == PreSpot.Slash -> when (preSpot) {
                PreSpot.Triangle -> "Go Square, place on Triangle"
                PreSpot.X -> "Go X Cannon"
                PreSpot.Equals -> "Go Square, place on Equals"
                PreSpot.Slash -> if (advanced) "Pull Square and X Cannon. Next: collect Shop" else "Pull Square. Next: collect Shop"
                else -> ""
            }

            // Square Missing
            missing == PreSpot.Square -> when (preSpot) {
                PreSpot.Triangle, PreSpot.Equals -> "Go Shop"
                PreSpot.X -> "Go X Cannon"
                PreSpot.Slash -> "Go X Cannon"
                else -> ""
            }

            // X Cannon Missing
            missing == PreSpot.xCannon -> when (preSpot) {
                PreSpot.Triangle, PreSpot.Equals -> "Go Shop"
                PreSpot.X -> "Go Square"
                PreSpot.Slash -> "Go Square, place on X Cannon"
                else -> ""
            }

            // X Missing
            missing == PreSpot.X -> when (preSpot) {
                PreSpot.Triangle -> "Go X Cannon"
                PreSpot.X -> if (advanced) "Pull Square and X Cannon. Next: collect Shop" else "Pull Square. Next: collect Shop"
                PreSpot.Equals -> if (advanced) "Go Shop" else "Go X Cannon"
                PreSpot.Slash -> "Go Square, place on X"
                else -> ""
            }

            else -> ""
        }
    }
}