package me.odinmain.utils.skyblock

enum class Island(val displayName: String) {
    SinglePlayer("Singleplayer"),
    PrivateIsland("Private Island"),
    Garden("Garden"),
    SpiderDen("Spider's Den"),
    CrimsonIsle("Crimson Isle"),
    TheEnd("The End"),
    GoldMine("Gold Mine"),
    DeepCaverns("Deep Caverns"),
    DwarvenMines("Dwarven Mines"),
    CrystalHollows("Crystal Hollows"),
    FarmingIsland("The Farming Islands"),
    ThePark("The Park"),
    Dungeon("Catacombs"),
    DungeonHub("Dungeon Hub"),
    Hub("Hub"),
    DarkAuction("Dark Auction"),
    JerryWorkshop("Jerry's Workshop"),
    Kuudra("Kuudra"),
    Mineshaft("Mineshaft"),
    Rift("The Rift"),
    BackwaterBayou("Backwater Bayou"),
    Unknown("(Unknown)");

    fun isArea(area: Island): Boolean {
        if (this == SinglePlayer) return true
        return this == area
    }

    fun isArea(vararg areas: Island): Boolean {
        if (this == SinglePlayer) return true
        return this in areas
    }
}