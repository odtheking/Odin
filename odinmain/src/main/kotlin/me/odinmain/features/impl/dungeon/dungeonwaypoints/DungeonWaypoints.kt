package me.odinmain.features.impl.dungeon.dungeonwaypoints

import me.odinmain.config.DungeonWaypointConfigCLAY
import me.odinmain.events.impl.*
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.dungeon.dungeonwaypoints.SecretWaypoints.clearSecrets
import me.odinmain.features.impl.dungeon.dungeonwaypoints.SecretWaypoints.resetSecrets
import me.odinmain.features.impl.render.DevPlayers
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.*
import me.odinmain.utils.render.*
import me.odinmain.utils.render.RenderUtils.outlineBounds
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.ScanUtils
import net.minecraft.client.gui.*
import net.minecraft.util.*
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * Custom Waypoints for Dungeons
 * @author Bonsai, Azael
 */
object DungeonWaypoints : Module(
    name = "Dungeon Waypoints",
    description = "Shows waypoints for dungeons. Currently it's quite buggy and doesn't work well with some rooms.",
    category = Category.DUNGEON,
    tag = TagType.NEW
) {
    private var allowEdits: Boolean by BooleanSetting("Allow Edits", false)
    var editText: Boolean by BooleanSetting("Edit Text", false, description = "Displays text under your crosshair telling you when you are editing waypoints.")
    var color: Color by ColorSetting("Color", default = Color.GREEN, description = "The color of the next waypoint you place.", allowAlpha = true).withDependency { colorPallet == 0 }
    private val colorPallet: Int by SelectorSetting("Color pallet", "None", arrayListOf("None", "Aqua", "Magenta", "Yellow", "Lime"))
    var filled: Boolean by BooleanSetting("Filled", false, description = "If the next waypoint you place should be 'filled'.")
    var throughWalls: Boolean by BooleanSetting("Through walls", false, description = "If the next waypoint you place should be visible through walls.")
    var useBlockSize: Boolean by BooleanSetting("Use block size", false, description = "Use the size of the block you click for waypoint size.")
    var size: Double by NumberSetting("Size", 1.0, .125, 1.0, increment = 0.01, description = "The size of the next waypoint you place.").withDependency { !useBlockSize }
    var secretWaypoint: Boolean by BooleanSetting("Secret", default = false, description = "Waypoints with this setting will be removed when a secret is interacted with near this waypoint.")
    private val disableDepth: Boolean by BooleanSetting("Disable Depth", false, description = "Disables depth testing for waypoints.")
    private val resetButton: () -> Unit by ActionSetting("Reset Current Room") {
        val room = DungeonUtils.currentRoom ?: return@ActionSetting modMessage("Room not found!!!")

        val waypoints = DungeonWaypointConfigCLAY.waypoints.getOrPut(room.room.data.name) { mutableListOf() }
        if (!waypoints.removeAll { true }) return@ActionSetting modMessage("Current room does not have any waypoints!")

        DungeonWaypointConfigCLAY.saveConfig()
        ScanUtils.setWaypoints(room)
        glList = -1
        modMessage("Successfully reset current room!")
    }
    private val debugWaypoint: Boolean by BooleanSetting("Debug Waypoint", false).withDependency { DevPlayers.isDev }
    var glList = -1

    data class DungeonWaypoint(
        val x: Double, val y: Double, val z: Double,
        val color: Color, val filled: Boolean, val depth: Boolean,
        val aabb: AxisAlignedBB, val title: String?,
        val secret: Boolean, var clicked: Boolean = false
    )

    override fun onKeybind() {
        allowEdits = !allowEdits
        modMessage("Dungeon Waypoint editing ${if (allowEdits) "§aenabled" else "§cdisabled"}§r!")
    }

    init {
        onWorldLoad { resetSecrets() }

        onMessage(Regex("(?s).*(\\d+)/\\1 Secrets.*")) { clearSecrets() }
    }

    @SubscribeEvent
    fun onSecret(event: SecretPickupEvent) {
        if (!allowEdits) SecretWaypoints.onSecret(event)
    }

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if ((DungeonUtils.inBoss || !DungeonUtils.inDungeons) && !mc.theWorld.isRemote) return
        val room = DungeonUtils.currentRoom ?: return
        startProfile("Dungeon Waypoints")
        glList = RenderUtils.drawBoxes(room.waypoints.filter { !it.clicked }, glList, disableDepth)
        room.waypoints.filter { it.title != null && !it.clicked }.forEach {
            Renderer.drawStringInWorld(it.title ?: "", Vec3(it.x + 0.5, it.y + 0.5, it.z + 0.5))
        }

        if (debugWaypoint) {
            val distinct = room.positions.distinct().minByOrNull { it.core } ?: return
            Renderer.drawBox(Vec3(distinct.x.toDouble(), 70.0, distinct.z.toDouble()).toAABB(), Color.GREEN, fillAlpha = 0)
        }
        endProfile()
    }

    @SubscribeEvent
    fun onRenderOverlay(event: RenderGameOverlayEvent.Post) {
        if (mc.currentScreen != null || event.type != RenderGameOverlayEvent.ElementType.ALL || !allowEdits || !editText) return
        val sr = ScaledResolution(mc)
        scale(2f / sr.scaleFactor, 2f / sr.scaleFactor, 1f)
        mc.fontRendererObj.drawString("Editing Waypoints", mc.displayWidth / 4 - mc.fontRendererObj.getStringWidth("Editing Waypoints") / 2, mc.displayHeight / 4 + 10, Color.WHITE.withAlpha(.5f).rgba)
        scale(sr.scaleFactor / 2f, sr.scaleFactor / 2f, 1f)
    }

    @SubscribeEvent
    fun onInteract(event: ClickEvent.RightClickEvent) {
        val pos = mc.objectMouseOver?.blockPos ?: return
        if (!allowEdits || isAir(pos)) return
        val room = DungeonUtils.currentRoom ?: return
        val vec = Vec3(pos).subtractVec(x = room.clayPos.x, z = room.clayPos.z).rotateToNorth(room.room.rotation)
        val aabb =
            if (useBlockSize) getBlockAt(pos).getSelectedBoundingBox(mc.theWorld, BlockPos(0, 0, 0))?.outlineBounds() ?: return
            else AxisAlignedBB(.5 - (size / 2), .5 - (size / 2), .5 - (size / 2), .5 + (size / 2), .5 + (size / 2), .5 + (size / 2)).expand(0.002, 0.002, 0.002)

        val waypoints = DungeonWaypointConfigCLAY.waypoints.getOrPut(room.room.data.name) { mutableListOf() }

        val color = when (colorPallet) {
            0 -> color
            1 -> Color.CYAN
            2 -> Color.MAGENTA
            3 -> Color.YELLOW
            4 -> Color.GREEN
            else -> color
        }

        if (mc.thePlayer.isSneaking) {
            GuiSign.setCallback { enteredText ->
                waypoints.removeIf { it.toVec3().equal(vec) }
                waypoints.add(DungeonWaypoint(vec.xCoord, vec.yCoord, vec.zCoord, color.copy(), filled, !throughWalls, aabb, enteredText, secretWaypoint))
            }
            mc.displayGuiScreen(GuiSign)
        } else if (waypoints.removeIf { it.toVec3().equal(vec) }) {
            devMessage("Removed waypoint at $vec")
        } else {
            waypoints.add(DungeonWaypoint(vec.xCoord, vec.yCoord, vec.zCoord, color.copy(), filled, !throughWalls, aabb, "", secretWaypoint))
            devMessage("Added waypoint at $vec")
        }
        DungeonWaypointConfigCLAY.saveConfig()
        ScanUtils.setWaypoints(room)
        glList = -1
    }

    @SubscribeEvent
    fun onNewRoom(event: EnteredDungeonRoomEvent) {
        glList = -1
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