package me.odinclient.dungeonmap.features

import me.odinclient.dungeonmap.core.DungeonPlayer
import me.odinclient.features.impl.dungeon.MapModule
import me.odinclient.utils.skyblock.dungeon.map.MapRenderUtils
import me.odinclient.utils.skyblock.dungeon.map.MapUtils
import me.odinclient.utils.skyblock.dungeon.map.MapUtils.mapRoomSize
import me.odinmain.OdinMain.mc
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.gui.nvg.drawTexturedModalRect
import me.odinmain.utils.skyblock.dungeon.*
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.inDungeons
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

// TODO: MAKE IT RENDER WITH HUD ELEMENT.
object MapRender {

    private val neuGreen = ResourceLocation("odinclient", "map/neu/green_check.png")
    private val neuWhite = ResourceLocation("odinclient", "map/neu/white_check.png")
    private val neuCross = ResourceLocation("odinclient", "map/neu/cross.png")
    private val defaultGreen = ResourceLocation("odinclient", "map/default/green_check.png")
    private val defaultWhite = ResourceLocation("odinclient", "map/default/white_check.png")
    private val defaultCross = ResourceLocation("odinclient", "map/default/cross.png")

    @SubscribeEvent
    fun onOverlay(event: RenderGameOverlayEvent.Post) {
        if (event.type != RenderGameOverlayEvent.ElementType.HOTBAR || !inDungeons || !MapModule.enabled) return
        if ((MapModule.hideInBoss && DungeonUtils.inBoss) || MapModule.mapWindow) return

        GlStateManager.pushMatrix()
        GlStateManager.translate(MapModule.mapX, MapModule.mapY, 0f)
        GlStateManager.scale(MapModule.mapScale, MapModule.mapScale, 1f)

        MapRenderUtils.renderRect(
            0.0,
            0.0,
            128.0,
            if (MapModule.showRunInfo) 138.0 else 128.0,
            MapModule.backgroundColor
        )

        MapRenderUtils.renderRectBorder(
            0.0,
            0.0,
            128.0,
            if (MapModule.showRunInfo) 138.0 else 128.0,
            MapModule.borderWidth,
            MapModule.borderColor
        )

        renderRooms()
        renderText()
        renderPlayerHeads()

        if (MapModule.showRunInfo) {
            renderRunInformation()
        }

        GlStateManager.popMatrix()
    }

    private fun renderRooms() {
        GlStateManager.pushMatrix()
        GlStateManager.translate(MapUtils.startCorner.first.toFloat(), MapUtils.startCorner.second.toFloat(), 0f)

        val connectorSize = mapRoomSize shr 2

        for (y in 0..10) {
            for (x in 0..10) {
                val tile = Dungeon.Info.dungeonList[y * 11 + x]
                if (tile is Door && tile.type == DoorType.NONE) continue

                val xOffset = (x shr 1) * (mapRoomSize + connectorSize)
                val yOffset = (y shr 1) * (mapRoomSize + connectorSize)

                val xEven = x and 1 == 0
                val yEven = y and 1 == 0

                val color = tile.color

                when {
                    xEven && yEven -> if (tile is Room) {
                        MapRenderUtils.renderRect(
                            xOffset.toDouble(),
                            yOffset.toDouble(),
                            mapRoomSize.toDouble(),
                            mapRoomSize.toDouble(),
                            color
                        )
                    }
                    !xEven && !yEven -> {
                        MapRenderUtils.renderRect(
                            xOffset.toDouble(),
                            yOffset.toDouble(),
                            (mapRoomSize + connectorSize).toDouble(),
                            (mapRoomSize + connectorSize).toDouble(),
                            color
                        )
                    }
                    else -> drawRoomConnector(
                        xOffset,
                        yOffset,
                        connectorSize,
                        tile is Door,
                        !xEven,
                        color
                    )
                }
            }
        }
        GlStateManager.popMatrix()
    }

