package me.odinmain.features.impl.nether

import me.odinmain.clickgui.settings.Setting.Companion.withDependency
import me.odinmain.clickgui.settings.impl.BooleanSetting
import me.odinmain.clickgui.settings.impl.NumberSetting
import me.odinmain.features.Module
import me.odinmain.utils.skyblock.KuudraUtils.giantZombies
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.skyblock.partyMessage
import net.minecraft.util.Vec3

object NoPre : Module(
    name = "Pre-Spot Alert",
    description = "Alerts the party if a pre spot is missing."
) {
    private val showCratePriority by BooleanSetting("Show Crate Priority", false, desc = "Shows the crate priority alert.")
    private val cratePriorityTitleTime by NumberSetting("Title Time", 30, 1, 60, desc = "The time the crate priority alert will be displayed for.").withDependency { showCratePriority }
    private val advanced by BooleanSetting("Advanced Mode", false, desc = "Provides harder crate priority in certain situations.").withDependency { showCratePriority }

    private var preSpot = Supply.None
    var missing = Supply.None

    init {
        onMessage(Regex("\\[NPC] Elle: Head over to the main platform, I will join you when I get a bite!")) {
            val playerLocation = mc.thePlayer?.positionVector ?: return@onMessage
            preSpot = when {
                Supply.Triangle.pickUpSpot.distanceTo(playerLocation) < 15 -> Supply.Triangle
                Supply.X.pickUpSpot.distanceTo(playerLocation) < 30 -> Supply.X
                Supply.Equals.pickUpSpot.distanceTo(playerLocation) < 15 -> Supply.Equals
                Supply.Slash.pickUpSpot.distanceTo(playerLocation) < 15 -> Supply.Slash
                else -> Supply.None
            }
            modMessage(if (preSpot == Supply.None) "§cDidn't register your pre-spot because you didn't get there in time." else "Pre-spot: ${preSpot.name}")
        }

        onMessage(Regex("\\[NPC] Elle: Not again!")) {
            if (preSpot == Supply.None) return@onMessage
            var pre = false
            var second = false
            var msg = ""
            giantZombies.forEach { supply ->
                val supplyLoc = Vec3(supply.posX, 76.0, supply.posZ)
                when {
                    preSpot.pickUpSpot.distanceTo(supplyLoc) < 18 -> pre = true
                    preSpot == Supply.Triangle && Supply.Shop.pickUpSpot.distanceTo(supplyLoc) < 18 -> second = true
                    preSpot == Supply.X && Supply.xCannon.pickUpSpot.distanceTo(supplyLoc) < 16 -> second = true
                    preSpot == Supply.Slash && Supply.Square.pickUpSpot.distanceTo(supplyLoc) < 20 -> second = true
                }
            }
            if (second && pre) return@onMessage
            if (!pre && preSpot != Supply.None) msg = "No ${preSpot.name}!"
            else if (!second) {
                msg = when (preSpot) {
                    Supply.Triangle -> "No Shop!"
                    Supply.X -> "No xCannon!"
                    Supply.Slash -> "No Square!"
                    else -> return@onMessage
                }
            }
            if (msg.isEmpty()) return@onMessage
            partyMessage(msg)
        }

        onMessage(Regex("^Party > (\\[[^]]*?])? ?(\\w{1,16}): No ?(Triangle|X|Equals|Slash|xCannon|Square|Shop)!$")) {
            missing = Supply.valueOf(it.groupValues.lastOrNull() ?: return@onMessage)
            if (!showCratePriority) return@onMessage
            val cratePriority = cratePriority(missing).ifEmpty { return@onMessage }
            PlayerUtils.alert(cratePriority, time = cratePriorityTitleTime)
            modMessage("Crate Priority: $cratePriority")
        }

        onWorldLoad {
            preSpot = Supply.None
            missing = Supply.None
        }
    }
    
    private fun cratePriority(missing: Supply): String {
        return when (missing) {
            // Shop Missing
            Supply.Shop -> when (preSpot) {
                Supply.Triangle, Supply.X -> "Go X Cannon"
                Supply.Equals, Supply.Slash -> "Go Square, place on Shop"
                else -> ""
            }

            // Triangle Missing
            Supply.Triangle -> when (preSpot) {
                Supply.Triangle -> if (advanced) "Pull Square and X Cannon. Next: collect Shop" else "Pull Square. Next: collect Shop"
                Supply.X -> "Go X Cannon"
                Supply.Equals -> if (advanced) "Go Shop" else "Go X Cannon"
                Supply.Slash -> "Go Square, place on Triangle"
                else -> ""
            }

            // Equals Missing
            Supply.Equals -> when (preSpot) {
                Supply.Triangle -> if (advanced) "Go Shop" else "Go X Cannon"
                Supply.X -> "Go X Cannon"
                Supply.Equals -> if (advanced) "Pull Square and X Cannon. Next: collect Shop" else "Pull Square. Next: collect Shop"
                Supply.Slash -> "Go Square, place on Equals"
                else -> ""
            }

            // Slash Missing
            Supply.Slash -> when (preSpot) {
                Supply.Triangle -> "Go Square, place on Slash"
                Supply.X -> "Go X Cannon"
                Supply.Equals -> if (advanced) "Go Shop" else "Go X Cannon"
                Supply.Slash -> if (advanced) "Pull Square and X Cannon. Next: collect Shop" else "Pull Square. Next: collect Shop"
                else -> ""
            }

            // Square Missing
            Supply.Square -> when (preSpot) {
                Supply.Triangle, Supply.Equals -> "Go Shop"
                Supply.X, Supply.Slash -> "Go X Cannon"
                else -> ""
            }

            // X Cannon Missing
            Supply.xCannon -> when (preSpot) {
                Supply.Triangle, Supply.Equals -> "Go Shop"
                Supply.Slash, Supply.X -> "Go Square, place on X Cannon"
                else -> ""
            }

            // X Missing
            Supply.X -> when (preSpot) {
                Supply.Triangle -> "Go X Cannon"
                Supply.X -> if (advanced) "Pull Square and X Cannon. Next: collect Shop" else "Pull Square. Next: collect Shop"
                Supply.Equals -> if (advanced) "Go Shop" else "Go X Cannon"
                Supply.Slash -> "Go Square, place on X"
                else -> ""
            }

            else -> ""
        }
    }
}