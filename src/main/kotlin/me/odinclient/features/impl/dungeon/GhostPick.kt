package me.odinclient.features.impl.dungeon

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.enchantment.Enchantment
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import org.lwjgl.input.Mouse

object GhostPick : Module(
    "Ghost Pickaxe",
    description = "Gives you a ghost pickaxe in your selected slot when you press the keybind.",
    category = Category.DUNGEON
) {
    private val slot: Int by NumberSetting("Ghost pick slot", 1, 1.0, 9.0, 1.0)
    private val rightStonk: Boolean by BooleanSetting("Right Click Ghost Block")
    private val leftStonk: Boolean by BooleanSetting("Left Click Ghost Block")
    private val stonkShiftOnly: Boolean by BooleanSetting("Shift Only")

    private val item = ItemStack(Item.getItemById(278), 1).apply {
        addEnchantment(Enchantment.getEnchantmentById(32), 10)
        tagCompound?.setBoolean("Unbreakable", true)
    }

    override fun onKeybind() {
        if (mc.thePlayer == null || mc.currentScreen != null) return
        if (enabled) mc.thePlayer?.inventory?.mainInventory?.set(slot - 1, item)
    }

    @SubscribeEvent
    fun onRightClick(event: InputEvent.MouseInputEvent) {
        if (!rightStonk || !DungeonUtils.inDungeons) return
        if (stonkShiftOnly) {
            if (!mc.thePlayer.isSneaking) return
        }
        val heldStack = mc.thePlayer.heldItem ?: return
        val heldItem = heldStack.item
        if (Mouse.isButtonDown(1)) {
            if (heldItem == Items.golden_pickaxe || heldItem == Items.diamond_pickaxe || heldItem == Items.iron_pickaxe || heldItem == Items.iron_pickaxe || heldItem == Items.stone_pickaxe || heldItem == Items.wooden_pickaxe){
                createGhostBlock()
            }
        }
    }

    @SubscribeEvent
    fun onLeftClick(event: InputEvent.MouseInputEvent) {
        if (!leftStonk || !DungeonUtils.inDungeons) return
        if (stonkShiftOnly) {
            if (!mc.thePlayer.isSneaking) return
        }
        val heldStack = mc.thePlayer.heldItem ?: return
        val heldItem = heldStack.item
        if (Mouse.isButtonDown(0)) {
            if (heldItem == Items.golden_pickaxe || heldItem == Items.diamond_pickaxe || heldItem == Items.iron_pickaxe || heldItem == Items.iron_pickaxe || heldItem == Items.stone_pickaxe || heldItem == Items.wooden_pickaxe){
                createGhostBlock()
            }
        }
    }
    private fun createGhostBlock() {
        val raytrace = mc.thePlayer.rayTrace(6.0,1.0f)
        if (raytrace != null) {
            val block = mc.thePlayer.worldObj.getChunkFromBlockCoords(raytrace.blockPos).getBlock(raytrace.blockPos)
            if (block != Blocks.chest && block != Blocks.trapped_chest && block != Blocks.skull && block != Blocks.lever && block != Blocks.hopper) mc.thePlayer.worldObj.setBlockToAir(raytrace.blockPos)
            else return
        } else return
    }
}