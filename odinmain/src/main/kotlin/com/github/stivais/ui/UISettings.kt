@file:Suppress("UNUSED")

package com.github.stivais.ui

// add more customizablity
class UISettings {

    var frameMetrics: Boolean = true

    var elementMetrics: Boolean = true

    var repositionOnEvent: Boolean = true

    // don't use for uis that update a lot
    var cacheFrames: Boolean = false

}