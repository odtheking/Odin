package me.odinmain.features.impl.nether

import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.skyblock.KuudraUtils.SupplyPickUpSpot
import me.odinmain.utils.skyblock.KuudraUtils.giantZombies
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.skyblock.partyMessage
import net.minecraft.util.Vec3

object NoPre : Module(
    name = "Pre-Spot Alert",
    desc = "Alerts the party if a pre spot is missing."
) {
    private val showCratePriority by BooleanSetting("Show Crate Priority", false, desc = "Shows the crate priority alert.")
    private val cratePriorityTitleTime by NumberSetting("Title Time", 30, 1, 60, desc = "The time the crate priority alert will be displayed for.").withDependency { showCratePriority }
    private val advanced by BooleanSetting("Advanced Mode", false, desc = "Enables pro mode for the crate priority alert.").withDependency { showCratePriority }

    private var preSpot = SupplyPickUpSpot.None
    var missing = SupplyPickUpSpot.None
    var prio = SupplyPickUpSpot.None

    init {
        onMessage(Regex("\\[NPC] Elle: Head over to the main platform, I will join you when I get a bite!")) {
            val playerLocation = mc.thePlayer?.positionVector ?: return@onMessage
            preSpot = when {
                SupplyPickUpSpot.Triangle.location.distanceTo(playerLocation) < 15 -> SupplyPickUpSpot.Triangle
                SupplyPickUpSpot.X.location.distanceTo(playerLocation) < 30 -> SupplyPickUpSpot.X
                SupplyPickUpSpot.Equals.location.distanceTo(playerLocation) < 15 -> SupplyPickUpSpot.Equals
                SupplyPickUpSpot.Slash.location.distanceTo(playerLocation) < 15 -> SupplyPickUpSpot.Slash
                else -> SupplyPickUpSpot.None
            }
            modMessage(if (preSpot == SupplyPickUpSpot.None) "§cDidn't register your pre-spot because you didn't get there in time." else "Pre-spot: ${preSpot.name}")
        }

        onMessage(Regex("\\[NPC] Elle: Not again!")) {
            if (preSpot == SupplyPickUpSpot.None) return@onMessage
            var pre = false
            var second = false
            var msg = ""
            giantZombies.forEach { supply ->
                val supplyLoc = Vec3(supply.posX, 76.0, supply.posZ)
                when {
                    preSpot.location.distanceTo(supplyLoc) < 18 -> pre = true
                    preSpot == SupplyPickUpSpot.Triangle && SupplyPickUpSpot.Shop.location.distanceTo(supplyLoc) < 18 -> second = true
                    preSpot == SupplyPickUpSpot.X && SupplyPickUpSpot.xCannon.location.distanceTo(supplyLoc) < 16 -> second = true
                    preSpot == SupplyPickUpSpot.Slash && SupplyPickUpSpot.Square.location.distanceTo(supplyLoc) < 20 -> second = true
                }
            }
            if (second && pre) return@onMessage
            if (!pre && preSpot != SupplyPickUpSpot.None) msg = "No ${preSpot.name}!"
            else if (!second) {
                msg = when (preSpot) {
                    SupplyPickUpSpot.Triangle -> "No Shop!"
                    SupplyPickUpSpot.X -> "No xCannon!"
                    SupplyPickUpSpot.Slash -> "No Square!"
                    else -> return@onMessage
                }
            }
            if (msg.isEmpty()) return@onMessage
            partyMessage(msg)
        }

        onMessage(Regex("^Party > (\\[[^]]*?])? ?(\\w{1,16}): No ?(Triangle|X|Equals|Slash|xCannon|Square|Shop)!\$")) {
            missing = SupplyPickUpSpot.valueOf(it.groupValues.lastOrNull() ?: return@onMessage)
            if (!showCratePriority) return@onMessage
            val (prio, cratePriority) = cratePriority(missing)
            cratePriority.ifEmpty { return@onMessage }
            PlayerUtils.alert(cratePriority, time = cratePriorityTitleTime)
            modMessage("Crate Priority: $cratePriority")
        }

        onWorldLoad {
            preSpot = SupplyPickUpSpot.None
            missing = SupplyPickUpSpot.None
        }
    }
    
    private fun cratePriority(missing: SupplyPickUpSpot): Pair<SupplyPickUpSpot, String> {
        return when (missing) {
            // Shop Missing
            SupplyPickUpSpot.Shop -> when (preSpot) {
                SupplyPickUpSpot.Triangle, SupplyPickUpSpot.X -> SupplyPickUpSpot.xCannon to "Go X Cannon"
                SupplyPickUpSpot.Equals, SupplyPickUpSpot.Slash -> SupplyPickUpSpot.Square to "Go Square, place on Shop"
                else -> SupplyPickUpSpot.None to ""
            }

            // Triangle Missing
            SupplyPickUpSpot.Triangle -> when (preSpot) {
                SupplyPickUpSpot.Triangle -> SupplyPickUpSpot.None to if (advanced) "Pull Square and X Cannon. Next: collect Shop" else "Pull Square. Next: collect Shop"
                SupplyPickUpSpot.X -> SupplyPickUpSpot.xCannon to "Go X Cannon"
                SupplyPickUpSpot.Equals -> if (advanced) SupplyPickUpSpot.Shop to "Go Shop" else SupplyPickUpSpot.xCannon to "Go X Cannon"
                SupplyPickUpSpot.Slash -> SupplyPickUpSpot.Square to "Go Square, place on Triangle"
                else -> SupplyPickUpSpot.None to ""
            }

            // Equals Missing
            SupplyPickUpSpot.Equals -> when (preSpot) {
                SupplyPickUpSpot.Triangle -> if (advanced) SupplyPickUpSpot.Shop to "Go Shop" else SupplyPickUpSpot.xCannon to "Go X Cannon"
                SupplyPickUpSpot.X -> SupplyPickUpSpot.xCannon to "Go X Cannon"
                SupplyPickUpSpot.Equals -> SupplyPickUpSpot.Shop to if (advanced) "Pull Square and X Cannon. Next: collect Shop" else "Pull Square. Next: collect Shop"
                SupplyPickUpSpot.Slash -> SupplyPickUpSpot.Square to "Go Square, place on Equals"
                else -> SupplyPickUpSpot.None to ""
            }

            // Slash Missing
            SupplyPickUpSpot.Slash -> when (preSpot) {
                SupplyPickUpSpot.Triangle -> SupplyPickUpSpot.Square to "Go Square, place on Slash"
                SupplyPickUpSpot.X -> SupplyPickUpSpot.xCannon to "Go X Cannon"
                SupplyPickUpSpot.Equals -> if (advanced) SupplyPickUpSpot.Shop to "Go Shop" else SupplyPickUpSpot.xCannon to "Go X Cannon"
                SupplyPickUpSpot.Slash -> SupplyPickUpSpot.Shop to if (advanced) "Pull Square and X Cannon. Next: collect Shop" else "Pull Square. Next: collect Shop"
                else -> SupplyPickUpSpot.None to ""
            }

            // Square Missing
            SupplyPickUpSpot.Square -> when (preSpot) {
                SupplyPickUpSpot.Triangle, SupplyPickUpSpot.Equals -> SupplyPickUpSpot.Shop to "Go Shop"
                SupplyPickUpSpot.X, SupplyPickUpSpot.Slash -> SupplyPickUpSpot.xCannon to "Go X Cannon"
                else -> SupplyPickUpSpot.None to ""
            }

            // X Cannon Missing
            SupplyPickUpSpot.xCannon -> when (preSpot) {
                SupplyPickUpSpot.Triangle, SupplyPickUpSpot.Equals -> SupplyPickUpSpot.Shop to "Go Shop"
                SupplyPickUpSpot.Slash, SupplyPickUpSpot.X -> SupplyPickUpSpot.Square to "Go Square, place on X Cannon"
                else -> SupplyPickUpSpot.None to ""
            }

            // X Missing
            SupplyPickUpSpot.X -> when (preSpot) {
                SupplyPickUpSpot.Triangle -> SupplyPickUpSpot.xCannon to "Go X Cannon"
                SupplyPickUpSpot.X -> SupplyPickUpSpot.Shop to if (advanced) "Pull Square and X Cannon. Next: collect Shop" else "Pull Square. Next: collect Shop"
                SupplyPickUpSpot.Equals -> if (advanced) SupplyPickUpSpot.Shop to "Go Shop" else SupplyPickUpSpot.xCannon to "Go X Cannon"
                SupplyPickUpSpot.Slash -> SupplyPickUpSpot.Square to "Go Square, place on X"
                else -> SupplyPickUpSpot.None to ""
            }

            else -> SupplyPickUpSpot.None to ""
        }
    }
}