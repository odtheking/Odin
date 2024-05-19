package com.github.stivais.ui.elements.scope

fun ElementScope<*>.afterInit(block: () -> Unit) {
    if (element.initialized) {
        if (ui.afterInit == null) ui.afterInit = arrayListOf()
        ui.afterInit!!.add(block)
    } else {
        onInitialization {
            if (ui.afterInit == null) ui.afterInit = arrayListOf()
            ui.afterInit!!.add(block)
        }
    }
}