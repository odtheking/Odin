package me.odinclient.ui.waypoint

import cc.polyfrost.oneconfig.renderer.NanoVGHelper
import cc.polyfrost.oneconfig.renderer.font.Fonts
import cc.polyfrost.oneconfig.utils.dsl.*
import me.odinclient.OdinClient.Companion.waypointConfig
import me.odinclient.features.impl.general.WaypointManager
import me.odinclient.ui.waypoint.elements.AreaButton
import me.odinclient.ui.waypoint.elements.WaypointElement
import me.odinclient.utils.VecUtils.floored
import me.odinclient.utils.gui.GuiUtils.scaleFactor
import me.odinclient.utils.gui.GuiUtils.scaleWithMouse
import me.odinclient.utils.gui.GuiUtils.scaledHeight
import me.odinclient.utils.gui.GuiUtils.scaledWidth
import me.odinclient.utils.gui.GuiUtils.scissor
import me.odinclient.utils.gui.GuiUtils.translateWithMouse
import me.odinclient.utils.gui.MouseHandler
import me.odinclient.utils.gui.animations.impl.EaseInOut
import me.odinclient.utils.gui.animations.impl.LinearAnimation
import me.odinclient.utils.skyblock.LocationUtils.currentArea
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Mouse
import java.awt.Color
import java.io.IOException
import java.util.*
import kotlin.math.sign

object WaypointGUI : GuiScreen() {
    var displayArea: String? = null // rename
    var list = mutableListOf<WaypointElement>()

    private var scrollTarget = 0f // idk a better name
    private var scrollOffset = 0f
    private val scrollAnimation = LinearAnimation(200)

    private var areaTarget = 10f
    private var areaOffset = 100f
    private val areaAnimation = LinearAnimation(200)

    var mouseHandler = MouseHandler()

    override fun initGui() {
        displayArea = currentArea
        displayArea?.let { updateElements(it) }
        scrollTarget = 0f
        scrollOffset = 0f
        areas = areas.sortedByDescending { waypointConfig.waypoints[it.area]?.size }
        super.initGui()
    }

    private var settingMenu = false
    private val settingAnimation = EaseInOut(250)

