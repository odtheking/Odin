package me.odinclient.features.impl.general

import me.odinclient.features.Category
import me.odinclient.features.Module

object LockCursor: Module(
    name = "Lock Cursor",
    description = "Makes you completely unable to move your camera",
    category = Category.GENERAL,
) // rest is in mixins