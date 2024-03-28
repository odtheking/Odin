package me.odinmain.features.impl.dungeon.puzzlesolvers

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.ClickEvent
import me.odinmain.features.impl.dungeon.puzzlesolvers.PuzzleSolvers.blockWrongClicks
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.currentRoomName
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.inDungeons
import me.odinmain.utils.toAABB
import net.minecraft.entity.item.EntityItemFrame
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MovingObjectPosition
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.experimental.and

/**
 * Solver based on [Floppa Client](https://github.com/FloppaCoding/FloppaClient/)
 * and [Skytils](https://github.com/Skytils/SkytilsMod/)
 */
object TicTacToe {

    private var topLeft: BlockPos? = null
    private var roomFacing: EnumFacing? = null
    private var board: Board? = null
    private var mappedPositions = HashMap<Int,EntityItemFrame>()
    private var bestMove: BlockPos? = null

    /**
     * Solve the board.
     *
     * Taken from Skytils.
     */
    fun tttTick(event: TickEvent.ClientTickEvent) {
        if (!inDungeons || event.phase != TickEvent.Phase.START || mc.thePlayer == null || mc.theWorld == null) return

        if (!currentRoomName.contains("Tic Tac Toe")) {
            bestMove = null
            return
        }

        val frames = mc.theWorld.loadedEntityList.filter {
            if (it !is EntityItemFrame) return@filter false
            val realPos = it.position.down()
            if (it.rotation != 0 || realPos.y !in 70..72) return@filter false
            val item = it.displayedItem
            if (item == null || item.item != Items.filled_map) return@filter false
            val mapData = Items.filled_map.getMapData(item, mc.theWorld) ?: return@filter false
            val colorInt: Int = (mapData.colors[8256] and 255.toByte()).toInt()
            if (colorInt != 114 && colorInt != 33) return@filter false
            val blockBehind = realPos.offset(it.facingDirection.opposite, 1)
            return@filter mc.theWorld.getBlockState(blockBehind).block == Blocks.iron_block
        }
        try {
            if (topLeft == null || roomFacing == null || board == null) {
                for (frame in frames) {
                    if (frame !is EntityItemFrame) continue
                    val realPos = frame.position.down()
                    val blockBehind = realPos.offset(frame.facingDirection.opposite, 1)
                    val row = when (realPos.y) {
                        72 -> 0
                        71 -> 1
                        70 -> 2
                        else -> continue
                    }
                    val column = when {
                        mc.theWorld.getBlockState(blockBehind.offset(frame.facingDirection.rotateYCCW())).block != Blocks.iron_block -> 2
                        mc.theWorld.getBlockState(blockBehind.offset(frame.facingDirection.rotateY())).block != Blocks.iron_block -> 0
                        else -> 1
                    }
                    val mapData = Items.filled_map.getMapData(frame.displayedItem, mc.theWorld) ?: continue
                    val colorInt: Int = (mapData.colors[8256] and 255.toByte()).toInt()
                    val owner = if (colorInt == 114) Board.State.X else Board.State.O
                    if (board == null) {
                        topLeft = realPos.up(row).offset(frame.facingDirection.rotateY(), column)
                        roomFacing = frame.facingDirection.opposite
                        board = Board()
                    }
                    with(board!!) {
                        place(column, row, owner)
                        mappedPositions[row * Board.BOARD_WIDTH + column] = frame
                    }
                }
                if (board != null) {
                    board!!.turn = if (frames.size % 2 == 0) Board.State.X else Board.State.O
                }
            } else if (!board!!.isGameOver) {
                with(board!!) {
                    turn = if (frames.size % 2 == 0) Board.State.X else Board.State.O
                    if (turn == Board.State.O) {
                        for (frame in frames) {
                            if (frame !is EntityItemFrame) continue
                            if (!mappedPositions.containsValue(frame)) {
                                val mapData =
                                    Items.filled_map.getMapData(frame.displayedItem, mc.theWorld) ?: continue
                                val colorInt: Int = (mapData.colors[8256] and 255.toByte()).toInt()
                                val owner = if (colorInt == 114) Board.State.X else Board.State.O
                                val realPos = frame.position.down()
                                val blockBehind = realPos.offset(frame.facingDirection.opposite, 1)
                                with(board!!) {
                                    val row = when (realPos.y) {
                                        72 -> 0
                                        71 -> 1
                                        70 -> 2
                                        else -> -1
                                    }
                                    val column =
                                        if (mc.theWorld.getBlockState(blockBehind.offset(frame.facingDirection.rotateYCCW())).block != Blocks.iron_block) {
                                            2
                                        } else {
                                            if (mc.theWorld.getBlockState(blockBehind.offset(frame.facingDirection.rotateY())).block != Blocks.iron_block) {
                                                0
                                            } else 1
                                        }
                                    place(column, row, owner)
                                    mappedPositions[row * Board.BOARD_WIDTH + column] = frame
                                }
                            }
                        }
                        AlphaBetaAdvanced.run(this)

                        val move = algorithmBestMove
                        if (move != -1) {
                            val column = move % Board.BOARD_WIDTH
                            val row = move / Board.BOARD_WIDTH
                            bestMove = topLeft!!.down(row).offset(roomFacing!!.rotateY(), column)
                        }
                    } else {
                        bestMove = null
                    }
                }
            } else {
                bestMove = null
            }
        } catch (e: Exception) {
            bestMove = null
        }
    }

