package me.odinclient.utils.skyblock

enum class Area {
    Dungeon,
    PrivateIsland,
    DungeonHub,
    GoldMine,
    DeepCaverns,
    DwarvenMines,
    CrystalHollows,
    SpiderDen,
    CrimsonIsle,
    End,
    Park,
    FarmingIslands,
    Kuudra,
    Hub,
    Garden,
    Rift;

    companion object {
        fun fromString(str: String?): Area? = when (str) {
            "The Rift" -> Rift
            "Crimson Isle" -> CrimsonIsle
            "Catacombs" -> Dungeon
            "Private Island" -> PrivateIsland
            "Dungeon Hub" -> DungeonHub
            "Gold Mine" -> GoldMine
            "Deep Caverns" -> DeepCaverns
            "Dwarven Mines" -> DwarvenMines
            "Crystal Hollows" -> CrystalHollows
            "Spider's Den" -> SpiderDen
            "The Park" -> Park
            "The End" -> End
            "The Farming Islands" -> FarmingIslands
            "Kuudra's Hollow" -> Kuudra
            "Hub" -> Hub
            "Garden" -> Garden
            else -> null
        }
    }
}