package me.odinmain.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.DropdownSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.utils.equalsOneOf
import net.minecraft.entity.EntityLivingBase

object HideArmor : Module(
    name = "Hide Armor",
    description = "Hide armor pieces.",
    category = Category.SKYBLOCK
) {
    private val hideArmor by SelectorSetting(name = "Hide Armor", "Self", options = arrayListOf("Self", "Others", "Both"), description = "Hide the armor of yourself, others, or both.")
    private val selfDropdown by DropdownSetting("Self").withDependency { hideArmor == 0 || hideArmor == 2 }
    private val selfHelmet by BooleanSetting("Helmet", true, description = "Hide your helmet.").withDependency { selfDropdown }
    private val selfChestplate by BooleanSetting("Self Chestplate", true, description = "Hide your chestplate.").withDependency { selfDropdown && hideArmor != 1 }
    private val selfLeggings by BooleanSetting("Self Leggings", true, description = "Hide your leggings.").withDependency { selfDropdown && hideArmor != 1 }
    private val selfBoots by BooleanSetting("Self Boots", true, description = "Hide your boots.").withDependency { selfDropdown && hideArmor != 1 }
    private val selfSkull by BooleanSetting("Self Skull", true, description = "Hide your skull.").withDependency { selfDropdown && hideArmor != 1 }
    private val othersDropdown by DropdownSetting("Others").withDependency { hideArmor == 1 || hideArmor == 2 }
    private val othersHelmet by BooleanSetting("Others Helmet", true, description = "Hide others' helmets.").withDependency { othersDropdown && hideArmor != 0 }
    private val othersChestplate by BooleanSetting("Others Chestplate", true, description = "Hide others' chestplates.").withDependency { othersDropdown && hideArmor != 0 }
    private val othersLeggings by BooleanSetting("Others Leggings", true, description = "Hide others' leggings.").withDependency { othersDropdown && hideArmor != 0 }
    private val othersBoots by BooleanSetting("Others Boots", true, description = "Hide others' boots.").withDependency { othersDropdown && hideArmor != 0 }
    private val othersSkull by BooleanSetting("Others Skull", true, description = "Hide others' skulls.").withDependency { othersDropdown && hideArmor != 0 }

    @JvmStatic
    fun shouldHideArmor(entityLivingBase: EntityLivingBase, piece: Int): Boolean {
        if (!enabled || mc.thePlayer == null) return false

        return when {
            entityLivingBase == mc.thePlayer && hideArmor.equalsOneOf(0, 2) -> when (piece) {
                4 -> selfHelmet
                3 -> selfChestplate
                2 -> selfLeggings
                1 -> selfBoots
                else -> false
            }
            hideArmor.equalsOneOf(1, 2) -> when (piece) {
                4 -> othersHelmet
                3 -> othersChestplate
                2 -> othersLeggings
                1 -> othersBoots
                else -> false
            }
            else -> false
        }
    }

    @JvmStatic
    fun shouldHideSkull(entityLivingBase: EntityLivingBase): Boolean {
        if (!enabled || mc.thePlayer == null) return false

        return when {
            entityLivingBase == mc.thePlayer && hideArmor.equalsOneOf(0, 2) -> selfSkull
            hideArmor.equalsOneOf(1, 2) -> othersSkull
            else -> false
        }
    }
}