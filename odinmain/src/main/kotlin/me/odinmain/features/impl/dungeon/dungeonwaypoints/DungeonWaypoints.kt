package me.odinmain.features.impl.dungeon.dungeonwaypoints

import me.odinmain.config.DungeonWaypointConfig
import me.odinmain.events.impl.ClickEvent
import me.odinmain.events.impl.DungeonEvents
import me.odinmain.events.impl.SecretPickupEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.dungeon.dungeonwaypoints.SecretWaypoints.onEtherwarp
import me.odinmain.features.impl.dungeon.dungeonwaypoints.SecretWaypoints.onLocked
import me.odinmain.features.impl.dungeon.dungeonwaypoints.SecretWaypoints.onPosUpdate
import me.odinmain.features.impl.dungeon.dungeonwaypoints.SecretWaypoints.resetSecrets
import me.odinmain.features.impl.render.DevPlayers
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.*
import me.odinmain.utils.render.*
import me.odinmain.utils.render.RenderUtils.outlineBounds
import me.odinmain.utils.render.RenderUtils.renderVec
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.getRealCoords
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.getRelativeCoords
import me.odinmain.utils.skyblock.dungeon.tiles.FullRoom
import net.minecraft.block.BlockSign
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiTextField
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.MovingObjectPosition.MovingObjectType
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.MouseEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.sign

/**
 * Custom Waypoints for Dungeons
 * @author Bonsai, Azael
 */
