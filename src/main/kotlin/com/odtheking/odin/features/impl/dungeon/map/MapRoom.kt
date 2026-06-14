package com.odtheking.odin.features.impl.dungeon.map

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.features.impl.dungeon.DungeonMap
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Color.Companion.darker
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.tiles.*
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.renderer.RenderPipelines

class MapRoom(var type: RoomType, var shape: RoomShape, var data: RoomData?, var height: Int) {
    val tiles = mutableListOf<Tile>()
    val places = mutableListOf<Vec2i>()
    var state = RoomState.UNDISCOVERED
    var rotation: Rotations = Rotations.NONE
    val doors = mutableSetOf<Door>()

    var entryTile: Vec2i? = null
    var isKnown1x1 = false
    var specialTile = false
    var rushRoom = false

    constructor(data: RoomData, height: Int) : this(data.type, data.shape, data, height)
    constructor(type: RoomType, shape: RoomShape) : this(type, shape, null, 0)

    class Tile(val owner: MapRoom?, val pos: Vec2i) {
        val placement = run {
            val x = (pos.x + 185) shr 5
            val z = (pos.z + 185) shr 5
            val xOffset = x * (16 + 4)
            val yOffset = z * (16 + 4)
            return@run Vec2i(xOffset, yOffset)
        }

        inline val listIndex: Int
            get() = (pos.x + 185) / 32 * 6 + (pos.z + 185) / 32
    }

    data class StateUpdated(val mapRoom: MapRoom, val old: RoomState, val new: RoomState)

    fun updateState(placement: Vec2i, color: Int): StateUpdated? {
        if (state == RoomState.GREEN && data?.name == "Golden Oasis") return null

        val oldState = state
        state = when (color) {
            0 -> RoomState.UNDISCOVERED
            34 -> RoomState.CLEARED
            18 -> when (data?.type) {
                RoomType.BLOOD -> {
                    MapScanner.blood = this
                    RoomState.DISCOVERED
                }
                RoomType.PUZZLE -> RoomState.FAILED
                else -> state
            }
            30 -> when (data?.type) {
                RoomType.ENTRANCE -> RoomState.DISCOVERED
                else -> RoomState.GREEN
            }
            85, 119 -> {
                entryTile = placement
                specialTile = placement.x == SpecialColumn.column
                RoomState.UNOPENED
            }
            else -> RoomState.DISCOVERED
        }

        return if (state != oldState) StateUpdated(this, oldState, state) else null
    }

    private fun color(): Array<Color> {
        if (state == RoomState.UNOPENED) {
            return if (isKnown1x1 && doors.count { it.seen } == 1 && !DungeonMap.disablePred) SpecialColumn.roomColorGuess(this)
            else if (type == RoomType.BLOOD) arrayOf(DungeonMap.bloodRoomColor.darker(DungeonMap.darkenMultiplier))
            else arrayOf(DungeonMap.unopenedRoomColor)
        }

        val color = when (type) {
            RoomType.BLOOD -> DungeonMap.bloodRoomColor
            RoomType.NORMAL -> DungeonMap.normalRoomColor
            RoomType.PUZZLE -> DungeonMap.puzzleRoomColor
            RoomType.CHAMPION -> DungeonMap.championRoomColor
            RoomType.TRAP -> DungeonMap.trapRoomColor
            RoomType.ENTRANCE -> DungeonMap.entranceRoomColor
            RoomType.FAIRY -> DungeonMap.fairyRoomColor
            RoomType.RARE -> DungeonMap.rareRoomColor
            RoomType.UNKNOWN -> return arrayOf(DungeonMap.unopenedRoomColor)
        }

        if (state == RoomState.UNDISCOVERED || state == RoomState.UNOPENED) {
            return arrayOf(color.darker(DungeonMap.darkenMultiplier))
        }

        return arrayOf(color)
    }

