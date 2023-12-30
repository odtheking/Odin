package me.odinclient.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module

object LockCursor: Module(
    name = "Lock Cursor",
    description = "Makes you completely unable to move your camera.",
    category = Category.RENDER,
    tag = TagType.NEW
) // rest is in mixins