package me.odinmain.commands.impl

import com.github.stivais.commodore.Commodore
import me.odinmain.OdinMain.mc
import me.odinmain.config.Config
import me.odinmain.features.impl.skyblock.PetKeybinds.petList
import me.odinmain.utils.skyblock.isHolding
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.skyblock.uuid

val petCommand = Commodore("petkeys") {
    literal("add").runs {
        val petID = if (isHolding("PET")) mc.thePlayer?.heldItem.uuid else null
        if (petID == null) return@runs modMessage("You can only add pets to the pet list!")
        if (petList.size >= 9) return@runs modMessage("You cannot add more than 9 pets to the list. Remove a pet using /petkeys remove or clear the list using /petkeys clear.")
        if (petID in petList) return@runs modMessage("This pet is already in the list!")

        petList.add(petID)
        modMessage("Added this pet to the pet list in position ${petList.indexOf(petID) +1}!")
        Config.save()
    }

    literal("petpos").runs {
        val petID = if (isHolding("PET")) mc.thePlayer?.heldItem.uuid else return@runs modMessage("This is not a pet!")
        if (petID !in petList) return@runs modMessage("This pet is not in the list!")
        modMessage("This pet is position ${petList.indexOf(petID) +1} in the list.")
    }

    literal("remove").runs {
        val petID = if (isHolding("PET")) mc.thePlayer?.heldItem.uuid else return@runs modMessage("This is not a pet!")
        if (petID !in petList) return@runs modMessage("This pet is not in the list!")

        petList.remove(petID)
        modMessage("Removed this pet from the pet list!")
        Config.save()
    }

    literal("clear").runs {
        petList.clear()
        modMessage("Cleared the pet list!")
        Config.save()
    }

    literal("list").runs {
        if (petList.isEmpty()) return@runs modMessage("Pet list is empty")
        modMessage("Pet list:\n${petList.joinToString("\n")}")
    }
}