    private fun renderText() {
        GlStateManager.pushMatrix()
        GlStateManager.translate(MapUtils.startCorner.first.toFloat(), MapUtils.startCorner.second.toFloat(), 0f)

        val connectorSize = mapRoomSize shr 2
        val checkmarkSize = when (MapModule.checkMarkStyle) {
            1 -> 8 // default
            else -> 10 // neu
        }

        for (y in 0..10 step 2) {
            for (x in 0..10 step 2) {

                val tile = Dungeon.Info.dungeonList[y * 11 + x]

                if (tile is Room && tile in Dungeon.Info.uniqueRooms) {

                    val xOffset = (x shr 1) * (mapRoomSize + connectorSize)
                    val yOffset = (y shr 1) * (mapRoomSize + connectorSize)

                    if (MapModule.checkMarkStyle != 0) {
                        getCheckmark(tile.state, MapModule.checkMarkStyle)?.let {
                            GlStateManager.enableAlpha()
                            GlStateManager.color(255f, 255f, 255f, 255f)
                            mc.textureManager.bindTexture(it)

                            drawTexturedModalRect(
                                xOffset + (mapRoomSize - checkmarkSize) / 2,
                                yOffset + (mapRoomSize - checkmarkSize) / 2,
                                checkmarkSize,
                                checkmarkSize
                            )
                            GlStateManager.disableAlpha()
                        }
                    }

                    val color = if (MapModule.colorText) when (tile.state) {
                        RoomState.GREEN -> 0x55ff55
                        RoomState.CLEARED, RoomState.FAILED -> 0xffffff
                        else -> 0xaaaaaa
                    } else 0xffffff

                    val name = mutableListOf<String>()

                    if (MapModule.roomNames != 0 && tile.data.type.equalsOneOf(RoomType.PUZZLE, RoomType.TRAP) ||
                        MapModule.roomNames == 2 && tile.data.type.equalsOneOf(
                            RoomType.NORMAL,
                            RoomType.RARE,
                            RoomType.CHAMPION
                        )
                    ) {
                        name.addAll(tile.data.name.split(" "))
                    }
                    // Offset + half of roomsize
                    MapRenderUtils.renderCenteredText(name, xOffset + (mapRoomSize shr 1), yOffset + (mapRoomSize shr 1), color)
                }
            }
        }
        GlStateManager.popMatrix()
    }

    private fun getCheckmark(state: RoomState, type: Int): ResourceLocation? {
        return when (type) {
            1 -> when (state) {
                RoomState.CLEARED -> defaultWhite
                RoomState.GREEN -> defaultGreen
                RoomState.FAILED -> defaultCross
                else -> null
            }
            2 -> when (state) {
                RoomState.CLEARED -> neuWhite
                RoomState.GREEN -> neuGreen
                RoomState.FAILED -> neuCross
                else -> null
            }
            else -> null
        }
    }

    private fun renderPlayerHeads() {
        if (Dungeon.dungeonTeammates.isEmpty()) {
            MapRenderUtils.drawPlayerHead(mc.thePlayer.name, DungeonPlayer(mc.thePlayer.locationSkin).apply {
                yaw = mc.thePlayer.rotationYawHead
            })
        } else {
            Dungeon.dungeonTeammates.forEach { (name, teammate) ->
                if (!teammate.dead) {
                    MapRenderUtils.drawPlayerHead(name, teammate)
                }
            }
        }
    }

    private fun drawRoomConnector(
        x: Int,
        y: Int,
        doorWidth: Int,
        doorway: Boolean,
        vertical: Boolean,
        color: Color
    ) {
        val doorwayOffset = if (mapRoomSize == 16) 5 else 6
        val width = if (doorway) 6 else mapRoomSize
        var x1 = if (vertical) x + mapRoomSize else x
        var y1 = if (vertical) y else y + mapRoomSize
        if (doorway) {
            if (vertical) y1 += doorwayOffset else x1 += doorwayOffset
        }
        MapRenderUtils.renderRect(
            x1.toDouble(), y1.toDouble(),
            (if (vertical) doorWidth else width).toDouble(),
            (if (vertical) width else doorWidth).toDouble(),
            color
        )
    }

    private fun renderRunInformation() {
        GlStateManager.pushMatrix()
        GlStateManager.translate(0f, 128f, 0f)
        GlStateManager.scale(0.66, 0.66, 1.0)
        mc.fontRendererObj.drawString("Secrets: ${RunInformation.secretCount}/${Dungeon.Info.secretCount}", 5, 0, 0xffffff)
        mc.fontRendererObj.drawString("Crypts: ${RunInformation.cryptsCount}", 85, 0, 0xffffff)
        mc.fontRendererObj.drawString("Deaths: ${RunInformation.deathCount}", 140, 0, 0xffffff)
        GlStateManager.popMatrix()
    }
}