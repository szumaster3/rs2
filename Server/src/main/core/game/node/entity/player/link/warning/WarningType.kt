package core.game.node.entity.player.link.warning


/**
 * Represents all configurable warnings.
 *///https://runescape.wiki/w/Doomsayer?oldid=714289
enum class WarningType(val varbit: Int, val component: Int, val buttonId: Int) {
    WILDERNESS_DITCH(3857, 382, 68),
    CORPOREAL_BEAST_DANGEROUS(5366, 650, 79),
    BOUNTY_AREA(4199, 561, 75),
    GOD_WARS_WILDERNESS_AGILITY_ROUTE(3946, 600, 73),
    DEATH_PLATEAU(3872, 581, 72),
    DUEL_ARENA(4132, 627, 74),
    CLAN_WARS_FFA_SAFETY(5294, -1, 80),
    CLAN_WARS_FFA_DANGEROUS(5295, -1, 81),
    PVP_WORLDS(5396, -1, 82),
    SHANTAY_PASS(3860, 565, 64),
    FAIRY_RING_TO_DORGESH_KAAN(3865, 578, 60),
    MORT_MYRE(3870, 580, 62),
    DORGESH_KAAN_CITY_EXIT(3869, 571, 69),
    RANGING_GUILD(3871, 564, 71),
    DAGANNOTH_KINGS_LADDER(3851, 574, 51),
    CONTACT_DUNGEON_LADDER(3852, 562, 57),
    FALADOR_MOLE_LAIR(3853, 568, 54),
    STRONGHOLD_OF_SECURITY_LADDERS(3854, 579, 53),
    WATCHTOWER_SHAMAN_CAVE(3862, 573, 66),
    LUMBRIDGE_SWAMP_CAVE_ROPE(3863, 572, 52),
    HAM_TUNNEL_FROM_MILL(3864, 567, 59),
    ELID_GENIE_CAVE(3867, 570, 65),
    DORGESH_KAAN_TUNNEL_TO_KALPHITES(3868, 577, 70),
    TROLLHEIM_WILDERNESS_ENTRANCE(3858, 576, 67),
    OBSERVATORY_STAIRS(3859, 560, 63),
    LUMBRIDGE_CELLAR(3866, 569, 61),
    PLAYER_OWNED_HOUSES(3855, 563, 56),
    DROPPED_ITEMS_IN_RANDOM_EVENTS(3856, 566, 55),
    ICY_PATH_AREA(3861, 327, 58),
    CHAOS_TUNNELS_EAST(4307, 676, 76),
    CHAOS_TUNNELS_CENTRAL(4308, 677, 77),
    CHAOS_TUNNELS_WEST(4309, 678, 78),
    LIVING_ROCK_CAVERNS(6500, 892, 83);

    val binaryToggle: Boolean
        get() = this == CLAN_WARS_FFA_SAFETY || this == CLAN_WARS_FFA_DANGEROUS

    companion object {
        val values = enumValues<WarningType>()
        val byVarbit = values.associateBy { it.varbit }
        val byButton = values.associateBy { it.buttonId }
    }
}