    private var drawingAreas = false

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        nanoVG {
            translateWithMouse(mouseHandler, scaledWidth / 4f, scaledHeight / 4f)
            scaleWithMouse(mouseHandler, scaleFactor, scaleFactor)

            drawDropShadow(0, 0, 480, 264, 10f, 1f, 10f)
            drawRoundedRectVaried(0, 25, 480, 239, Color(21, 22, 23, 235).rgb, 0, 0, 10, 10)

            scissor(0f, 25f, 480f, 239f) {
                scrollOffset = scrollAnimation.get(scrollOffset, scrollTarget)
                var currentY = 35f - scrollOffset
                for (waypoint in list) {
                    waypoint.y = currentY
                    currentY += waypoint.drawScreen(this)
                }
            }

            val animY = settingAnimation.get(25f, 50f, !settingMenu)

            drawRoundedRectVaried(0, 0, 480, animY, Color(21, 22, 23).rgb, 10, 10, 0, 0)
            drawLine(0, animY, 480, animY, 1.5, Color(30, 32, 34).rgb)

            drawingAreas = animY != 25f
            if (drawingAreas) {
                scissor(0f, 25f, 480f, 50f) {

                    areaOffset = areaAnimation.get(areaOffset, areaTarget)

                    var currentX = areaOffset
                    for (area in areas) {
                        area.set(currentX, animY - 11f)
                        area.draw(this)
                        drawLine(currentX - 5, animY - 7, currentX - 5, animY + 7, 0.7, -1)
                        currentX += area.draw(this)
                    }
                }
            }

            drawRoundedRectVaried(0, 0,  480, 25, Color(21, 22, 23).rgb, 10, 10, 0, 0)
            drawLine(0, 25, 480, 25, 1.5, Color(30, 32, 34).rgb)

            drawText("Add Waypoint", 16, 13.25, Color.LIGHT_GRAY.rgb, 10, Fonts.REGULAR)
            val buttonColor = if (mouseHandler.isAreaHovered(10f, 5f, 78.5f, 15f)) Color(38, 40, 42) else Color(30, 32, 34)
            drawHollowRoundedRect(10, 5, 78, 15, 5, buttonColor.rgb, 0.75)

            val color = if (mouseHandler.isAreaHovered(455f, 5f, 15f, 15f)) Color.LIGHT_GRAY.rgb else Color.WHITE.rgb
            translate(462.5f, 12.5f)
            NanoVGHelper.INSTANCE.rotate(this.instance, Math.toRadians((animY - 25.0) * 12.0).toFloat())
            translate(-462.5f, -12.5f)
            drawSVG("/assets/odinclient/Settings.svg", 455, 5, 15, 15, color, 36, javaClass)
        }
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseHandler.isAreaHovered(455f, 5f, 15f, 15f)) {
            if (settingAnimation.start()) settingMenu = !settingMenu
            return
        }
        if (drawingAreas && mouseHandler.isAreaHovered(0f, 25f, 480f, 25f)) {
            for (area in areas) {
                if (!area.mouseClicked()) continue
                displayArea = area.area
                updateElements(area.area)
                return
            }
            return
        }
        if (mouseHandler.isAreaHovered(10f, 5f, 78.5f, 15f)) {
            val randomColor = Random().run { Color(nextInt(255), nextInt(255), nextInt(255)) }
            val waypoint = WaypointManager.Waypoint("§fWaypoint", mc.thePlayer.positionVector.floored(), randomColor)
            WaypointManager.addWaypoint(waypoint, displayArea ?: "")
            list.add(WaypointElement(waypoint))
            return
        }
        for (i in list) if (i.mouseClicked(mouseButton)) break

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        for (i in list) i.keyTyped(typedChar, keyCode)
        super.keyTyped(typedChar, keyCode)
    }

    @Throws(IOException::class)
    override fun handleMouseInput() {
        super.handleMouseInput()
        if (Mouse.getEventDWheel() != 0) {
            val amount = Mouse.getEventDWheel().sign * -16
            if (drawingAreas && mouseHandler.isAreaHovered(0f, 25f, 480f, 25f)) {
                areaTarget = (areaTarget + amount).coerceAtMost(10f).coerceAtLeast(292f - areas.sumOf { it.width.toInt() })
                areaAnimation.start(true)
            } else {
                scrollTarget = (scrollTarget + amount).coerceAtMost(-229 + list.size * 40f).coerceAtLeast(0f)
                scrollAnimation.start(true)
            }
        }
    }

    fun updateElements(area: String = currentArea ?: "") {
        list = waypointConfig.waypoints[area]?.map { WaypointElement(it) }?.toMutableList() ?: mutableListOf()
    }

    override fun doesGuiPauseGame(): Boolean = false

    private var areas = listOf(
        AreaButton("The Park", mouseHandler),
        AreaButton("Hub", mouseHandler),
        AreaButton("Dungeon Hub", mouseHandler),
        AreaButton("Garden", mouseHandler),
        AreaButton("Private Island", mouseHandler),
        AreaButton("The Farming Islands", mouseHandler),
        AreaButton("Golden Mine", mouseHandler),
        AreaButton("Deep Caverns", mouseHandler),
        AreaButton("Dwarven Mines", mouseHandler),
        AreaButton("Crystal Hollows", mouseHandler),
        AreaButton("Crimson Isle", mouseHandler),
        AreaButton("Spider's Den", mouseHandler),
        AreaButton("The End", mouseHandler),
        AreaButton("Catacombs", mouseHandler),
        AreaButton("Dungeon Boss", mouseHandler),
        AreaButton("P1", mouseHandler),
        AreaButton("P2", mouseHandler),
        AreaButton("P3", mouseHandler),
        AreaButton("P4", mouseHandler),
        AreaButton("P5", mouseHandler),
    )
}
