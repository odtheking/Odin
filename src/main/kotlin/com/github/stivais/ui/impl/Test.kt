package com.github.stivais.ui.impl

import com.github.stivais.ui.UI
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.constrain
import com.github.stivais.ui.constraints.copies
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.elements.impl.Grid
import com.github.stivais.ui.events.Lifetime
import com.github.stivais.ui.renderer.Framebuffer
import me.odinmain.OdinMain.mc
import me.odinmain.utils.skyblock.heldItem
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import org.lwjgl.opengl.GL11


fun basic() = UI {

//    Column(at(Center, Center), 5.px, 5.px).scope {
////        block(constrain(100.px, 100.px, 200.px, 200.px), Color.RED)
//        text("wtf", pos = at(x = Center), size = 20.px)
//        text("a", pos = at(x = Center), size = 20.px)
//        text("123", pos = at(x = Center), size = 20.px)
//        text("bcdef", pos = at(x = Center), size = 20.px)
//        text("ghjijk", pos = at(x = Center), size = 20.px)
//    }



//   Grid(copies()).scope {
//       repeat(4) {
//           group(size(50.percent, 50.percent)) {
//               image(
//                   "https://mc-heads.net/avatar/Stivais/128"
//               )
//           }
//       }
//   }
    val heldItem = heldItem ?: return@UI modMessage("no held item")

    val item2 = ItemStack(Item.getByNameOrId("minecraft:fireworks"))

    Grid(copies()).scope {
        repeat(5) {
            Test(constrain(w = (100 + it * 10).px, h = (100 + it * 10).px), heldItem).add()
        }
//        Test(constrain(w = 100.px, h = 100.px), heldItem).add()
//        Test(constrain(w = 100.px, h = 100.px), heldItem).add()
//        Test(constrain(w = 100.px, h = 100.px), heldItem).add()
//        Test(constrain(w = 100.px, h = 100.px), heldItem).add()
    }

}

// idk doesn t seem to work
class Test(constraints: Constraints, val item: ItemStack) : Element(constraints) {

//    var framebuffer: Framebuffer? = null
//        set(value) {
//            if (field == value) return
//            field = value
//        }

    private var framebuffer: Framebuffer? = null

    init {
        registerEvent(Lifetime.Uninitialized) {
            if (framebuffer != null) renderer.destroyFramebuffer(framebuffer!!)
            true
        }
    }

    override fun preDraw() {
        if (framebuffer == null && (width != 0f && height != 0f)) {
            framebuffer = renderer.createFramebuffer(width + x / 100f, height + y / 100f)

            GlStateManager.pushMatrix()
            renderer.push()
            renderer.bindFramebuffer(framebuffer!!)

            GlStateManager.enableDepth()    
            GlStateManager.enableAlpha()
            GlStateManager.enableBlend()
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GlStateManager.enableTexture2D()

            // setup overlay rendering
            val w = (width).toDouble()
            val h = (height).toDouble()
            GL11.glMatrixMode(GL11.GL_PROJECTION)
            GL11.glPushMatrix()
            GL11.glLoadIdentity()
            GL11.glOrtho(0.0, w, h, 0.0, 1000.0, 3000.0)
            GL11.glMatrixMode(GL11.GL_MODELVIEW)
            GL11.glPushMatrix()
            GL11.glLoadIdentity()
            GL11.glPushMatrix()
            GL11.glTranslatef(0f, 0f, -2000f)
            GL11.glScalef(5f, 5f, 1f)

            RenderHelper.enableStandardItemLighting()
            RenderHelper.enableGUIStandardItemLighting()
            mc.renderItem.renderItemIntoGUI(item, 0, 0)

            RenderHelper.disableStandardItemLighting()

            // resetoverlay renderer
            GL11.glPopMatrix()
            GL11.glPopMatrix()
            GL11.glMatrixMode(GL11.GL_PROJECTION)
            GL11.glPopMatrix()
            GL11.glMatrixMode(GL11.GL_MODELVIEW)

            GlStateManager.disableTexture2D()
            GlStateManager.disableDepth()
            GlStateManager.disableAlpha()
            GlStateManager.disableBlend()
            renderer.unbindFramebuffer()
            renderer.pop()
            GlStateManager.popMatrix()

//            var errorCode = GL11.glGetError()
//
//            while (errorCode != GL11.GL_NO_ERROR) {
//                val errorMessage = when (errorCode) {
//                    GL11.GL_INVALID_ENUM -> "INVALID_ENUM"
//                    GL11.GL_INVALID_VALUE -> "INVALID_VALUE"
//                    GL11.GL_INVALID_OPERATION -> "INVALID_OPERATION"
//                    GL11.GL_STACK_OVERFLOW -> "STACK_OVERFLOW"
//                    GL11.GL_STACK_UNDERFLOW -> "STACK_UNDERFLOW"
//                    GL11. GL_OUT_OF_MEMORY -> "OUT_OF_MEMORY"
//                    else -> "UNKNOWN_ERROR"
//                }
//                println("OpenGL error: $errorMessage ($errorCode)")
//                errorCode = GL11.glGetError()  // Continue checking for more errors
//            }
//            renderer.push()
//            renderer.bindFramebuffer(framebuffer!!)
//            renderer.beginFrame(1920f, 1080f)
//            renderer.text("hi", x, y, 20f)
//            renderer.endFrame()
//            renderer.unbindFramebuffer()
//            renderer.pop()
        }
    }

    override fun draw() {
        renderer.hollowRect(x, y, width, height, 1f, Color.WHITE.rgba)
        val fbo = framebuffer ?: return
//        renderer.translate(x, y)
        renderer.drawFramebuffer(fbo, x, y)
    }
}