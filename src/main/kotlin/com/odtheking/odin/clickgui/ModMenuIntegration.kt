package com.odtheking.odin.clickgui

import com.odtheking.odin.clickgui.settings.ClickGUI
import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi

class ModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> = ConfigScreenFactory { ClickGUI }
}