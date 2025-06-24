package me.odinmain.utils.ui.screens

// Custom class to allow simple custom GUIs to be created without the need for a full GUI system
//class UIChest(private val ui: AuroraUI): Window {
//
//    private var previousWidth: Int = 0
//    private var previousHeight: Int = 0
//
//    fun open(init: Boolean = true) {
//        if (init) {
//            ui.initialize(Display.getWidth(), Display.getHeight(), this)
//        }
//        MinecraftForge.EVENT_BUS.register(this)
//    }
//
//    fun close() {
//        ui.close()
//        MinecraftForge.EVENT_BUS.unregister(this)
//    }
//
//    @SubscribeEvent
//    fun onRender(event: GuiEvent.DrawGuiContainerScreenEvent) {
//        event.isCanceled = true
//        val w = mc.framebuffer.framebufferWidth
//        val h = mc.framebuffer.framebufferHeight
//        if (w != previousWidth || h != previousHeight) {
//            ui.resize(w, h)
//            previousWidth = w
//            previousHeight = h
//        }
//
//        val mx = Mouse.getX().toFloat()
//        val my = previousHeight - Mouse.getY() - 1f
//        ui.eventManager.onMouseMove(mx, my)
//
//        ui.render()
//    }
//
//    @SubscribeEvent
//    fun onMouseClick(event: GuiScreenEvent.MouseInputEvent.Pre) {
//        if (Mouse.getEventButtonState()) ui.eventManager.onMouseClick(Mouse.getEventButton())
//    }
//
//    @SubscribeEvent
//    fun onMouseReleased(event: GuiEvent.GuiMouseReleaseEvent) {
//        ui.eventManager.onMouseRelease(event.button)
//    }
//
//    @SubscribeEvent
//    fun onKeyboardClick(event: GuiScreenEvent.MouseInputEvent.Pre) {
//        ui.eventManager.onKeyTyped(Keyboard.getEventCharacter())
//    }
//
//    @SubscribeEvent
//    fun onGuiOpen(event: GuiOpenEvent) {
//        if (event.gui == null) close()
//    }
//
//    override fun getClipboard(): String? {
//        return getClipboardString()
//    }
//
//    override fun setClipboard(string: String?) {
//        return setClipboardString(string)
//    }
//}