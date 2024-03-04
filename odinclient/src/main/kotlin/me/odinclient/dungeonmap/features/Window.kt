package me.odinclient.dungeonmap.features

import me.odinclient.dungeonmap.core.DungeonPlayer
import me.odinclient.dungeonmap.features.Dungeon.Info.dungeonList
import me.odinclient.features.impl.dungeon.MapModule
import me.odinclient.utils.skyblock.dungeon.map.MapUtils
import me.odinclient.utils.skyblock.dungeon.map.MapUtils.mapRoomSize
import me.odinmain.OdinMain.mc
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.render.RenderUtils

import me.odinmain.utils.skyblock.dungeon.*
import me.odinmain.utils.skyblock.itemID
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.Timer

object Window: JFrame() {

    private val xScale get() = width / 400.0
    private val yScale get() = height / 400.0

    fun init() {
        title = "OdinClient Dungeon Map"
        setSize(400, 400)
        defaultCloseOperation = DISPOSE_ON_CLOSE

        val panel = object : JPanel() {
            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)
                if (!DungeonUtils.inDungeons || !MapModule.enabled || (MapModule.hideInBoss && DungeonUtils.inBoss) || !DungeonScan.hasScanned) return
                val g2d = g as Graphics2D

                this.background = MapModule.backgroundColor.javaColor

                g2d.scale(xScale * 2.8, yScale * 2.8)
                renderRooms(g2d) // Renders all the rooms
                if (MapModule.showRunInfo) renderRunInformation(g2d) // Renders the run information underneath the map (Secrets deaths crypts)
                renderText(g2d) // Renders the text and checkmarks on the rooms depending on player's config
                renderPlayerHeads(g2d) // Renders the player heads (is last, so it renders on top of everything else)
            }
        }

        contentPane = panel

        val timer = Timer(100) { panel.repaint() } // Makes the panel update (render) every 100ms, even if it isn't focused
        timer.start()

        addWindowListener(object : java.awt.event.WindowAdapter() { // Hides the window when the user closes it, to not dispose it
            override fun windowClosing(e: java.awt.event.WindowEvent?) {
                isVisible = false
            }
        })
    }

    // Decides whether the window should be visible or not
    val shouldShow get() = MapModule.mapWindow && DungeonUtils.inDungeons && MapModule.enabled && (!MapModule.hideInBoss || !DungeonUtils.inBoss) && DungeonScan.hasScanned

    private fun renderRooms(g2d: Graphics2D) {
        g2d.translate(MapUtils.startCorner.first, MapUtils.startCorner.second)

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
                        g2d.color = color.javaColor
                        g2d.fillRect(
                            xOffset,
                            yOffset,
                            mapRoomSize,
                            mapRoomSize
                        )
                    }
                    !xEven && !yEven -> {
                        g2d.color = color.javaColor
                        g2d.fillRect(
                            xOffset,
                            yOffset,
                            (mapRoomSize + connectorSize),
                            (mapRoomSize + connectorSize)
                        )
                    }
                    else -> drawRoomConnector(
                        xOffset,
                        yOffset,
                        connectorSize,
                        tile is Door,
                        !xEven,
                        color.javaColor,
                        g2d
                    )
                }
            }
        }
        g2d.translate(-MapUtils.startCorner.first, -MapUtils.startCorner.second)
        g2d.color = Color.WHITE
    }

    private fun drawRoomConnector(x: Int, y: Int, doorWidth: Int, doorway: Boolean, vertical: Boolean, color: Color, g2d: Graphics2D) {
        val doorwayOffset = if (mapRoomSize == 16) 5 else 6
        val width = if (doorway) 6 else mapRoomSize
        var x1 = if (vertical) x + mapRoomSize else x
        var y1 = if (vertical) y else y + mapRoomSize
        if (doorway) {
            if (vertical) y1 += doorwayOffset else x1 += doorwayOffset
        }
        g2d.color = color
        g2d.fillRect(
            x1, y1,
            (if (vertical) doorWidth else width),
            (if (vertical) width else doorWidth)
        )
    }

    private fun renderRunInformation(g2d: Graphics2D) {
        g2d.translate(0, 128)
        g2d.scale(1 / 1.5, 1 / 1.5)
        g2d.color = Color.WHITE
        g2d.drawString("Secrets: ${RunInformation.secretCount}/${Dungeon.Info.secretCount}", 5, 0)
        g2d.drawString("Crypts: ${RunInformation.cryptsCount}", 85, 0)
        g2d.drawString("Deaths: ${RunInformation.deathCount}", 140, 0)
        g2d.scale(1.5, 1.5)
        g2d.translate(0.0, -128.0)
    }

    private fun renderPlayerHeads(g2d: Graphics2D) {
        if (Dungeon.dungeonTeammatesFmap.isEmpty()) { // only draw the player, as the dungeon hasn't started, so we can't be sure about the teammates
            drawPlayerHead(mc.thePlayer.name, DungeonPlayer(mc.thePlayer.locationSkin).apply {
                yaw = mc.thePlayer.rotationYawHead
            }, g2d)
        } else {
            Dungeon.dungeonTeammatesFmap.forEach { (name, teammate) ->
                if (teammate.dead) return@forEach
                drawPlayerHead(name, teammate, g2d)
            }
        }
    }

    private fun drawPlayerHead(name: String, player: DungeonPlayer, g2d: Graphics2D) {
        g2d.color = Color.WHITE
        try {
            if (name == mc.thePlayer.name) {
                g2d.translate(
                    (mc.thePlayer.posX - DungeonScan.startX + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.first - 2,
                    (mc.thePlayer.posZ - DungeonScan.startZ + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.second - 2
                )
            } else {
                g2d.translate(player.mapX, player.mapZ)
            }

            if (MapModule.playerHeads == 2 || MapModule.playerHeads == 1 && mc.thePlayer.heldItem?.itemID.equalsOneOf(
                    "SPIRIT_LEAP",
                    "INFINITE_SPIRIT_LEAP"
                )
            ) {
                g2d.scale(0.5, 0.5)
                g2d.drawString(
                    name,
                    -(g2d.fontMetrics.stringWidth(name) shr 1),
                    16
                )
                g2d.scale(2.0, 2.0)
            }

            g2d.rotate(Math.toRadians(player.yaw + 180.0), .0, .0)
            g2d.scale(MapModule.playerHeadScale.toDouble() * 1.5, MapModule.playerHeadScale.toDouble() * 1.5)
            g2d.color = Color.BLACK
            g2d.fillRect(-5, -5, 10, 10) // Draw outline

            if (player.bufferedImage.width == 8 && player.bufferedImage.height == 8) {
                g2d.drawImage(Dungeon.playerImage.getSubimage(8, 8, 8, 8), -4, -4, this) // Draw player if dungeon hasn't started
            } else {
                g2d.drawImage(player.bufferedImage.getSubimage(8, 8, 8, 8), -4, -4, this) // Draw head
            }

            g2d.scale(.66 / MapModule.playerHeadScale.toDouble(), .66 / MapModule.playerHeadScale.toDouble())
            g2d.rotate(-Math.toRadians(player.yaw + 180.0), .0, .0)


            if (name == mc.thePlayer.name) {
                g2d.translate(
                    -((mc.thePlayer.posX - DungeonScan.startX + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.first - 2),
                    -((mc.thePlayer.posZ - DungeonScan.startZ + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.second - 2)
                )
            } else {
                g2d.translate(-player.mapX, -player.mapZ)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun renderText(g2d: Graphics2D) {
        g2d.translate(MapUtils.startCorner.first, MapUtils.startCorner.second)

        val connectorSize = mapRoomSize shr 2
        val checkmarkSize = when (MapModule.checkMarkStyle) {
            1 -> 8 // default
            else -> 10 // neu
        }

        for (y in 0..10 step 2) {
            for (x in 0..10 step 2) {

                val tile = dungeonList[y * 11 + x]

                if (tile !is Room || tile !in Dungeon.Info.uniqueRooms) continue

                val xOffset = (x shr 1) * (mapRoomSize + connectorSize)
                val yOffset = (y shr 1) * (mapRoomSize + connectorSize)

                if (MapModule.checkMarkStyle != 0) {
                    getCheckmark(tile.state, MapModule.checkMarkStyle)?.let {
                        g2d.drawImage(
                            it,
                            xOffset + (mapRoomSize - checkmarkSize) / 2,
                            yOffset + (mapRoomSize - checkmarkSize) / 2,
                            checkmarkSize,
                            checkmarkSize,
                            this
                        )
                    }
                }

                val color = if (MapModule.colorText) when (tile.state) {
                    RoomState.GREEN -> Color(0, 170, 0)
                    RoomState.CLEARED, RoomState.FAILED -> Color.WHITE
                    else -> Color.GRAY
                } else Color.WHITE

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
                // Offset + half of the room's size
                renderCenteredText(name, xOffset + (mapRoomSize shr 1), yOffset + (mapRoomSize shr 1), color, g2d)
            }
        }
        g2d.translate(-MapUtils.startCorner.first, -MapUtils.startCorner.second)
    }

    private fun renderCenteredText(text: List<String>, x: Int, y: Int, color: Color, g2d: Graphics2D) {

        g2d.translate(x, y + 3)
        g2d.scale(MapModule.textScale.toDouble() / 1.5, MapModule.textScale.toDouble() / 1.5)

        g2d.color = color
        if (text.isNotEmpty()) {
            val yTextOffset = text.size * 5
            for (i in text.indices) {
                g2d.drawString(
                    text[i],
                    (-g2d.fontMetrics.stringWidth(text[i]) shr 1),
                    i * 12 - yTextOffset,
                )
            }
        }
        g2d.scale(1.5 / MapModule.textScale.toDouble(), 1.5 / MapModule.textScale.toDouble())
        g2d.translate(-x, -y - 3)
    }

    private fun getCheckmark(state: RoomState, type: Int): BufferedImage? {
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

    private val defaultWhite = RenderUtils.loadBufferedImage("/assets/odinclient/map/default/white_check.png")
    private val defaultGreen = RenderUtils.loadBufferedImage("/assets/odinclient/map/default/green_check.png")
    private val defaultCross = RenderUtils.loadBufferedImage("/assets/odinclient/map/default/cross.png")
    private val neuWhite = RenderUtils.loadBufferedImage("/assets/odinclient/map/neu/white_check.png")
    private val neuGreen = RenderUtils.loadBufferedImage("/assets/odinclient/map/neu/green_check.png")
    private val neuCross = RenderUtils.loadBufferedImage("/assets/odinclient/map/neu/cross.png")


}