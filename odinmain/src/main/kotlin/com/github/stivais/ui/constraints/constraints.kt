package com.github.stivais.ui.constraints

import com.github.stivais.ui.elements.Element

class Constraints(var x: Position, var y: Position, var width: Size, var height: Size)

// todo: reduce interface usages (however im not sure if its possible)
interface Constraint {
    fun get(element: Element, type: Type): Float

    fun reliesOnChild() = false

    companion object {
        const val HORIZONTAL: Int = 0
        const val VERTICAL: Int = 1
    }
}

interface Position : Constraint

interface Size : Constraint

interface Measurement : Position, Size

enum class Type {
    X, Y, W, H;

    inline val axis: Int
        get() = when (this) {
            X, W -> 0
            Y, H -> 1
        }


    inline val isPosition: Boolean
        get() = ordinal < 2
}