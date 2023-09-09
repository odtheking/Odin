package me.odinclient.features.impl.skyblock

import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.features.settings.impl.SelectorSetting

object Splits : Module(
    "Splits",
    description = "Custom splits for different bosses.",
    category = Category.SKYBLOCK
) {

    private val kuudra: Boolean by BooleanSetting("Kuudra splits", description = "Toggles Kuudra splits")
    private val dungeon: Boolean by BooleanSetting("Dungeon splits", description = "Toggles Dungeon splits")
    private val hide0s: Boolean by BooleanSetting("Hide 0s", description = "Hides splits that are 0s")



    private val stormTime = 0.000

    /*
    currentPhase = 0
    now its 71
    update time every frame
    now its 72
    update the time of 72
     */







}