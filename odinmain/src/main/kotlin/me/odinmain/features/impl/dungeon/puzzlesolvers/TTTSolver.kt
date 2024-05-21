package me.odinmain.features.impl.dungeon.puzzlesolvers

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.EnteredDungeonRoomEvent
import me.odinmain.utils.*
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.tiles.Rotations
import net.minecraft.entity.item.EntityItemFrame
import net.minecraft.init.Items
import net.minecraft.util.BlockPos
import kotlin.experimental.and

object TTTSolver {

    // currently just rendering the board, no actual solving

    private var bottomRight = Vec2(0, 0)
    private val board = Array(3) { Array(3) { State.Blank to BlockPos(0, 0,0) } }

    fun tttRoomEnter(event: EnteredDungeonRoomEvent) {
        val room = event.room?.room ?: return
        if (room.data.name != "Tic Tac Toe") return

        val vec2 = Vec2(room.x, room.z)

        bottomRight = vec2.addRotationCoords(room.rotation, 7, 0)

        initializeBoard(bottomRight, room.rotation)
    }

    private fun initializeBoard(bottomRight: Vec2, rotations: Rotations) {
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                val location = bottomRight.addRotationCoords(rotations, 0, -j)
                    .let { BlockPos(it.x.toDouble(), 70.0 + i, it.z.toDouble())}
                board[i][j] = findSlotState(location) to location
            }
        }
    }
    // need to figure out when to call this, but it works
    private fun updateBoard(rotations: Rotations) {
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                val currentSlot = bottomRight.addRotationCoords(rotations, 0, -j)
                    .let { BlockPos(it.x.toDouble(), 70.0 + i, it.z.toDouble())}

                val slotState = findSlotState(currentSlot)
                when (slotState) {
                    State.X -> board[i][j] = State.X to currentSlot
                    State.O -> board[i][j] = State.O to currentSlot
                    else -> {}
                }
            }
        }
    }

    private fun findSlotState(blockPos: BlockPos): State {
        val itemFrameBlock = mc.theWorld.getEntitiesWithinAABB(EntityItemFrame::class.java, blockPos.toAABB()).filterIsInstance<EntityItemFrame>().firstOrNull() ?: return State.Blank
        val mapData = Items.filled_map.getMapData(itemFrameBlock.displayedItem, mc.theWorld) ?: return State.Blank
        val colorInt: Int = (mapData.colors[8256] and 255.toByte()).toInt()
        return if (colorInt == 114) State.X else State.O
    }

    fun tttRenderWorld() {
        board.forEach { rows ->
            rows.forEach { columns ->
                val state = columns.first
                val blockPos = columns.second

                val color = when (state) {
                    State.X -> Color.RED
                    State.O -> Color.BLUE
                    else -> Color.WHITE
                }
                Renderer.drawBox(blockPos.toAABB(), color, 1f, fillAlpha = 0f)
            }
        }
    }

    enum class State {
        Blank, X, O
    }
}