package me.odinclient.utils.extras

import net.minecraft.util.BlockPos

// TODO: an importer from floppa and fme will be added in the future
data class ExtrasData(
    // Room or region name because using core would make it harder to update the room search algorithm
    val baseName: String,
    val extraCategories: MutableList<ExtrasCategory> = mutableListOf()
)

data class ExtrasCategory(
    // name that acts as kind of id base id is "base" if the user didn't specify one if a "id" already exists we just add a number to the end
    val id: String,
    // only creates the blocks if enabled
    val enabled: Boolean,
    // if online isn't "none" then it will try to load the category from the url
    // in the url it should just be a config file in a raw format it will then try to search for the id in the config file
    // and if it finds it will load/update the category
    // also by using a command u can fully import it from an url
    val online: String,
    // blockStateString, list of blockPos
    val extras: MutableMap<String, MutableSet<BlockPos>> = mutableMapOf()
)