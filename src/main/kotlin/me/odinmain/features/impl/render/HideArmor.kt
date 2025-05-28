package me.odinmain.features.impl.render

import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.DropdownSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.utils.equalsOneOf
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer

object HideArmor : Module(
    name = "Hide Armor",
    desc = "Prevents rendering of selectable armor pieces."
) {
    private val hideOnlyPlayers by BooleanSetting("Hide Only Players", true, desc = "Only hide armor on players.")
    private val hideArmor by SelectorSetting("Hide Armor", "Self", options = arrayListOf("Self", "Others", "Both"), desc = "Hide the armor of yourself, others, or both.")
    private val selfDropdown by DropdownSetting("Self").withDependency { hideArmor.equalsOneOf(0, 2) }
    private val selfHelmet by BooleanSetting("Self Helmet", true, desc = "Hide your helmet.").withDependency { selfDropdown && hideArmor != 1 }
    private val selfChestplate by BooleanSetting("Self Chestplate", true, desc = "Hide your chestplate.").withDependency { selfDropdown && hideArmor != 1 }
    private val selfLeggings by BooleanSetting("Self Leggings", true, desc = "Hide your leggings.").withDependency { selfDropdown && hideArmor != 1 }
    private val selfBoots by BooleanSetting("Self Boots", true, desc = "Hide your boots.").withDependency { selfDropdown && hideArmor != 1 }
    private val selfSkull by BooleanSetting("Self Skull", true, desc = "Hide your skull.").withDependency { selfDropdown && hideArmor != 1 }
    private val othersDropdown by DropdownSetting("Others").withDependency { hideArmor.equalsOneOf(1, 2) }
    private val othersHelmet by BooleanSetting("Others Helmet", true, desc = "Hide others' helmets.").withDependency { othersDropdown && hideArmor != 0 }
    private val othersChestplate by BooleanSetting("Others Chestplate", true, desc = "Hide others' chestplates.").withDependency { othersDropdown && hideArmor != 0 }
    private val othersLeggings by BooleanSetting("Others Leggings", true, desc = "Hide others' leggings.").withDependency { othersDropdown && hideArmor != 0 }
    private val othersBoots by BooleanSetting("Others Boots", true, desc = "Hide others' boots.").withDependency { othersDropdown && hideArmor != 0 }
    private val othersSkull by BooleanSetting("Others Skull", true, desc = "Hide others' skulls.").withDependency { othersDropdown && hideArmor != 0 }

    @JvmStatic
    fun shouldHideArmor(entityLivingBase: EntityLivingBase, piece: Int): Boolean {
        if (!enabled || mc.thePlayer == null || (hideOnlyPlayers && entityLivingBase !is EntityPlayer && entityLivingBase.uniqueID.version() != 2)) return false

        return when {
            entityLivingBase == mc.thePlayer && hideArmor.equalsOneOf(0, 2) -> when (piece) {
                4 -> selfHelmet
                3 -> selfChestplate
                2 -> selfLeggings
                1 -> selfBoots
                else -> false
            }
            entityLivingBase != mc.thePlayer && hideArmor.equalsOneOf(1, 2) -> when (piece) {
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
        if (!enabled || mc.thePlayer == null || (hideOnlyPlayers && entityLivingBase !is EntityPlayer && entityLivingBase.uniqueID.version() != 2)) return false

        return when {
            entityLivingBase == mc.thePlayer && hideArmor.equalsOneOf(0, 2) -> selfSkull
            entityLivingBase != mc.thePlayer && hideArmor.equalsOneOf(1, 2) -> othersSkull
            else -> false
        }
    }
}