object DungeonWaypoints : Module(
    name = "Dungeon Waypoints",
    description = "Custom Waypoints for Dungeon Rooms.",
    category = Category.DUNGEON,
    tag = TagType.NEW
) {
    private var allowEdits by BooleanSetting("Allow Edits", false, description = "Allows you to edit waypoints.")
    private var allowMidair by BooleanSetting("Allow Midair", default = false, description = "Allows waypoints to be placed midair if they reach the end of distance without hitting a block").withDependency { allowEdits}
    private var reachColor by ColorSetting("Reach Color", default = Color(0, 255, 213, 0.43f), description = "Color of the reach box highlight.", allowAlpha = true).withDependency { allowEdits }
    private val allowTextEdit by BooleanSetting("Allow Text Edit", true, description = "Allows you to set the text of a waypoint while sneaking.")

    var editText by BooleanSetting("Edit Text", false, description = "Displays text under your crosshair telling you when you are editing waypoints.")
    private val renderTitle by BooleanSetting("Render Title", true, description = "Renders the title of the waypoint.")
    private val disableDepth by BooleanSetting("Global Depth", false, description = "Disables depth testing for all waypoints.")

    private val settingsDropDown by DropdownSetting("Next Waypoint Settings")
    var waypointType: Int by SelectorSetting("Waypoint Type", WaypointType.NONE.displayName, WaypointType.getArrayList(), description = "The type of waypoint you want to place.").withDependency { settingsDropDown }
    private val colorPallet by SelectorSetting("Color pallet", "None", arrayListOf("None", "Aqua", "Magenta", "Yellow", "Lime", "Red"), description = "The color pallet of the next waypoint you place.").withDependency { settingsDropDown }
    var color by ColorSetting("Color", default = Color.GREEN, description = "The color of the next waypoint you place.", allowAlpha = true).withDependency { colorPallet == 0 && settingsDropDown }
    var filled by BooleanSetting("Filled", false, description = "If the next waypoint you place should be 'filled'.").withDependency { settingsDropDown }
    var throughWalls by BooleanSetting("Through walls", false, description = "If the next waypoint you place should be visible through walls.").withDependency { settingsDropDown }
    var useBlockSize by BooleanSetting("Use block size", true, description = "Use the size of the block you click for waypoint size.").withDependency { settingsDropDown }
    var size: Double by NumberSetting("Size", 1.0, .125, 1.0, increment = 0.01, description = "The size of the next waypoint you place.").withDependency { !useBlockSize && settingsDropDown }
    var timerSetting: Int by SelectorSetting("Timer Type", TimerType.NONE.displayName, TimerType.getArrayList(), description = "Type of route timer you want to place.").withDependency { !waypointType.equalsOneOf(0, 1, 5) && settingsDropDown }

    private val resetButton by ActionSetting("Reset Current Room", description = "Resets the waypoints for the current room.") {
        val room = DungeonUtils.currentFullRoom ?: return@ActionSetting modMessage("§cRoom not found!")

        val waypoints = DungeonWaypointConfig.waypoints.getOrPut(room.room.data.name) { mutableListOf() }
        if (waypoints.isEmpty()) return@ActionSetting modMessage("§cCurrent room does not have any waypoints!")
        waypoints.clear()
        DungeonWaypointConfig.saveConfig()
        setWaypoints(room)
        glList = -1
        modMessage("Successfully reset current room!")
    }
    private val debugWaypoint by BooleanSetting("Debug Waypoint", false, description = "Shows a waypoint in the middle of every extra room.").withDependency { DevPlayers.isDev }

    private val selectedColor get() = when (colorPallet) {
        0 -> color
        1 -> Color.CYAN
        2 -> Color.MAGENTA
        3 -> Color.YELLOW
        4 -> Color.GREEN
        5 -> Color.RED
        else -> color
    }

    var glList = -1
    var offset = BlockPos(0.0, 0.0, 0.0)

    enum class WaypointType {
        NONE, NORMAL, SECRET, ETHERWARP, MOVE, BLOCKETHERWARP
        ;
        val displayName get() = name.capitalizeFirst()
        companion object {
            fun getArrayList() = ArrayList(entries.map { it.displayName })
            fun getByInt(i: Int) = entries.getOrNull(i).takeIf { it != NONE }
            fun getByName(name: String): WaypointType? {
                return entries.find { it.name == name.uppercase() }
            }
        }
    }

    enum class TimerType {
        NONE, START, CHECKPOINT, END,
        ;
        val displayName get() = name.capitalizeFirst()
        companion object{
            fun getType() = if (waypointType.equalsOneOf(0, 1, 5)) null else getByInt(timerSetting)
            fun getArrayList() = ArrayList(TimerType.entries.map { it.displayName })
            fun getByInt(i: Int) = TimerType.entries.getOrNull(i).takeIf { it != NONE }
            fun getByName(name: String): TimerType? {
                return TimerType.entries.find { it.name == name.uppercase() }
            }
        }
    }

    data class DungeonWaypoint(
        val x: Double, val y: Double, val z: Double,
        val color: Color, val filled: Boolean, val depth: Boolean,
        val aabb: AxisAlignedBB, val title: String? = null,
        var type: WaypointType? = null, val timer: TimerType? = null,
        @Transient var clicked: Boolean = false,
    ) {
        var secret: Boolean
            get() = type == WaypointType.SECRET
            set(value) {
                type = if (value) WaypointType.SECRET else null
            }
    }

    override fun onKeybind() {
        allowEdits = !allowEdits
        modMessage("Dungeon Waypoint editing ${if (allowEdits) "§aenabled" else "§cdisabled"}§r!")
    }

    init {
        onWorldLoad { resetSecrets() }

        onMessage("That chest is locked!", true) {
            onLocked()
        }

        onPacket(S08PacketPlayerPosLook::class.java) {
            onEtherwarp(it)
        }

        onPacket(C04PacketPlayerPosition::class.java) {
            onPosUpdate(Vec3(it.positionX, it.positionY, it.positionZ))
        }

        onPacket(C06PacketPlayerPosLook::class.java) {
            onPosUpdate(Vec3(it.positionX, it.positionY, it.positionZ))
        }
    }

    @SubscribeEvent
    fun onSecret(event: SecretPickupEvent) {
        if (!allowEdits) SecretWaypoints.onSecret(event)
    }

    private var reachPos: EtherWarpHelper.EtherPos? = null
    var distance = 5.0
    var lastEtherPos: EtherWarpHelper.EtherPos? = null
    var lastEtherTime = 0L

    private inline val reachPosition: BlockPos? get() =
        mc.objectMouseOver?.takeUnless { it.typeOfHit == MovingObjectType.MISS || (distance <= 4.5 && allowMidair) }?.blockPos ?: reachPos?.pos

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if ((DungeonUtils.inBoss || !DungeonUtils.inDungeons) && !LocationUtils.currentArea.isArea(Island.SinglePlayer)) return
        val room = DungeonUtils.currentFullRoom ?: return
        startProfile("Dungeon Waypoints")
        glList = RenderUtils.drawBoxes(room.waypoints, glList, disableDepth)
        if (renderTitle) {
            for (waypoint in room.waypoints) {
                if (waypoint.clicked) continue
                Renderer.drawStringInWorld(waypoint.title ?: continue, Vec3(waypoint.x + 0.5, waypoint.y + 0.5, waypoint.z + 0.5), depth = waypoint.depth)
            }
        }

        if (debugWaypoint) {
            room.components.forEach {
                Renderer.drawBox(Vec3(it.x.toDouble(), 70.0, it.z.toDouble()).toAABB(), Color.GREEN, fillAlpha = 0)
            }
        }
        endProfile()


        if (allowEdits) {
            reachPos = EtherWarpHelper.getEtherPos(mc.thePlayer.renderVec, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, distance, allowMidair)
            reachPosition?.let {
                if (useBlockSize) Renderer.drawStyledBlock(it, reachColor, style = if (filled) 0 else 1, 1, !throughWalls)
                else Renderer.drawStyledBox(AxisAlignedBB(it.x + 0.5, it.y + .5, it.z + .5, it.x + .5, it.y + .5, it.z + .5).outlineBounds(), reachColor, style = if (filled) 0 else 1, 1, !throughWalls)
            }
        }
    }

    @SubscribeEvent
    fun onRenderOverlay(event: RenderGameOverlayEvent.Post) {
        if (mc.currentScreen != null || event.type != RenderGameOverlayEvent.ElementType.ALL || !allowEdits || !editText) return
        val sr = ScaledResolution(mc)
        val pos = reachPosition
        val (text, editText) = pos?.add(offset)?.let {
            val room = DungeonUtils.currentFullRoom ?: return
            val vec = room.getRelativeCoords(it.add(offset).toVec3())
            val waypoint = getWaypoints(room).find { it.toVec3().equal(vec) }

            val text = waypoint?.let {"§fType: §5${waypoint.type?.displayName ?: "None"}${waypoint.timer?.let { "§7, §fTimer: §a${it.displayName}" } ?: ""}" }
                ?: "§fType: §5${WaypointType.getByInt(waypointType)?.displayName ?: "None"}§7, §r#${selectedColor.hex}§7, ${if (filled) "§2Filled" else "§3Outline"}§7, ${if (throughWalls) "§cThrough Walls§7, " else ""}${if (useBlockSize) "§2Block Size" else "§3Size: $size"}${TimerType.getType()?.let { "§7, §fTimer: §a${it.displayName}" } ?: ""}"


            text to "§fEditing Waypoints §8|§f ${waypoint?.let { "Viewing" } ?: "Placing"}"
        } ?: ("" to "Editing Waypoints")

        scale(2f / sr.scaleFactor, 2f / sr.scaleFactor, 1f)
        mcText(editText, mc.displayWidth / 4, mc.displayHeight  / 4 + 10, 1f, Color.WHITE.withAlpha(.8f))
        mcText(text,mc.displayWidth / 4,  mc.displayHeight / 4 + 20, 1f, selectedColor)
        scale(sr.scaleFactor / 2f, sr.scaleFactor / 2f, 1f)
    }

    @SubscribeEvent
    fun onMouseInput(event: MouseEvent) {
        if (allowEdits && event.dwheel.sign != 0) {
            distance = (distance + event.dwheel.sign).coerceAtLeast(0.0)
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onInteract(event: ClickEvent.RightClickEvent) {
        if (mc.thePlayer.usingEtherWarp) {
            val pos = EtherWarpHelper.getEtherPos(mc.thePlayer.renderVec, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)
            if (pos.succeeded && pos.pos != null) {
                if (DungeonUtils.currentFullRoom?.waypoints?.any { pos.vec?.equal(it.toVec3()) == true && (it.type == WaypointType.BLOCKETHERWARP) } == true) {
                    event.isCanceled = true
                    return
                }
                lastEtherPos = pos
                lastEtherTime = System.currentTimeMillis()
            }
        }
        if (!allowEdits) return
        val pos = reachPosition ?: return
        val offsetPos = pos.add(offset)
        offset = BlockPos(0.0, 0.0, 0.0)
        if (isAir(offsetPos) && !allowMidair) return
        val room = DungeonUtils.currentFullRoom ?: return
        val vec = room.getRelativeCoords(offsetPos.toVec3())
        val block = getBlockAt(offsetPos)
        val aabb =
            if (useBlockSize && block !is BlockSign) block.getSelectedBoundingBox(mc.theWorld, BlockPos(0, 0, 0))?.outlineBounds() ?: return
            else AxisAlignedBB(.5 - (size / 2), .5 - (size / 2), .5 - (size / 2), .5 + (size / 2), .5 + (size / 2), .5 + (size / 2)).expand(0.002, 0.002, 0.002)

        val waypoints = getWaypoints(room)

        val type = WaypointType.getByInt(waypointType)
        val timer = TimerType.getType()

        if (allowTextEdit && mc.thePlayer?.isSneaking == true) {
            GuiSign.setCallback { enteredText ->
                waypoints.removeIf { it.toVec3().equal(vec) }
                waypoints.add(DungeonWaypoint(vec.xCoord, vec.yCoord, vec.zCoord, selectedColor.copy(), filled, !throughWalls, aabb, enteredText, type, timer))
                DungeonWaypointConfig.saveConfig()
                setWaypoints(room)
                glList = -1
            }
            mc.displayGuiScreen(GuiSign)
        } else if (waypoints.removeIf { it.toVec3().equal(vec) }) {
            devMessage("Removed waypoint at $vec")
        } else {
            waypoints.add(DungeonWaypoint(vec.xCoord, vec.yCoord, vec.zCoord, selectedColor.copy(), filled, !throughWalls, aabb, type = type, timer = timer))
            devMessage("Added waypoint at $vec")
        }
        DungeonWaypointConfig.saveConfig()
        setWaypoints(room)
        glList = -1
    }

    @SubscribeEvent
    fun onNewRoom(event: DungeonEvents.RoomEnterEvent) {
        glList = -1
        event.fullRoom?.let { setWaypoints(it) }
    }

    fun DungeonWaypoint.toVec3() = Vec3(x, y, z)
    fun DungeonWaypoint.toBlockPos() = BlockPos(x, y, z)

    fun DungeonWaypoint.toAABB(size: Double): AxisAlignedBB = AxisAlignedBB(
        x + .5 - (size / 2),
        y + .5 - (size / 2),
        z + .5 - (size / 2),
        x + .5 + (size / 2),
        y + .5 + (size / 2),
        z + .5 + (size / 2)
    ).expand(.01, .01, .01)

    /**
     * Sets the waypoints for the current room.
     */
    fun setWaypoints(curRoom: FullRoom) {
        curRoom.waypoints = arrayListOf<DungeonWaypoint>().apply {
            DungeonWaypointConfig.waypoints[curRoom.room.data.name]?.let { waypoints ->
                addAll(waypoints.map { waypoint ->
                    val vec = curRoom.getRealCoords(waypoint.toVec3())
                    waypoint.copy(x = vec.xCoord, y = vec.yCoord, z = vec.zCoord)
                })
            }
        }
    }

    fun getWaypoints(room: FullRoom) : MutableList<DungeonWaypoint> {
        return DungeonWaypointConfig.waypoints.getOrPut(room.room.data.name) { mutableListOf() }
    }
}

object GuiSign : GuiScreen() {
    private lateinit var textField: GuiTextField
    private var callback: (String) -> Unit = {}

    override fun initGui() {
        super.initGui()
        textField = GuiTextField(0, fontRendererObj, width / 2 - 50, height / 2 - 10, 100, 20)
        textField.text = "Enter text"
        textField.isFocused = true // Set the text field to be focused initially
        textField.maxStringLength = 50 // Maximum characters allowed
        buttonList.add(GuiButton(0, width / 2 - 50, height / 2 + 20, 100, 20, "Submit"))
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.drawScreen(mouseX, mouseY, partialTicks)
        textField.drawTextBox()
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        super.keyTyped(typedChar, keyCode)
        textField.textboxKeyTyped(typedChar, keyCode)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        super.mouseClicked(mouseX, mouseY, mouseButton)
        textField.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun actionPerformed(button: GuiButton) {
        super.actionPerformed(button)
        if (button.id != 0) return
        val enteredText = textField.text
        callback.invoke(enteredText)
        mc.displayGuiScreen(null)
    }

    // Method to set the callback function
    fun setCallback(callback: (String) -> Unit) {
        this.callback = callback
    }
}