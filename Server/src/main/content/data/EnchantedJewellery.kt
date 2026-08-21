package content.data

import content.global.skill.magic.TeleportMethod
import content.global.skill.slayer.SlayerManager
import core.Util
import core.api.*
import core.game.dialogue.FaceAnim
import core.game.event.TeleportEvent
import core.game.interaction.QueueStrength
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.TeleportManager
import core.game.node.item.Item
import core.game.world.map.Location
import core.game.world.update.flag.context.Animation
import core.game.world.update.flag.context.Graphics
import shared.consts.Animations
import shared.consts.Items
import shared.consts.Sounds

/**
 * Represents enchanted jewellery items and their corresponding teleport options.
 */
enum class EnchantedJewellery(
    val options: Array<String>,
    val locations: Array<Location>,
    val crumbled: Boolean,
    vararg val ids: Int
) {

    RING_OF_SLAYING(
        options = arrayOf(
            "Sumona in Pollnivneach.",
            "Morytania Slayer Tower.",
            "Rellekka Slayer Caves.",
            "Tarn's Lair",
            "Nowhere. Give me a Slayer update."
        ),
        locations = arrayOf(
            Location.create(3361, 2994, 0),
            Location.create(3428, 3535, 0),
            Location.create(2792, 3615, 0),
            Location.create(3424, 9660, 0)
        ),
        crumbled = true,
        Items.RING_OF_SLAYING8_13281,
        Items.RING_OF_SLAYING7_13282,
        Items.RING_OF_SLAYING6_13283,
        Items.RING_OF_SLAYING5_13284,
        Items.RING_OF_SLAYING4_13285,
        Items.RING_OF_SLAYING3_13286,
        Items.RING_OF_SLAYING2_13287,
        Items.RING_OF_SLAYING1_13288
    ),

    RING_OF_DUELING(
        options = arrayOf(
            "Al Kharid Duel Arena.",
            "Castle Wars Arena.",
            "Nowhere."
        ),
        locations = arrayOf(
            Location.create(3314, 3235, 0),
            Location.create(2442, 3089, 0)
        ),
        crumbled = true,
        Items.RING_OF_DUELLING8_2552,
        Items.RING_OF_DUELLING7_2554,
        Items.RING_OF_DUELLING6_2556,
        Items.RING_OF_DUELLING5_2558,
        Items.RING_OF_DUELLING4_2560,
        Items.RING_OF_DUELLING3_2562,
        Items.RING_OF_DUELLING2_2564,
        Items.RING_OF_DUELLING1_2566
    ),

    AMULET_OF_GLORY(
        options = arrayOf(
            "Edgeville.",
            "Karamja.",
            "Draynor Village.",
            "Al-Kharid.",
            "Nowhere."
        ),
        locations = arrayOf(
            Location.create(3087, 3495, 0),
            Location.create(2919, 3175, 0),
            Location.create(3104, 3249, 0),
            Location.create(3304, 3124, 0)
        ),
        crumbled = false,
        Items.AMULET_OF_GLORY4_1712,
        Items.AMULET_OF_GLORY3_1710,
        Items.AMULET_OF_GLORY2_1708,
        Items.AMULET_OF_GLORY1_1706,
        Items.AMULET_OF_GLORY_1704
    ),

    AMULET_OF_GLORY_T(
        options = AMULET_OF_GLORY.options,
        locations = AMULET_OF_GLORY.locations,
        crumbled = true,
        Items.AMULET_OF_GLORYT4_10354,
        Items.AMULET_OF_GLORYT3_10356,
        Items.AMULET_OF_GLORYT2_10358,
        Items.AMULET_OF_GLORYT1_10360,
        Items.AMULET_OF_GLORYT_10362
    ),

    GAMES_NECKLACE(
        options = arrayOf(
            "Burthorpe Games Room.",
            "Barbarian Outpost.",
            "Clan Wars.",
            "Wilderness Volcano.",
            "Nowhere."
        ),
        locations = arrayOf(
            Location.create(2207, 4940, 0),
            Location.create(2520, 3571, 0),
            Location.create(3266, 3686, 0),
            Location.create(3179, 3685, 0)
        ),
        crumbled = true,
        Items.GAMES_NECKLACE8_3853,
        Items.GAMES_NECKLACE7_3855,
        Items.GAMES_NECKLACE6_3857,
        Items.GAMES_NECKLACE5_3859,
        Items.GAMES_NECKLACE4_3861,
        Items.GAMES_NECKLACE3_3863,
        Items.GAMES_NECKLACE2_3865,
        Items.GAMES_NECKLACE1_3867
    ),

    DIGSITE_PENDANT(
        options = arrayOf(),
        locations = arrayOf(
            Location.create(3342, 3445, 0)
        ),
        crumbled = true,
        Items.DIGSITE_PENDANT_5_11194,
        Items.DIGSITE_PENDANT_4_11193,
        Items.DIGSITE_PENDANT_3_11192,
        Items.DIGSITE_PENDANT_2_11191,
        Items.DIGSITE_PENDANT_1_11190
    ),

    COMBAT_BRACELET(
        options = arrayOf(
            "Warriors' Guild",
            "Champions' Guild",
            "Monastery",
            "Ranging Guild",
            "Nowhere"
        ),
        locations = arrayOf(
            Location.create(2878, 3546, 0),
            Location.create(3191, 3365, 0),
            Location.create(3051, 3489, 0),
            Location.create(2657, 3439, 0)
        ),
        crumbled = false,
        Items.COMBAT_BRACELET4_11118,
        Items.COMBAT_BRACELET3_11120,
        Items.COMBAT_BRACELET2_11122,
        Items.COMBAT_BRACELET1_11124,
        Items.COMBAT_BRACELET_11126
    ),

    SKILLS_NECKLACE(
        options = arrayOf(
            "Fishing Guild.",
            "Mining Guild.",
            "Crafting Guild.",
            "Cooking Guild.",
            "Nowhere."
        ),
        locations = arrayOf(
            Location.create(2611, 3392, 0),
            Location.create(3016, 3338, 0),
            Location.create(2933, 3290, 0),
            Location.create(3143, 3442, 0)
        ),
        crumbled = false,
        Items.SKILLS_NECKLACE4_11105,
        Items.SKILLS_NECKLACE3_11107,
        Items.SKILLS_NECKLACE2_11109,
        Items.SKILLS_NECKLACE1_11111,
        Items.SKILLS_NECKLACE_11113
    ),

    RING_OF_LIFE(
        options = arrayOf(),
        locations = arrayOf(),
        crumbled = true,
        Items.RING_OF_LIFE_2570
    );

    /**
     * Handles the use of enchanted jewellery.
     */
    fun use(
        player: Player,
        item: Item,
        buttonID: Int,
        isEquipped: Boolean
    ) {
        if (buttonID >= locations.size) {
            if (isSlayerRing(item)) {
                slayerProgressDialogue(player)
            }
            return
        }

        attemptTeleport(player, item, buttonID, isEquipped)
    }

    /**
     * Attempts to teleport the player.
     */
    fun attemptTeleport(
        player: Player,
        item: Item,
        buttonID: Int,
        isEquipped: Boolean
    ): Boolean {
        val itemIndex = getItemIndex(item)
        val nextItem = Item(getNext(itemIndex))

        if (!canTeleport(player, nextItem)) {
            return false
        }

        lock(player, 4)

        val location = if (this == RING_OF_LIFE) {
            player.getRespawnLocation()
        } else {
            getLocation(buttonID)
        }

        closeAllInterfaces(player)

        queueScript(player, 0, QueueStrength.NORMAL) { stage ->
            when (stage) {
                0 -> {
                    player.impactHandler.disabledTicks = 3
                    visualize(player, ANIMATION, TELEPORT_GRAPHICS)
                    playGlobalAudio(player.location, Sounds.TP_ALL_200)
                    delayScript(player, 3)
                }

                1 -> {
                    teleport(player, location)
                    resetAnimator(player)
                    handleJewelleryUsage(
                        player = player,
                        item = item,
                        nextItem = nextItem,
                        itemIndex = itemIndex,
                        isEquipped = isEquipped,
                        location = location
                    )
                    stopExecuting(player)
                }

                else -> stopExecuting(player)
            }
        }

        return true
    }

    /**
     * Handles jewellery after teleportation.
     */
    private fun handleJewelleryUsage(
        player: Player,
        item: Item,
        nextItem: Item,
        itemIndex: Int,
        isEquipped: Boolean,
        location: Location
    ) {
        val lastCharge = isLastItemIndex(itemIndex)
        val jewelleryType = getJewelleryType(item)

        if (lastCharge) {
            if (crumbled) {
                crumbleJewellery(player, item, isEquipped)
            } else {
                sendMessage(
                    player,
                    "You will need to recharge your $jewelleryType before you can use it again."
                )
            }
        } else {
            replaceJewellery(player, item, nextItem, isEquipped)
        }

        unlock(player)

        sendMessage(
            player,
            if (lastCharge) {
                "You use your ${getPossessiveJewelleryName(item)} last charge."
            } else {
                "Your $jewelleryType has ${Util.convert(ids.size - itemIndex - 1)} uses left."
            }
        )

        player.dispatch(
            TeleportEvent(
                TeleportManager.TeleportType.NORMAL,
                TeleportMethod.JEWELRY,
                item,
                location
            )
        )
    }

    /**
     * Replaces the current jewellery item with the next charge.
     */
    private fun replaceJewellery(
        player: Player,
        item: Item,
        nextItem: Item,
        isEquipped: Boolean
    ) {
        if (isEquipped) {
            replaceSlot(
                player,
                item.slot,
                nextItem,
                item,
                Container.EQUIPMENT
            )
        } else {
            replaceSlot(player, item.slot, nextItem)
        }
    }

    /**
     * Removes a depleted jewellery item.
     */
    private fun crumbleJewellery(
        player: Player,
        item: Item,
        isEquipped: Boolean
    ) {
        if (isEquipped) {
            removeItem(player, item, Container.EQUIPMENT)
        } else {
            removeItem(player, item)
        }

        if (!isSlayerRing(item)) {
            return
        }

        sendMessage(
            player,
            "The ring collapses into a Slayer gem, which you stow in your pack."
        )
        addItem(player, Items.ENCHANTED_GEM_4155)
    }

    /**
     * Checks if the item is a slayer ring.
     */
    private fun isSlayerRing(item: Item): Boolean =
        item.id in RING_OF_SLAYING.ids

    /**
     * Displays the slayer progress dialogue.
     */
    private fun slayerProgressDialogue(player: Player) {
        val manager = SlayerManager.getInstance(player)
        val master = manager.master!!

        if (!manager.hasTask()) {
            sendNPCDialogue(
                player,
                master.npc,
                "You need something new to hunt. Come and see me when you can and I'll give you a new task.",
                FaceAnim.HALF_GUILTY
            )
            return
        }

        sendNPCDialogue(
            player,
            master.npc,
            "You're currently assigned to kill ${getSlayerTaskName(player).lowercase()}'s; " +
                    "only ${getSlayerTaskKillsRemaining(player)} more to go.",
            FaceAnim.FRIENDLY
        )

        setVarp(
            player,
            2502,
            manager.flags.taskFlags shr 4
        )
    }

    /**
     * Checks if the player can teleport using the jewellery.
     */
    private fun canTeleport(
        player: Player,
        item: Item
    ): Boolean = player.zoneMonitor.teleport(1, item)

    /**
     * Gets the next item in the jewellery sequence.
     */
    fun getNext(index: Int): Int =
        ids.getOrElse(index + 1) { ids.last() }

    /**
     * Gets the location associated with the given option.
     */
    private fun getLocation(index: Int): Location =
        locations.getOrElse(index) { locations.last() }

    /**
     * Gets the jewellery name without its charge suffix.
     */
    fun getJewelleryName(item: Item): String =
        item.name.replace(CHARGE_SUFFIX_REGEX, "")

    /**
     * Gets the generic jewellery type name.
     */
    fun getJewelleryType(item: Item): String =
        when (this) {
            GAMES_NECKLACE -> "games necklace"
            DIGSITE_PENDANT -> "digsite pendant"
            COMBAT_BRACELET -> "combat bracelet"
            SKILLS_NECKLACE -> "skill's necklace"
            AMULET_OF_GLORY,
            AMULET_OF_GLORY_T -> "amulet of glory"
            RING_OF_SLAYING -> "ring of slaying"
            RING_OF_DUELING -> "ring of dueling"
            else -> item.name.substringBefore(" ").lowercase()
        }

    /**
     * Gets the possessive name used in the last-charge message.
     */
    private fun getPossessiveJewelleryName(item: Item): String =
        when {
            "ring" in item.name.lowercase() -> "ring's"
            "combat" in item.name.lowercase() -> "bracelet's"
            "necklace" in item.name.lowercase() -> "necklace's"
            else -> "amulet's"
        }

    /**
     * Checks if this is the last item in the sequence.
     */
    fun isLastItemIndex(index: Int): Boolean =
        index == ids.lastIndex

    /**
     * Gets the index of the item in the jewellery sequence.
     */
    fun getItemIndex(item: Item): Int =
        ids.indexOf(item.id)

    companion object {
        private val ANIMATION = Animation(Animations.ANIMATION_9603)

        private val TELEPORT_GRAPHICS =
            Graphics(shared.consts.Graphics.TP_RING_OF_DUELING_1684)

        private val CHARGE_SUFFIX_REGEX =
            """ ?\(t?[0-9]?\)""".toRegex()

        val idMap = HashMap<Int, EnchantedJewellery>()

        init {
            values().forEach { jewellery ->
                jewellery.ids.forEach { id ->
                    idMap[id] = jewellery
                }
            }
        }
    }
}