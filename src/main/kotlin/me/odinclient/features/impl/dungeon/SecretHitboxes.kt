package me.odinclient.features.impl.dungeon

import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import net.minecraft.block.Block
import net.minecraft.block.BlockButton
import net.minecraft.block.BlockLever.EnumOrientation
import net.minecraft.block.state.IBlockState
import net.minecraft.util.EnumFacing

object SecretHitboxes: Module(
    name = "Secret Hitboxes",
    description = "Full block Secret hitboxes",
    category = Category.DUNGEON
) {

    val lever: Boolean by BooleanSetting("Lever", default = true)
    val button: Boolean by BooleanSetting("Button", default = true)
    val essence: Boolean by BooleanSetting("Essence", default = true)

    var expandedLevers: HashMap<Block, EnumOrientation> = HashMap()
    var expandedButtons: HashMap<Block, IBlockState> = HashMap()
    var expandedSkulls: HashMap<Block, EnumFacing> = HashMap()

    override fun onEnable()
    {
        if (lever)
        {
            for (lever in expandedLevers)
            {
                lever.key.setBlockBounds(0f, 0f, 0f, 1f, 1f, 1f)
            }
        }

        if (button)
        {
            for (button in expandedButtons)
            {
                button.key.setBlockBounds(0f, 0f, 0f, 1f, 1f, 1f)
            }
        }

        if (essence)
        {
            for (skull in expandedSkulls)
            {
                skull.key.setBlockBounds(0f, 0f, 0f, 1f, 1f, 1f)
            }
        }

        super.onEnable()
    }

    override fun onDisable()
    {
        if (lever)
        {
            for (lever in expandedLevers)
            {
                var f = 0.1875f
                when (lever.value) {
                    EnumOrientation.EAST -> {
                        lever.key.setBlockBounds(0.0f, 0.2f, 0.5f - f, f * 2.0f, 0.8f, 0.5f + f)
                    }

                    EnumOrientation.WEST -> {
                        lever.key.setBlockBounds(1.0f - f * 2.0f, 0.2f, 0.5f - f, 1.0f, 0.8f, 0.5f + f)
                    }

                    EnumOrientation.SOUTH -> {
                        lever.key.setBlockBounds(0.5f - f, 0.2f, 0.0f, 0.5f + f, 0.8f, f * 2.0f)
                    }

                    EnumOrientation.NORTH -> {
                        lever.key.setBlockBounds(0.5f - f, 0.2f, 1.0f - f * 2.0f, 0.5f + f, 0.8f, 1.0f)
                    }

                    EnumOrientation.UP_Z, EnumOrientation.UP_X -> {
                        f = 0.25f
                        lever.key.setBlockBounds(0.5f - f, 0.0f, 0.5f - f, 0.5f + f, 0.6f, 0.5f + f)
                    }

                    EnumOrientation.DOWN_X, EnumOrientation.DOWN_Z -> {
                        f = 0.25f
                        lever.key.setBlockBounds(0.5f - f, 0.4f, 0.5f - f, 0.5f + f, 1.0f, 0.5f + f)
                    }
                }
            }
        }

        if (button)
        {
            for (button in expandedButtons)
            {
                val enumfacing: EnumFacing = button.value.getValue(BlockButton.FACING)
                val flag: Boolean = button.value.getValue(BlockButton.POWERED)
                val f2 = (if (flag) 1 else 2).toFloat() / 16.0f
                when (enumfacing) {
                    EnumFacing.EAST -> {
                        button.key.setBlockBounds(0.0f, 0.375f, 0.3125f, f2, 0.625f, 0.6875f)
                    }

                    EnumFacing.WEST -> {
                        button.key.setBlockBounds(1.0f - f2, 0.375f, 0.3125f, 1.0f, 0.625f, 0.6875f)
                    }

                    EnumFacing.SOUTH -> {
                        button.key.setBlockBounds(0.3125f, 0.375f, 0.0f, 0.6875f, 0.625f, f2)
                    }

                    EnumFacing.NORTH -> {
                        button.key.setBlockBounds(0.3125f, 0.375f, 1.0f - f2, 0.6875f, 0.625f, 1.0f)
                    }

                    EnumFacing.UP -> {
                        button.key.setBlockBounds(0.3125f, 0.0f, 0.375f, 0.6875f, 0.0f + f2, 0.625f)
                    }

                    EnumFacing.DOWN -> {
                        button.key.setBlockBounds(0.3125f, 1.0f - f2, 0.375f, 0.6875f, 1.0f, 0.625f)
                    }
                }
            }
        }

        if (essence)
        {
            for (skull in expandedSkulls)
            {
                when (skull.value) {
                    EnumFacing.NORTH -> {
                        skull.key.setBlockBounds(0.25f, 0.25f, 0.5f, 0.75f, 0.75f, 1.0f)
                    }

                    EnumFacing.SOUTH -> {
                        skull.key.setBlockBounds(0.25f, 0.25f, 0.0f, 0.75f, 0.75f, 0.5f)
                    }

                    EnumFacing.WEST -> {
                        skull.key.setBlockBounds(0.5f, 0.25f, 0.25f, 1.0f, 0.75f, 0.75f)
                    }

                    EnumFacing.EAST -> {
                        skull.key.setBlockBounds(0.0f, 0.25f, 0.25f, 0.5f, 0.75f, 0.75f)
                    }

                    else -> {
                        skull.key.setBlockBounds(0.25f, 0.0f, 0.25f, 0.75f, 0.5f, 0.75f)
                    }
                }
            }
        }

        super.onDisable()
    }

}