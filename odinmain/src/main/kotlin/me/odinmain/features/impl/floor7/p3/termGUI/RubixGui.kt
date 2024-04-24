package me.odinmain.features.impl.floor7.p3.termGUI

import me.odinmain.utils.render.Box

object RubixGui : TermGui  {
    override val itemIndexMap: MutableMap<Int, Box> = mutableMapOf()

    override fun mouseClicked(x: Int, y: Int): Boolean {
        return false
    }
    override fun render() {

    }
}