    fun reset() {
        topLeft = null
        roomFacing = null
        board = null
        bestMove = null
        mappedPositions.clear()
    }


    fun tttRender() {
        if (!inDungeons) return

        Renderer.drawBox(bestMove?.toAABB() ?: return, Color.GREEN, fillAlpha = 0, depth = true)
    }

    fun tttRightClick(event: ClickEvent.RightClickEvent) {
        if (!currentRoomName.contains("Tic Tac Toe") || !blockWrongClicks || mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || mc.theWorld.getBlockState(mc.objectMouseOver.blockPos).block != Blocks.stone_button) return
        if (bestMove == null || mc.objectMouseOver.blockPos != bestMove) event.isCanceled = true
    }

    /**
     * Represents the Tic Tac Toe board.
     * Modified version of [LazoCoder's Tic-Tac-Toe Java Implementation](https://github.com/LazoCoder/Tic-Tac-Toe), GPLv3 License.
     */
    class Board {
        enum class State {
            Blank, X, O
        }

        private val board: Array<Array<State?>> = Array(BOARD_WIDTH) { arrayOfNulls(BOARD_WIDTH) }

        /**
         * Check to see who's turn it is.
         * @return the player who's turn it is
         */
        var turn: State = State.X

        /**
         * Check to see who won.
         * @return the player who won (or Blank if the game is a draw)
         */
        var winner: State? = null

        /**
         * Get the indexes of all the positions on the board that are empty.
         * @return the empty cells
         */
        var availableMoves: HashSet<Int>
            private set
        private var moveCount = 0

        /**
         * Check to see if the game is over (if there is a winner or a draw).
         * @return true if the game is over
         */
        var isGameOver = false
            private set

        var algorithmBestMove = -1

        /**
         * Set the cells to be blank and load the available moves (all the moves are
         * available at the start of the game).
         */
        private fun initialize() {
            for (row in 0 until BOARD_WIDTH) {
                for (col in 0 until BOARD_WIDTH) {
                    board[row][col] = State.Blank
                }
            }
            availableMoves.clear()
            for (i in 0 until BOARD_WIDTH * BOARD_WIDTH) {
                availableMoves.add(i)
            }
        }

        /**
         * Restart the game with a new blank board.
         */
        private fun reset() {
            moveCount = 0
            isGameOver = false
            turn = State.X
            winner = State.Blank
            initialize()
        }

        /**
         * Places an X or an O on the specified index depending on whose turn it is.
         * @param index     the position on the board (example: index 4 is location (0, 1))
         * @return          true if the move has not already been played
         */
        fun move(index: Int): Boolean {
            return move(index % BOARD_WIDTH, index / BOARD_WIDTH)
        }

        /**
         * Places an X or an O on the specified location depending on who turn it is.
         * @param x         the x coordinate of the location
         * @param y         the y coordinate of the location
         * @return          true if the move has not already been played
         */
        private fun move(x: Int, y: Int): Boolean {
            check(!isGameOver) { "TicTacToe is over. No moves can be played." }
            if (board[y][x] == State.Blank) {
                board[y][x] = turn
            } else {
                return false
            }
            moveCount++
            availableMoves.remove(y * BOARD_WIDTH + x)

            // The game is a draw.
            if (moveCount == BOARD_WIDTH * BOARD_WIDTH) {
                winner = State.Blank
                isGameOver = true
            }

            // Check for a winner.
            checkRow(y)
            checkColumn(x)
            checkDiagonalFromTopLeft(x, y)
            checkDiagonalFromTopRight(x, y)
            turn = if (turn == State.X) State.O else State.X
            return true
        }

