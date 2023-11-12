package me.odinmain.utils

import me.odinmain.OdinMain.mc
import me.odinmain.utils.skyblock.ChatUtils
import net.minecraft.block.Block
import net.minecraft.block.properties.IProperty
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.*
import net.minecraft.util.BlockPos
import net.minecraft.util.MovingObjectPosition
import net.minecraft.world.WorldType
import net.minecraftforge.common.util.Constants
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.ByteArrayInputStream
import java.io.IOException


/**
 * Taken from [SkyblockAddons](https://github.com/BiscuitDevelopment/SkyblockAddons) to simplify development
 */
object DevUtils {

    fun copyEntityData() {
        if (mc.objectMouseOver == null || mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY) {
            ChatUtils.devMessage("You are not looking at an entity!")
            return
        }
        val stringBuilder = StringBuilder()
        val entity = mc.objectMouseOver?.entityHit ?: return

        // Copy the NBT data from the loaded entities.
        val entityData = NBTTagCompound()
        entity.writeToNBT(entityData)

        // Add spacing before each new entry.
        if (stringBuilder.isNotEmpty()) {
            stringBuilder.append(System.lineSeparator()).append(System.lineSeparator())
        }
        stringBuilder.append("Class: ").append(entity.javaClass.getSimpleName()).append(System.lineSeparator())
        if (entity.hasCustomName() || EntityPlayer::class.java.isAssignableFrom(entity.javaClass)) {
            stringBuilder.append("Name: ").append(entity.getName()).append(System.lineSeparator())
        }
        stringBuilder.append("NBT Data:").append(System.lineSeparator())
        stringBuilder.append(prettyPrintNBT(entityData))
        if (stringBuilder.isNotEmpty()) {
            this.writeToClipboard(
                stringBuilder.toString(),
                "Entity data was copied to clipboard!"
            )
        }
    }

    fun copyBlockData() {
        if (mc.objectMouseOver == null || mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || mc.objectMouseOver.blockPos == null) {
            ChatUtils.devMessage("You are not looking at a block!")
            return
        }
        val blockPos: BlockPos = mc.objectMouseOver.blockPos
        var blockState: IBlockState = mc.theWorld.getBlockState(blockPos)
        if (mc.theWorld.worldType !== WorldType.DEBUG_WORLD) {
            blockState = blockState.block.getActualState(blockState, mc.theWorld, blockPos)
        }
        val tileEntity = mc.theWorld.getTileEntity(blockPos)
        val nbt = NBTTagCompound()
        val nbtTileEntity = NBTTagCompound()
        tileEntity?.writeToNBT(nbtTileEntity)
        nbt.setTag("tileEntity", nbtTileEntity)
        nbt.setString("type", Block.blockRegistry.getNameForObject(blockState.block).toString())
        blockState.properties.forEach { (key: IProperty<*>, value: Comparable<*>) ->
            nbt.setString(
                key.name,
                value.toString()
            )
        }
        this.writeToClipboard(prettyPrintNBT(nbt), "Successfully copied the block data!")
    }

    private fun writeToClipboard(text: String, successMessage: String?) {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val output = StringSelection(text)
        try {
            clipboard.setContents(output, output)
            if (successMessage != null) {
                ChatUtils.devMessage(successMessage)
            }
        } catch (exception: IllegalStateException) {
            ChatUtils.devMessage("Clipboard not available!")
        }
    }

    private fun prettyPrintNBT(nbt: NBTBase): String {
        val INDENT = "    "

        val tagID = nbt.id.toInt()
        var stringBuilder = java.lang.StringBuilder()

        // Determine which type of tag it is.

        // Determine which type of tag it is.
        when (tagID) {
            Constants.NBT.TAG_END -> {
                stringBuilder.append('}')
            }
            Constants.NBT.TAG_BYTE_ARRAY, Constants.NBT.TAG_INT_ARRAY -> {
                stringBuilder.append('[')
                if (tagID == Constants.NBT.TAG_BYTE_ARRAY) {
                    val nbtByteArray = nbt as NBTTagByteArray
                    val bytes = nbtByteArray.byteArray
                    for (i in bytes.indices) {
                        stringBuilder.append(bytes[i].toInt())

                        // Don't add a comma after the last element.
                        if (i < bytes.size - 1) {
                            stringBuilder.append(", ")
                        }
                    }
                } else {
                    val nbtIntArray = nbt as NBTTagIntArray
                    val ints = nbtIntArray.intArray
                    for (i in ints.indices) {
                        stringBuilder.append(ints[i])

                        // Don't add a comma after the last element.
                        if (i < ints.size - 1) {
                            stringBuilder.append(", ")
                        }
                    }
                }
                stringBuilder.append(']')
            }
            Constants.NBT.TAG_LIST -> {
                val nbtTagList = nbt as NBTTagList
                stringBuilder.append('[')
                for (i in 0 until nbtTagList.tagCount()) {
                    val currentListElement = nbtTagList[i]
                    stringBuilder.append(prettyPrintNBT(currentListElement))

                    // Don't add a comma after the last element.
                    if (i < nbtTagList.tagCount() - 1) {
                        stringBuilder.append(", ")
                    }
                }
                stringBuilder.append(']')
            }
            Constants.NBT.TAG_COMPOUND -> {
                val nbtTagCompound = nbt as NBTTagCompound
                stringBuilder.append('{')
                if (!nbtTagCompound.hasNoTags()) {
                    val iterator: Iterator<String> = nbtTagCompound.keySet.iterator()
                    stringBuilder.append(System.lineSeparator())
                    while (iterator.hasNext()) {
                        val key = iterator.next()
                        val currentCompoundTagElement = nbtTagCompound.getTag(key)
                        stringBuilder.append(key).append(": ").append(
                            prettyPrintNBT(currentCompoundTagElement)
                        )
                        if (key.contains("backpack_data") && currentCompoundTagElement is NBTTagByteArray) {
                            try {
                                val backpackData = CompressedStreamTools.readCompressed(
                                    ByteArrayInputStream(
                                        currentCompoundTagElement.byteArray
                                    )
                                )
                                stringBuilder.append(",").append(System.lineSeparator())
                                stringBuilder.append(key).append("(decoded): ").append(
                                    prettyPrintNBT(backpackData)
                                )
                            } catch (e: IOException) {
                                // Empty catch block
                            }
                        }

                        // Don't add a comma after the last element.
                        if (iterator.hasNext()) {
                            stringBuilder.append(",").append(System.lineSeparator())
                        }
                    }

                    // Indent all lines
                    val indentedString =
                        stringBuilder.toString().replace(System.lineSeparator().toRegex(), System.lineSeparator() + INDENT)
                    stringBuilder = java.lang.StringBuilder(indentedString)
                }
                stringBuilder.append(System.lineSeparator()).append('}')
            }
            else -> {
                stringBuilder.append(nbt)
            }
        }

        return stringBuilder.toString()
    }

}