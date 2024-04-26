package me.odinclient.utils.extras

import net.minecraft.block.Block
import net.minecraft.block.properties.IProperty
import net.minecraft.block.state.IBlockState

object ExtrasHelper {
    fun getStateFromString(blockStateString: String): IBlockState? {
        if (!blockStateString.contains("[")) {
            return Block.getBlockFromName(blockStateString).defaultState
        }

        val parts = blockStateString.split("\\[|\\]".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val block = Block.getBlockFromName(parts[0]) ?: return null

        val blockState = arrayOf(block.defaultState)
        for (property in parts[1].split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            val propertyParts = property.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            blockState[0].properties.keys.stream()
                .filter { prop: IProperty<*> ->
                    prop.name == propertyParts[0]
                }
                .findFirst()
                .ifPresent { prop: IProperty<*> ->
                    prop.allowedValues.stream()
                        .filter { value -> value.toString() == propertyParts[1] }
                        .findFirst()
                        .ifPresent { value ->
                            blockState[0] = blockState[0]
                                .withProperty(prop, value)
                        }
                }
        }
        return blockState[0]
    }
}

private fun IBlockState.withProperty(iProperty: IProperty<*>, value: Comparable<Nothing>?): IBlockState? {
    TODO("Not yet implemented")
}
