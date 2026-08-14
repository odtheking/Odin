package com.odtheking.odin.features

import com.odtheking.odin.features.Category.Companion.categories

@ConsistentCopyVisibility
data class Category private constructor(val name: String, val x: Int, val y: Int) {
    companion object {

        /**
         * Map containing all the categories, with the key being the name.
         */
        val categories: LinkedHashMap<String, Category> = linkedMapOf()

        @JvmField
        val DUNGEON = custom("Dungeon", 10, 10)
        @JvmField
        val BOSS = custom("Boss", 180, 10)
        @JvmField
        val RENDER = custom("Render", 350, 10)
        @JvmField
        val SKYBLOCK = custom("Skyblock",520, 10)
        @JvmField
        val NETHER = custom("Nether",690, 10)

        /**
         * Returns a category with name provided.
         *
         * If a category with the same name has already been made, it won't reallocate.
         * Otherwise, it will be added to [categories].
         */
        fun custom(name: String, x: Int, y: Int): Category = categories.getOrPut(name) { Category(name, x, y) }
    }
}