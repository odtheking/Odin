package me.odinmain.features.impl.dungeon.dungeonwaypoints

import me.odinmain.config.DungeonWaypointConfig
import me.odinmain.events.impl.ClickEvent
import me.odinmain.events.impl.RoomEnterEvent
import me.odinmain.events.impl.SecretPickupEvent
import me.odinmain.features.Module
import me.odinmain.features.impl.dungeon.dungeonwaypoints.SecretWaypoints.onEtherwarp
import me.odinmain.features.impl.dungeon.dungeonwaypoints.SecretWaypoints.onLocked
import me.odinmain.features.impl.dungeon.dungeonwaypoints.SecretWaypoints.onPosUpdate
import me.odinmain.features.impl.dungeon.dungeonwaypoints.SecretWaypoints.resetSecrets
import me.odinmain.features.impl.render.RandomPlayers
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.*
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.render.RenderUtils.outlineBounds
import me.odinmain.utils.render.RenderUtils.renderVec
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.render.getMCTextHeight
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.getRealCoords
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.getRelativeCoords
import me.odinmain.utils.skyblock.dungeon.tiles.Room
import me.odinmain.utils.ui.Colors
import me.odinmain.utils.ui.clickgui.util.ColorUtil.withAlpha
import net.minecraft.block.BlockSign
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiTextField
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
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
    desc = "Custom Waypoints for Dungeon Rooms."
) {
    private var allowEdits by BooleanSetting("Allow Edits", false, desc = "Allows you to edit waypoints.")
    private var allowMidair by BooleanSetting("Allow Midair", false, desc = "Allows waypoints to be placed midair if they reach the end of distance without hitting a block.").withDependency { allowEdits }
    private var reachColor by ColorSetting("Reach Color", Color(0, 255, 213, 0.43f), desc = "Color of the reach box highlight.", allowAlpha = true).withDependency { allowEdits }
    private val allowTextEdit by BooleanSetting("Allow Text Edit", true, desc = "Allows you to set the text of a waypoint while sneaking.")

    private val renderTitle by BooleanSetting("Render Title", true, desc = "Renders the titles of waypoints")
    private val titleScale by NumberSetting("Title Scale", 1f, 0.1f, 4f, increment = 0.1f, desc = "The scale of the titles of waypoints.").withDependency { renderTitle }
    private val disableDepth by BooleanSetting("Global Depth", false, desc = "Disables depth testing for all waypoints.")

    private val settingsDropDown by DropdownSetting("Next Waypoint Settings")
    var waypointType by SelectorSetting("Waypoint Type", WaypointType.NONE.displayName, WaypointType.getArrayList(), desc = "The type of waypoint you want to place.").withDependency { settingsDropDown }
    private val colorPallet by SelectorSetting("Color pallet", "None", arrayListOf("None", "Aqua", "Magenta", "Yellow", "Lime", "Red"), desc = "The color pallet of the next waypoint you place.").withDependency { settingsDropDown }
    var color by ColorSetting("Color", Colors.MINECRAFT_GREEN, desc = "The color of the next waypoint you place.", allowAlpha = true).withDependency { colorPallet == 0 && settingsDropDown }
    var filled by BooleanSetting("Filled", false, desc = "If the next waypoint you place should be 'filled'.").withDependency { settingsDropDown }
    var throughWalls by BooleanSetting("Through walls", false, desc = "If the next waypoint you place should be visible through walls.").withDependency { settingsDropDown }
    var useBlockSize by BooleanSetting("Use block size", true, desc = "Use the size of the block you click for waypoint size.").withDependency { settingsDropDown }
    var size by NumberSetting("Size", 1.0, .125, 1.0, increment = 0.01, desc = "The size of the next waypoint you place.").withDependency { !useBlockSize && settingsDropDown }
    var timerSetting by SelectorSetting("Timer Type", TimerType.NONE.displayName, TimerType.getArrayList(), desc = "Type of route timer you want to place.").withDependency { !waypointType.equalsOneOf(0, 1, 5) && settingsDropDown }

    private val resetButton by ActionSetting("Reset Current Room", desc = "Resets the waypoints for the current room.") {
        val room = DungeonUtils.currentRoom ?: return@ActionSetting modMessage("§cRoom not found!")

        val waypoints = DungeonWaypointConfig.waypoints.getOrPut(room.data.name) { mutableListOf() }
        if (waypoints.isEmpty()) return@ActionSetting modMessage("§cCurrent room does not have any waypoints!")
        waypoints.clear()
        DungeonWaypointConfig.saveConfig()
        setWaypoints(room)
        glList = -1
        modMessage("Successfully reset current room!")
    }
    private val debugWaypoint by BooleanSetting("Debug Waypoint", false, desc = "Shows a waypoint in the middle of every extra room.").withDependency { RandomPlayers.isDev }

    private inline val selectedColor get() = when (colorPallet) {
        0 -> color
        1 -> Colors.MINECRAFT_DARK_AQUA
        2 -> Colors.MINECRAFT_DARK_PURPLE
        3 -> Colors.MINECRAFT_YELLOW
        4 -> Colors.MINECRAFT_GREEN
        5 -> Colors.MINECRAFT_RED
        else -> color
    }

    var glList = -1
    var offset = BlockPos(0.0, 0.0, 0.0)

    enum class WaypointType {
        NONE, NORMAL, SECRET, ETHERWARP, MOVE, BLOCKETHERWARP
        ;
        inline val displayName get() = name.lowercase().capitalizeFirst()
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
        inline val displayName get() = name.lowercase().capitalizeFirst()
        companion object{
            fun getType() = if (waypointType.equalsOneOf(0, 1, 5)) null else getByInt(timerSetting)
            fun getArrayList() = ArrayList(TimerType.entries.map { it.displayName })
            private fun getByInt(i: Int) = TimerType.entries.getOrNull(i).takeIf { it != NONE }
            fun getByName(name: String): TimerType? = TimerType.entries.find { it.name == name.uppercase() }
        }
    }

    data class DungeonWaypoint(
        val x: Double, val y: Double, val z: Double,
        val color: Color, val filled: Boolean, val depth: Boolean,
        val aabb: AxisAlignedBB, val title: String? = null,
        var type: WaypointType? = null, val timer: TimerType? = null,
        @Transient var clicked: Boolean = false,
    ) {
        inline var secret: Boolean
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

        onMessage(Regex("That chest is locked!")) {
            onLocked()
        }

        onPacket<S08PacketPlayerPosLook> {
            onEtherwarp(it)
        }

        onPacket<C04PacketPlayerPosition> {
            onPosUpdate(Vec3(it.positionX, it.positionY, it.positionZ))
        }

        onPacket<C06PacketPlayerPosLook> {
            onPosUpdate(Vec3(it.positionX, it.positionY, it.positionZ))
        }
    }

    @SubscribeEvent
    fun onSecret(event: SecretPickupEvent) {
        if (!allowEdits) SecretWaypoints.onSecret(event)
    }

    var distance = 5.0
    var lastEtherPos: EtherWarpHelper.EtherPos? = null
    var lastEtherTime = 0L

    private inline val reachPosition: BlockPos? get() =
        mc.objectMouseOver?.takeUnless { it.typeOfHit == MovingObjectType.MISS || (distance <= 4.5 && allowMidair) }?.blockPos ?: EtherWarpHelper.getEtherPos(mc.thePlayer.renderVec, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, distance, allowMidair).pos

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (DungeonUtils.inBoss || !DungeonUtils.inDungeons) return
        val room = DungeonUtils.currentRoom ?: return
        startProfile("Dungeon Waypoints")
        glList = RenderUtils.drawBoxes(room.waypoints, glList, disableDepth)
        if (renderTitle) {
            for (waypoint in room.waypoints) {
                if (waypoint.clicked) continue
                Renderer.drawStringInWorld(waypoint.title ?: continue, Vec3(waypoint.x + 0.5, waypoint.y + 0.5 + getMCTextHeight() * 0.015 * titleScale, waypoint.z + 0.5), depth = waypoint.depth, scale = 0.03f * titleScale)
            }
        }

        if (debugWaypoint) {
            room.roomComponents.forEach {
                Renderer.drawBox(Vec3(it.x.toDouble(), 70.0, it.z.toDouble()).toAABB(), Colors.MINECRAFT_GREEN, fillAlpha = 0)
            }
        }
        endProfile()

        reachPosition?.takeIf { allowEdits }?.let {
            if (useBlockSize) Renderer.drawStyledBlock(it, reachColor, style = if (filled) 0 else 1, 1, !throughWalls)
            else Renderer.drawStyledBox(AxisAlignedBB(it.x + 0.5, it.y + .5, it.z + .5, it.x + .5, it.y + .5, it.z + .5).outlineBounds(), reachColor, style = if (filled) 0 else 1, 1, !throughWalls)
        }
    }

    @SubscribeEvent
    fun onRenderOverlay(event: RenderGameOverlayEvent.Post) {
        if (mc.currentScreen != null || event.type != RenderGameOverlayEvent.ElementType.ALL || !allowEdits) return
        val sr = ScaledResolution(mc)
        val pos = reachPosition
        val (text, editText) = pos?.add(offset)?.let { position ->
            val room = DungeonUtils.currentRoom ?: return
            val vec = room.getRelativeCoords(position.add(offset).toVec3())
            val waypoint = getWaypoints(room).find { it.toVec3().equal(vec) }

            val text = waypoint?.let {"§fType: §5${waypoint.type?.displayName ?: "None"}${waypoint.timer?.let { "§7, §fTimer: §a${it.displayName}" } ?: ""}, §r#${waypoint.color.hex}§7" }
                ?: "§fType: §5${WaypointType.getByInt(waypointType)?.displayName ?: "None"}§7, §r#${selectedColor.hex}§7, ${if (filled) "§2Filled" else "§3Outline"}§7, ${if (throughWalls) "§cThrough Walls§7, " else ""}${if (useBlockSize) "§2Block Size" else "§3Size: $size"}${TimerType.getType()?.let { "§7, §fTimer: §a${it.displayName}" } ?: ""}"

            text to "§fEditing Waypoints §8|§f ${waypoint?.let { "Viewing" } ?: "Placing"}"
        } ?: ("" to "Editing Waypoints")

        GlStateManager.scale(2f / sr.scaleFactor, 2f / sr.scaleFactor, 1f)
        RenderUtils.drawText(editText, mc.displayWidth / 4f, mc.displayHeight  / 4f + 10, 1f, Colors.WHITE.withAlpha(.8f), center = true)
        RenderUtils.drawText(text, mc.displayWidth / 4f,  mc.displayHeight / 4f + 20, 1f, selectedColor, center = true)
        GlStateManager.scale(sr.scaleFactor / 2f, sr.scaleFactor / 2f, 1f)
    }

    @SubscribeEvent
    fun onMouseInput(event: MouseEvent) {
        if (!allowEdits || event.dwheel.sign == 0 || DungeonUtils.currentRoom == null) return
        distance = (distance + event.dwheel.sign).coerceIn(0.0, 100.0)
        event.isCanceled = true
    }

    @SubscribeEvent
    fun onInteract(event: ClickEvent.Right) {
        if (mc.thePlayer.usingEtherWarp) {
            val pos = EtherWarpHelper.getEtherPos(mc.thePlayer.renderVec, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)
            if (pos.succeeded && pos.pos != null) {
                if (DungeonUtils.currentRoom?.waypoints?.any { pos.vec?.equal(it.toVec3()) == true && (it.type == WaypointType.BLOCKETHERWARP) } == true) {
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
        val room = DungeonUtils.currentRoom ?: return
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
    fun onNewRoom(event: RoomEnterEvent) {
        glList = -1
        event.room?.let { setWaypoints(it) }
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
    fun setWaypoints(curRoom: Room) {
        curRoom.waypoints = mutableSetOf<DungeonWaypoint>().apply {
            DungeonWaypointConfig.waypoints[curRoom.data.name]?.let { waypoints ->
                addAll(waypoints.map { waypoint ->
                    val vec = curRoom.getRealCoords(waypoint.toVec3())
                    waypoint.copy(x = vec.xCoord, y = vec.yCoord, z = vec.zCoord)
                })
            }
        }
    }

    fun getWaypoints(room: Room) : MutableList<DungeonWaypoint> =
        DungeonWaypointConfig.waypoints.getOrPut(room.data.name) { mutableListOf() }
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