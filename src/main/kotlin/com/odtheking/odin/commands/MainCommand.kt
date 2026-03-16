package com.odtheking.odin.commands

import com.github.stivais.commodore.Commodore
import com.github.stivais.commodore.parsers.CommandParsable
import com.github.stivais.commodore.utils.GreedyString
import com.github.stivais.commodore.utils.SyntaxException
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.ClickGUI
import com.odtheking.odin.clickgui.HudManager
import com.odtheking.odin.features.ModuleManager
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.*
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.skyblock.PartyUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils

val mainCommand = Commodore("odin", "od") {
    runs {
        schedule(0) { mc.setScreen(ClickGUI) }
    }

    literal("edithud").runs {
        schedule(0) { mc.setScreen(HudManager) }
    }

    literal("tps").runs {
        modMessage("§aTPS: §f${ServerUtils.averageTps}")
    }

    literal("ping").runs {
        modMessage("§aPing: §f${ServerUtils.averagePing}ms")
    }

    literal("ep").runs {
        fillItemFromSack(16, "ENDER_PEARL", "ender_pearl", true)
    }

    literal("ij").runs {
        fillItemFromSack(64, "INFLATABLE_JERRY", "inflatable_jerry", true)
    }

    literal("sl").runs {
        fillItemFromSack(16, "SPIRIT_LEAP", "spirit_leap", true)
    }

    literal("sb").runs {
        fillItemFromSack(64, "SUPERBOOM_TNT", "superboom_tnt", true)
    }

    literal("dd").runs {
        fillItemFromSack(64, "DUNGEON_DECOY", "dungeon_decoy", true)
    }

    literal("sendcoords").runs { message: GreedyString? ->
        sendChatMessage(getPositionString() + if (message == null) "" else " ${message.string}")
    }

    literal("leaporder").executable {
        param("player1") {
            parser { string: String ->
                if (!PartyUtils.members.contains(string)) throw SyntaxException("Player not in your party.")
                string
            }
            suggests { PartyUtils.members.map { it.lowercase() } }
        }
        param("player2") {
            parser { string: String ->
                if (!PartyUtils.members.contains(string)) throw SyntaxException("Player not in your party.")
                string
            }
            suggests { PartyUtils.members.map { it.lowercase() } }
        }
        param("player3") {
            parser { string: String ->
                if (!PartyUtils.members.contains(string)) throw SyntaxException("Player not in your party.")
                string
            }
            suggests { PartyUtils.members.map { it.lowercase() } }
        }
        param("player4") {
            parser { string: String ->
                if (!PartyUtils.members.contains(string)) throw SyntaxException("Player not in your party.")
                string
            }
            suggests { PartyUtils.members.map { it.lowercase() } }
        }
        runs { player1: String?, player2: String?, player3: String?, player4: String? ->
            val players = listOf(player1, player2, player3, player4).mapNotNull { it?.lowercase() }
            DungeonUtils.customLeapOrder = players
            modMessage("§aCustom leap order set to: §f${player1}, ${player2}, ${player3}, $player4")
        }
    }

    literal("reset") {
        literal("module").executable {
            param("moduleName") {
                // keys for modules are already lowercase
                suggests { ModuleManager.modules.keys.map { it.replace(" ", "_") } }
            }

            runs { moduleName: String ->
                val module = ModuleManager.modules[moduleName.replace("_", " ")]
                    ?: throw SyntaxException("Module not found.")

                module.settings.forEach { (_, setting) -> setting.reset() }
                modMessage("§aSettings for module §f${module.name} §ahas been reset to default values.")
            }
        }

        literal("clickgui").runs {
            ClickGUIModule.resetPositions()
            modMessage("Reset click gui positions.")
        }
        literal("hud").runs {
            HudManager.resetHUDS()
            modMessage("Reset HUD positions.")
        }
    }

    runs { floor: Floors -> sendCommand("joininstance ${floor.instance()}") }
    runs { tier: KuudraTier -> sendCommand("joininstance ${tier.instance()}") }
}

@CommandParsable
private enum class Floors {
    F1, F2, F3, F4, F5, F6, F7, M1, M2, M3, M4, M5, M6, M7;

    private val floors = listOf("one", "two", "three", "four", "five", "six", "seven")
    fun instance() = "${if (ordinal > 6) "master_" else ""}catacombs_floor_${floors[(ordinal % 7)]}"
}

@CommandParsable
private enum class KuudraTier(private val test: String) {
    T1("normal"), T2("hot"), T3("burning"), T4("fiery"), T5("infernal");

    fun instance() = "kuudra_${test}"
}