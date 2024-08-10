package com.github.stivais.ui.impl.huds

import com.github.stivais.ui.UI
import com.github.stivais.ui.UIScreen.Companion.open
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.constrain
import com.github.stivais.ui.constraints.measurements.Percent
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.constraints.size
import com.github.stivais.ui.constraints.sizes.Bounding
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.elements.scope.ElementDSL
import com.github.stivais.ui.elements.scope.ElementScope
import com.github.stivais.ui.elements.scope.draggable
import com.github.stivais.ui.utils.loop
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.Display

//
class HUD(
    val defaultX: Float,
    val defaultY: Float,
    val builder: Scope.() -> Unit
) {
    var hudX: Float = defaultX
    var hudY: Float = defaultY

    init {
        HUDManager.HUDs.add(this)
    }


    fun create(scope: ElementDSL, preview: Boolean): Scope {
        val c = if (preview) size(Bounding, Bounding) else constrain(Percent(hudX), Percent(hudY), Bounding, Bounding)
        val drawable = Drawable(c)
        scope.addElement(drawable)
        val drawableScope = Scope(drawable, preview)
        drawableScope.builder()
        return drawableScope
    }

    fun reset() {
        hudX = defaultX
        hudY = defaultY
    }

    // this makes me kinda sad, but i need elements to not be centered
    internal class Drawable(constraints: Constraints) : Element(constraints) {
        override fun draw() {
        }
        override fun onElementAdded(element: Element) {
            // to stop default centering
        }
    }

    class Scope internal constructor(element: Drawable, val preview: Boolean) : ElementScope<Element>(element)
}


object HUDManager {

    val HUDs = arrayListOf<HUD>()

    var UI: UI = UI().apply { initialize(Display.getWidth(), Display.getHeight()) }

    private fun setup() {
        UI.main.createScope().apply {
            HUDs.loop { it.create(this, preview = false) }
        }
        UI.initialize(Display.getWidth(), Display.getHeight())
    }

    fun openEditor() {
        open(
            UI {
                onCreation {
                    // has to be done here for seamless transition
                    UI.empty()
                }
                HUDs.loop { hud ->
                    row(constrain(Percent(hud.hudX), Percent(hud.hudY), Bounding, Bounding)) {
                        hud.create(this, preview = true)
                        draggable()
                        onRelease {
                            hud.hudX = element.x / ui.main.width
                            hud.hudY = element.y / ui.main.height
                        }
                        block(size(20.px, 20.px), Color.RED)
                    }
                }
                onRemove {
                    setup()
                }
            }
        )
    }

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        UI.render()
    }
}