    fun render(graphics: GuiGraphicsExtractor) {
        if (state == RoomState.UNDISCOVERED && DungeonUtils.passedRooms.none { it.data.name == data?.name }) return

        val matrices = graphics.pose()

        if (state == RoomState.UNOPENED) {
            val entryTile = entryTile ?: return

            matrices.pushMatrix()
            matrices.translate(entryTile.x.toFloat() * 20, entryTile.z.toFloat() * 20)

            val color = color()
            when (color.size) {
                1 -> graphics.fill(0, 0, 16, 16, color[0].rgba)
                2 -> {
                    graphics.fill(0, 0, 16, 8, color[0].rgba)
                    graphics.fill(0, 8, 16, 16, color[1].rgba)
                }
                3 -> {
                    graphics.fill(0, 0, 16, 5, color[0].rgba)
                    graphics.fill(0, 0, 5, 10, color[0].rgba)
                    graphics.fill(10, 5, 16, 16, color[1].rgba)
                    graphics.fill(0, 10, 16, 16, color[1].rgba)
                    graphics.fill(5, 5, 11, 11, color[2].rgba)
                }
            }

            matrices.popMatrix()

            return
        }

        val topLeft = tiles.minBy { tile -> tile.placement.x * 1000 + tile.placement.z }.placement
        val bottomRight = tiles.maxBy { tile -> tile.placement.x * 1000 + tile.placement.z }.placement

        val color = color()
        if (shape == RoomShape.L && tiles.size > 2) {
            when (rotation) {
                Rotations.WEST -> {
                    graphics.fill(topLeft.x, topLeft.z, topLeft.x + 36, topLeft.z + 16, color[0].rgba)
                    graphics.fill(topLeft.x, topLeft.z, topLeft.x + 16, topLeft.z + 36, color[0].rgba)
                }
                Rotations.NORTH -> {
                    graphics.fill(topLeft.x, topLeft.z, bottomRight.x + 16, topLeft.z + 16, color[0].rgba)
                    graphics.fill(bottomRight.x, topLeft.z, bottomRight.x + 16, bottomRight.z + 16, color[0].rgba)
                }
                else -> {
                    graphics.fill(topLeft.x, topLeft.z, topLeft.x + 16, topLeft.z + 36, color[0].rgba)
                    graphics.fill(topLeft.x, bottomRight.z, topLeft.x + 36, bottomRight.z + 16, color[0].rgba)
                }
            }
        } else graphics.fill(topLeft.x, topLeft.z, bottomRight.x + 16, bottomRight.z + 16, color[0].rgba)
    }

    fun renderName(context: GuiGraphicsExtractor, textFactor: Float) {
        val matrices = context.pose()
        val fontHeight = mc.font.lineHeight

        if (DungeonUtils.passedRooms.any { it.data.name == data?.name } && type != RoomType.FAIRY && state != RoomState.UNDISCOVERED) {
            data?.name?.let { name ->
                val splitName = name.split(" ")
                val defaultHeight =
                    8 - fontHeight / (2 * textFactor) - ((splitName.size - 1) / 2.0 * (fontHeight / textFactor)).toInt()

                val placement = textPlacement()
                splitName.withIndex().forEach { (index, value) ->
                    matrices.pushMatrix()
                    matrices.translate(
                        placement.x + 8f,
                        placement.z + index * (fontHeight / textFactor) + defaultHeight
                    )
                    matrices.scale(DungeonMap.textScaling)

                    val color = when (state) {
                        RoomState.GREEN -> Colors.MINECRAFT_GREEN.rgba
                        RoomState.CLEARED -> Color(255, 255, 255).rgba
                        RoomState.DISCOVERED -> Color(100, 100, 100).rgba
                        RoomState.FAILED -> Color(255, 0, 0).rgba
                        else -> Color(255, 255, 255).rgba
                    }

                    context.centeredText(mc.font, value, 0, 0, color)

                    matrices.popMatrix()
                }

                return
            }
        }

        val resource = when (state) {
            RoomState.GREEN -> DungeonMap.green
            RoomState.CLEARED -> DungeonMap.white
            RoomState.FAILED -> DungeonMap.x
            RoomState.UNOPENED -> {
                if ((isKnown1x1 && doors.size == 1) || type == RoomType.BLOOD) return
                DungeonMap.question
            }
            else -> return
        }

        if (resource != DungeonMap.question && type == RoomType.FAIRY) return

        val placement = if (state == RoomState.UNOPENED) {
            entryTile?.multiply(20) ?: return
        } else textPlacement()

        context.blit(
            RenderPipelines.GUI_TEXTURED,
            resource,
            placement.x + 2, placement.z + 2,
            0f, 0f,
            12, 12,
            12, 12,
            0xFFFFFFFF.toInt()
        )
    }

    fun separator(pos: Vec2i): Tile = Tile(this, pos)

    fun roomTile(pos: Vec2i): Tile? {
        if (tiles.any { pos == it.pos }) return null

        val tile = Tile(this, pos)
        tiles.add(tile)
        places.add(pos.add(Vec2i(185, 185)).divide(32))
        MapScanner.roomsList[tile.listIndex] = tile
        return tile
    }

    fun topLeftTilePlacement(): Vec2i =
        tiles.minBy { it.pos.x * 1000 + it.pos.z }.placement

    fun textPlacement(): Vec2i {
        if (rotation == Rotations.NONE) return topLeftTilePlacement()
        val placements = tiles.map { it.placement }

        return if (shape == RoomShape.L && tiles.size > 2) {
            val topLeft = tiles.minWith { a, b -> a.pos.x * 1000 + a.pos.z - b.pos.x * 1000 - b.pos.z }.placement
            when (rotation) {
                Rotations.EAST, Rotations.SOUTH -> topLeft.add(10, 20)
                else -> topLeft.add(10, 0)
            }
        } else Vec2i((placements.minOf { it.x } + placements.maxOf { it.x }) / 2, (placements.minOf { it.z } + placements.maxOf { it.z }) / 2)
    }
}