package me.odinclient.dungeonmap.core

import net.minecraft.util.ResourceLocation
import java.awt.image.BufferedImage

data class DungeonPlayer(val skin: ResourceLocation) {
    var mapX = 0
    var mapZ = 0
    var yaw = 0f
    var renderHat = false
    var icon = ""
    var dead = false
    var bufferedImage: BufferedImage = BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB)
}