        /**
         * Places an X or an O on the specified location based on a parameter
         * @param x         the x coordinate of the location
         * @param y         the y coordinate of the location
         * @param player    whether or not an X or an O should be played
         * @return          true if the move has not already been played
         */
        fun place(x: Int, y: Int, player: State): Boolean {
            check(!isGameOver) { "TicTacToe is over. No moves can be played." }
            if (board[y][x] == State.Blank) {
                board[y][x] = player
            } else {
                return false
            }
            moveCount++
            availableMoves.remove(y * BOARD_WIDTH + x)

            // The game is a draw.
            if (moveCount == BOARD_WIDTH * BOARD_WIDTH) {
                winner = State.Blank
                isGameOver = true
            }

            // Check for a winner.
            checkRow(y)
            checkColumn(x)
            checkDiagonalFromTopLeft(x, y)
            checkDiagonalFromTopRight(x, y)
            turn = if (turn == State.X) State.O else State.X
            return true
        }

        /**
         * Get a copy of the array that represents the board.
         * @return          the board array
         */
        fun toArray(): Array<Array<State?>> {
            return board.clone()
        }

        /**
         * Checks the specified row to see if there is a winner.
         * @param row       the row to check
         */
        private fun checkRow(row: Int) {
            for (i in 1 until BOARD_WIDTH) {
                if (board[row][i] != board[row][i - 1]) {
                    break
                }
                if (i == BOARD_WIDTH - 1) {
                    winner = turn
                    isGameOver = true
                }
            }
        }

        /**
         * Checks the specified column to see if there is a winner.
         * @param column    the column to check
         */
        private fun checkColumn(column: Int) {
            for (i in 1 until BOARD_WIDTH) {
                if (board[i][column] != board[i - 1][column]) {
                    break
                }
                if (i == BOARD_WIDTH - 1) {
                    winner = turn
                    isGameOver = true
                }
            }
        }

        /**
         * Check the left diagonal to see if there is a winner.
         * @param x         the x coordinate of the most recently played move
         * @param y         the y coordinate of the most recently played move
         */
        private fun checkDiagonalFromTopLeft(x: Int, y: Int) {
            if (x == y) {
                for (i in 1 until BOARD_WIDTH) {
                    if (board[i][i] != board[i - 1][i - 1]) {
                        break
                    }
                    if (i == BOARD_WIDTH - 1) {
                        winner = turn
                        isGameOver = true
                    }
                }
            }
        }

        /**
         * Check the right diagonal to see if there is a winner.
         * @param x     the x coordinate of the most recently played move
         * @param y     the y coordinate of the most recently played move
         */
        private fun checkDiagonalFromTopRight(x: Int, y: Int) {
            if (BOARD_WIDTH - 1 - x == y) {
                for (i in 1 until BOARD_WIDTH) {
                    if (board[BOARD_WIDTH - 1 - i][i] != board[BOARD_WIDTH - i][i - 1]) {
                        break
                    }
                    if (i == BOARD_WIDTH - 1) {
                        winner = turn
                        isGameOver = true
                    }
                }
            }
        }

        /**
         * Get a deep copy of the Tic Tac Toe board.
         * @return      an identical copy of the board
         */
        val deepCopy: Board
            get() {
                val board = Board()
                for (i in board.board.indices) {
                    board.board[i] = this.board[i].clone()
                }
                board.turn = turn
                board.winner = winner
                board.availableMoves = HashSet()
                board.availableMoves.addAll(availableMoves)
                board.moveCount = moveCount
                board.isGameOver = isGameOver
                return board
            }

        override fun toString(): String {
            val sb = StringBuilder()
            for (y in 0 until BOARD_WIDTH) {
                for (x in 0 until BOARD_WIDTH) {
                    if (board[y][x] == State.Blank) {
                        sb.append("-")
                    } else {
                        sb.append(board[y][x]!!.name)
                    }
                    sb.append(" ")
                }
                if (y != BOARD_WIDTH - 1) {
                    sb.append("\n")
                }
            }
            return String(sb)
        }

        companion object {
            const val BOARD_WIDTH = 3
        }

        /**
         * Construct the Tic Tac Toe board.
         */
        init {
            availableMoves = HashSet()
            reset()
        }
    }

