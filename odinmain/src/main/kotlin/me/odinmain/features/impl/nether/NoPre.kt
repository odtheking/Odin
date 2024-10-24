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
            missing == PreSpot.Shop && (preSpot == PreSpot.Triangle || preSpot == PreSpot.X) -> "Go X Cannon"
            missing == PreSpot.Shop && (preSpot == PreSpot.Equals || preSpot == PreSpot.Slash) -> "Go Square"

            // Triangle Missing
            missing == PreSpot.Triangle && preSpot == PreSpot.Triangle -> if (advanced) "Pull Square and X Cannon. Next: collect Shop" else "Pull Square. Next: collect Shop"
            missing == PreSpot.Triangle && preSpot == PreSpot.X -> "Go X Cannon"
            missing == PreSpot.Triangle && preSpot == PreSpot.Equals -> if (advanced) "Go Shop" else "Go X Cannon"
            missing == PreSpot.Triangle && preSpot == PreSpot.Slash -> "Go Square"

            // Equals Missing
            missing == PreSpot.Equals && preSpot == PreSpot.Triangle -> if (advanced) "Go Shop" else "Go X Cannon"
            missing == PreSpot.Equals && preSpot == PreSpot.X -> "Go X Cannon"
            missing == PreSpot.Equals && preSpot == PreSpot.Equals -> if (advanced) "Pull Square and X Cannon. Next: collect Shop" else "Pull Square. Next: collect Shop"
            missing == PreSpot.Equals && preSpot == PreSpot.Slash -> "Go Square"

            // Slash Missing
            missing == PreSpot.Slash && preSpot == PreSpot.Triangle -> "Go Square"
            missing == PreSpot.Slash && preSpot == PreSpot.X -> "Go X Cannon"
            missing == PreSpot.Slash && preSpot == PreSpot.Equals -> if (advanced) "Go Shop" else "Go X Cannon"
            missing == PreSpot.Slash && preSpot == PreSpot.Slash -> if (advanced) "Pull Square and X Cannon. Next: collect Shop" else "Pull Square. Next: collect Shop"

            // Square Missing
            missing == PreSpot.Square && (preSpot == PreSpot.Triangle || preSpot == PreSpot.Equals) -> "Go Shop"
            missing == PreSpot.Square && (preSpot == PreSpot.X || preSpot == PreSpot.Slash) -> "Go X Cannon"

            // X Cannon Missing
            missing == PreSpot.xCannon && (preSpot == PreSpot.Triangle || preSpot == PreSpot.Equals) -> "Go Shop"
            missing == PreSpot.xCannon && (preSpot == PreSpot.X || preSpot == PreSpot.Slash) -> "Go Square"

            // X Missing
            missing == PreSpot.X && preSpot == PreSpot.Triangle -> "Go X Cannon"
            missing == PreSpot.X && preSpot == PreSpot.X -> if (advanced) "Pull Square and X Cannon. Next: collect Shop" else "Pull Square. Next: collect Shop"
            missing == PreSpot.X && preSpot == PreSpot.Equals -> if (advanced) "Go Shop" else "X Cannon"
            missing == PreSpot.X && preSpot == PreSpot.Slash -> "Go Square"

            else -> ""
        }
    }
}