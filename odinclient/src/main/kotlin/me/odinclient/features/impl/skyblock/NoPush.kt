package me.odinclient.features.impl.skyblock

import me.odinmain.features.Category
import me.odinmain.features.Module

/**
 * @see me.odinclient.mixin.mixins.entity.MixinEntityPlayerSP
 */
object NoPush : Module(
    name = "No Push",
    category = Category.SKYBLOCK,
    description = "Prevents from being pushed out of blocks"
) {  }