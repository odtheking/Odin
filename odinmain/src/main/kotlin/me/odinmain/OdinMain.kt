package me.odinmain

object OdinMain {
    val mc: Minecraft = Minecraft.getMinecraft()

    const val VERSION = "1.1.3"
    var NAME: String = "Odin"
    val scope = CoroutineScope(EmptyCoroutineContext)

    var display: GuiScreen? = null

}