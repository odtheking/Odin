package me.odinclient.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import net.minecraft.tileentity.TileEntitySkull
import net.minecraft.util.BlockPos
import java.util.*

/**
 * @see me.odinclient.mixin.mixins.block
 */
object SecretHitboxes : Module(
    name = "Secret Hitboxes",
    description = "Full block Secret hitboxes.",
    category = Category.DUNGEON
) {
    val lever: Boolean by BooleanSetting("Lever", default = false, description = "Extends the lever hitbox.")
    val button: Boolean by BooleanSetting("Button", default = false, description = "Extends the button hitbox.")
    val essence: Boolean by BooleanSetting("Essence", default = false, description = "Extends the essence hitbox.")
    val chests: Boolean by BooleanSetting("Chests", default = false, description = "Extends the chest hitbox.")

    private val mostSignificantBits = UUID.fromString("26bb1a8d-7c66-31c6-82d5-a9c04c94fb02").mostSignificantBits

    fun isEssence(blockPos: BlockPos): Boolean {
        return essence && (mc.theWorld?.getTileEntity(blockPos) as? TileEntitySkull)?.playerProfile?.id?.mostSignificantBits == mostSignificantBits
    }
}