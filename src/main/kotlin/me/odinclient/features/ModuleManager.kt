package me.odinclient.features

import me.odinclient.features.dungeon.AutoWish
import me.odinclient.features.general.ClickGui
import me.odinclient.features.m7.AutoEdrag
import me.odinclient.features.qol.TermAC
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

object ModuleManager {

    val modules: ArrayList<Module> = arrayListOf(
        // TODO: Add all modules here if they extend Module
        ClickGui,
        AutoWish,
        AutoEdrag,
        TermAC
    )

    fun initializeModules() {
        modules.forEach {
            it.initializeModule()
        }
    }

    @SubscribeEvent
    fun activateModuleKeyBinds(event: InputEvent.KeyInputEvent) {
        if (Keyboard.getEventKeyState()) return
        val eventKey = Keyboard.getEventKey()
        if (eventKey == 0) return
        modules.stream().filter { it.keyCode == eventKey }.forEach { it.keyBind() }
    }

    @SubscribeEvent
    fun activateModuleMouseBinds(event: InputEvent.MouseInputEvent) {
        if (Mouse.getEventButtonState()) return
        val eventButton = Mouse.getEventButton()
        if (eventButton == 0) return
        modules.stream().filter { module -> module.keyCode + 100 == eventButton }.forEach { module -> module.keyBind() }
    }


    fun getModuleByName(name: String): Module? {
        return modules.stream().filter { module -> module.name.equals(name, ignoreCase = true)}.findFirst().orElse(null)
    }
}