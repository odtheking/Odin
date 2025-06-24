package me.odinmain.aurora.components

import com.github.stivais.aurora.Aurora
import com.github.stivais.aurora.animations.Animation
import com.github.stivais.aurora.color.Color
import com.github.stivais.aurora.components.Container
import com.github.stivais.aurora.components.impl.Text
import com.github.stivais.aurora.measurements.Constraints
import com.github.stivais.aurora.measurements.Measurement
import com.github.stivais.aurora.measurements.impl.Undefined
import com.github.stivais.aurora.renderer.Renderer
import com.github.stivais.aurora.renderer.data.Font
import com.github.stivais.aurora.utils.Timing
import com.github.stivais.aurora.utils.multiply

class SwappingText(
    container: Container,
    string: String,
    font: Font = Aurora.defaultFont,
    color: Color = Color.WHITE,
    position: Constraints<Measurement.Position> = Constraints(Undefined, Undefined),
    size: Measurement.Size,
) : Text(container, string, font, color, position, Constraints(Undefined, size)) {

    private var animation: Animation? = null
    private var oldWidth = 0f

    private var newString: String? = null

    override fun draw(renderer: Renderer) {
        renderer.pushScissor(x, y, width, height)
        val animation = animation
        val newString = newString
        if (animation != null && newString != null) {
            val totalProgress = animation.get()

            if (totalProgress < .6f) {
                val progress = Animation.Style.EaseInQuad.getValue(totalProgress * 2f)
                val blur = progress * 10f
                renderer.text(string, x, y, height, color.rgba.multiply(a = 1f - progress), font, blur)
            }
            if (totalProgress > .5f) {
                val progress = Animation.Style.EaseOutQuad.getValue((totalProgress * 2f) - 1f)
                val blur = (1f - progress) * 10f
                renderer.text(newString, x, y, height, color.rgba.multiply(a = progress), font, blur)
            }
        } else {
            super.draw(renderer)
        }
        renderer.popScissor()
    }

    override fun size0() {
        if (size.second.stage() == 0 && size.first === Undefined) {
            height = size.second.calculateHeight(this)

            val animation = animation
            val newString = newString
            if (animation == null || newString == null) {
                width = aurora.renderer.textWidth(string, height, font)
            } else {
                val totalProgress = animation.get() * 2f
                redraw()

                if (totalProgress > 1f) {
                    val progress = Animation.Style.EaseOutQuint.getValue(totalProgress - 1f)
                    val newWidth = aurora.renderer.textWidth(newString, height, font)
                    width = oldWidth + (newWidth - oldWidth) * progress

                    if (animation.finished) {
                        string = newString
                        this.newString = null
                        this.animation = null
                    }
                } else {
                    width = aurora.renderer.textWidth(string, height, font)
                }
            }
        } else {
            super.size0()
        }
    }

    fun animate(new: String, timing: Timing) {
        if (animation == null) {
            oldWidth = width
            animation = Animation(timing, Animation.Style.Linear)
            redraw()
        }
        newString = new
    }
}