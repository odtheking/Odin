package com.github.stivais.ui.renderer

data class Scissor(
    var x: Float,
    var y: Float,
    var width: Float,
    var height: Float
) {
    constructor(scissor: Scissor) : this(scissor.x, scissor.y, scissor.width, scissor.height)
}