    /**
     * Uses the Alpha-Beta Pruning algorithm to play a move in a game of Tic Tac Toe
     * but includes depth in the evaluation function.
     *
     * The vanilla MiniMax algorithm plays perfectly but it may occasionally
     * decide to make a move that will results in a slower victory or a faster loss.
     * For example, playing the move 0, 1, and then 7 gives the AI the opportunity
     * to play a move at index 6. This would result in a victory on the diagonal.
     * But the AI does not choose this move, instead it chooses another one. It
     * still wins inevitably, but it chooses a longer route. By adding the depth
     * into the evaluation function, it allows the AI to pick the move that would
     * make it win as soon as possible.
     *
     * Modified version of [LazoCoder's Tic-Tac-Toe Java Implementation](https://github.com/LazoCoder/Tic-Tac-Toe), GPLv3 License.
     */
    object AlphaBetaAdvanced {
        private var maxPly = 0.0

        /**
         * Play using the Alpha-Beta Pruning algorithm. Include depth in the
         * evaluation function and a depth limit.
         * @param board     the Tic Tac Toe board to play on
         * @param ply       the maximum depth
         */
        fun run(board: Board, ply: Double = Double.POSITIVE_INFINITY): Int {
            return run(board.turn, board, ply)
        }

        /**
         * Execute the algorithm.
         * @param player        the player that the AI will identify as
         * @param board         the Tic Tac Toe board to play on
         * @param maxPly        the maximum depth
         * @return              the score of the move
         */
        private fun run(player: Board.State, board: Board, maxPly: Double): Int {
            require(maxPly >= 1) { "Maximum depth must be greater than 0." }
            AlphaBetaAdvanced.maxPly = maxPly
            return alphaBetaPruning(player, board, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0)
        }

        /**
         * The meat of the algorithm.
         * @param player        the player that the AI will identify as
         * @param board         the Tic Tac Toe board to play on
         * @param alpha         the alpha value
         * @param beta          the beta value
         * @param currentPly    the current depth
         * @return              the index of the move to make
         */
        private fun alphaBetaPruning(player: Board.State, board: Board, alpha: Double, beta: Double, currentPly: Int): Int {
            var cp = currentPly
            if (cp++.toDouble() == maxPly || board.isGameOver) {
                return score(player, board, cp)
            }
            return if (board.turn == player) {
                getMax(player, board, alpha, beta, cp)
            } else {
                getMin(player, board, alpha, beta, cp)
            }
        }

        /**
         * Play the move with the highest score.
         * @param player        the player that the AI will identify as
         * @param board         the Tic Tac Toe board to play on
         * @param alpha         the alpha value
         * @param beta          the beta value
         * @param currentPly    the current depth
         * @return              the index of the move to make
         */
        private fun getMax(player: Board.State, board: Board, alpha: Double, beta: Double, currentPly: Int): Int {
            var a = alpha
            var indexOfBestMove = -1
            for (theMove in board.availableMoves) {
                val modifiedBoard = board.deepCopy
                modifiedBoard.move(theMove)
                val score = alphaBetaPruning(player, modifiedBoard, a, beta, currentPly)
                if (score > a) {
                    a = score.toDouble()
                    indexOfBestMove = theMove
                }
                if (a >= beta) {
                    break
                }
            }
            if (indexOfBestMove != -1) {
                board.algorithmBestMove = indexOfBestMove
            }
            return a.toInt()
        }

        /**
         * Play the move with the lowest score.
         * @param player        the player that the AI will identify as
         * @param board         the Tic Tac Toe board to play on
         * @param alpha         the alpha value
         * @param beta          the beta value
         * @param currentPly    the current depth
         * @return              the score of the move
         */
        private fun getMin(player: Board.State, board: Board, alpha: Double, beta: Double, currentPly: Int): Int {
            var b = beta
            var indexOfBestMove = -1
            for (theMove in board.availableMoves) {
                val modifiedBoard = board.deepCopy
                modifiedBoard.move(theMove)
                val score = alphaBetaPruning(player, modifiedBoard, alpha, b, currentPly)
                if (score < b) {
                    b = score.toDouble()
                    indexOfBestMove = theMove
                }
                if (alpha >= b) {
                    break
                }
            }
            if (indexOfBestMove != -1) {
                board.algorithmBestMove = indexOfBestMove
            }
            return b.toInt()
        }

        /**
         * Get the score of the board. Takes depth into account.
         * @param player        the play that the AI will identify as
         * @param board         the Tic Tac Toe board to play on
         * @param currentPly    the current depth
         * @return              the score of the move
         */
        private fun score(player: Board.State, board: Board, currentPly: Int): Int {
            require(player != Board.State.Blank) { "Player must be X or O." }
            val opponent = if (player == Board.State.X) Board.State.O else Board.State.X
            return if (board.isGameOver && board.winner == player) {
                10 - currentPly
            } else if (board.isGameOver && board.winner == opponent) {
                -10 + currentPly
            } else {
                0
            }
        }
    }

}