package content.region.misthalin.draynor.quest.swept.plugin

import content.data.GameAttributes
import core.api.*
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.emote.Emotes
import core.game.node.scenery.Scenery
import core.game.system.task.Pulse
import core.game.world.GameWorld
import core.game.world.map.Location
import shared.consts.*
import shared.consts.Scenery as Objects

internal object SweptUtils {

    const val PEN_TABLE_INTERFACE = Components.MAGICAL_SLOPE_799
    const val BROOM_MODEL = 42783
    const val SWEEP_ANIMATION = Animations.SWEEP_BROOM_10532
    const val BROOM_ENCHANTMENT_GFX = 1865
    const val VARBIT_NEWT_CRATE_LABEL = 5460
    const val VARBIT_TOAD_CRATE_LABEL = 5461
    const val VARBIT_NEWT_AND_TOAD_CRATE_LABEL = 5462
    const val VARBIT_RIMMINGTON_TRAPDOOR = 5465
    const val VARBIT_LOTTIE_CHEST = 5467
    const val VARBIT_BETTY_TRAPDOOR = 5469

    const val STIR_ANIMATION = 10546
    const val ENCHANT_BROOMSTICK_ANIMATION = 10535
    const val STIR_WITH_BROOMSTICK_ANIMATION = 10543
    const val FILL_GOULASH_FROM_CAULDRON_A = 10544
    const val FILL_GOULASH_FROM_CAULDRON_B = 10545
    const val TAKE_FROG_FROM_CRATE_ANIMATION = 10550
    const val TAKE_NEWT_FROM_CRATE_ANIMATION = 10551

    const val NT_CRATE = shared.consts.Scenery.WRONGLY_LABELLED_CRATE_NEWTS_AND_TOADS_39336
    const val N_CRATE = shared.consts.Scenery.WRONGLY_LABELLED_CRATE_NEWTS_39335
    const val T_CRATE = shared.consts.Scenery.WRONGLY_LABELLED_CRATE_TOADS_39334

    val GUS_CRATES = intArrayOf(39334, 39335, 39336)
    val GUS_CRATES_LABELLED = intArrayOf(39337, 39339, 39341)

    val LABEL_ITEMS = intArrayOf(Items.NEWT_LABEL_14065, Items.TOAD_LABEL_14066, Items.NEWTS_AND_TOADS_LABEL_14067)

    val CREATURES = mutableMapOf<Pen, Pair<NPC, Int>>()

    enum class Pen(val penId: Int, val npcId: Int, val itemId: Int, val location: Location, val modelId: Int) {
        BAT(Objects.BAT_PEN_39287, NPCs.BAT_8208, Items.BAT_14072, Location(3222, 4513, 0), 42750),
        RAT(Objects.RAT_PEN_39309, NPCs.RAT_8209, Items.RAT_14074, Location(3240, 4513, 0), 42747),
        REPTILE(Objects.REPTILE_PEN_39303, NPCs.LIZARD_8210, Items.REPTILE_14070, Location(3240, 4504, 0), 42748),
        BLACKBIRD(Objects.BLACKBIRD_PEN_39281, NPCs.BLACKBIRD_8211, Items.BLACKBIRD_14071, Location(3231, 4513, 0), 42746),
        SPIDER(Objects.SPIDER_PEN_39297, NPCs.SPIDER_8212, Items.SPIDER_14073, Location(3231, 4504, 0), 42751),
        SNAIL(Objects.SNAIL_PEN_39292, NPCs.SNAIL_8213, Items.SNAIL_14075, Location(3222, 4504, 0), 42749),
        HOLDING(Objects.HOLDING_PEN_39314, -1, -1, Location(3231, 4523, 0), -1);

        companion object {
            private val map = values().associateBy { it.penId }
            fun fromId(id: Int) = map[id]
        }
    }

    private val COMPONENT_TO_PEN = mapOf(
        10 to Objects.HOLDING_PEN_39314,
        11 to Objects.BAT_PEN_39287,
        12 to Objects.BLACKBIRD_PEN_39281,
        13 to Objects.RAT_PEN_39309,
        14 to Objects.SNAIL_PEN_39292,
        15 to Objects.SPIDER_PEN_39297,
        16 to Objects.REPTILE_PEN_39303
    )

    /**
     * Starting layout of the menagerie: pen -> creature that starts inside it.
     * The bat and the snail are already at home, the other four are scrambled.
     */
    private val INITIAL_LAYOUT: Map<Pen, Pen> = linkedMapOf(
        Pen.BAT to Pen.BAT,
        Pen.SNAIL to Pen.SNAIL,
        Pen.RAT to Pen.SPIDER,
        Pen.SPIDER to Pen.REPTILE,
        Pen.REPTILE to Pen.BLACKBIRD,
        Pen.BLACKBIRD to Pen.RAT
    )

    private const val MAX_CREATURE_MOVES = 16

    private fun spawnCreature(pen: Pen, creature: Pen) {
        val npc = NPC.create(creature.npcId, pen.location)
        CREATURES[pen] = Pair(npc, creature.itemId)
        npc.init()
    }

    /**
     * Spawns all NPCs in the Betty basement pens in their (scrambled) starting layout.
     */
    @JvmStatic
    fun spawnBettyBasementNPCs() {
        removeBettyNPCs()
        INITIAL_LAYOUT.forEach { (pen, creature) -> spawnCreature(pen, creature) }
    }

    /**
     * Resets all NPCs in the Betty basement pens and respawns them.
     */
    @JvmStatic
    fun resetBettyBasementNPCs() {
        spawnBettyBasementNPCs()
    }

    /**
     * Removes all Betty basement NPCs.
     */
    @JvmStatic
    fun removeBettyNPCs() {
        CREATURES.values.forEach { it.first.clear() }
        CREATURES.clear()
    }

    /**
     * Returns the creature the player is currently carrying, if any.
     */
    private fun heldCreature(player: Player): Pen? =
        Pen.values().firstOrNull { it.itemId != -1 && inInventory(player, it.itemId) }

    /**
     * Checks if every creature is in its own pen and the holding pen is empty.
     */
    @JvmStatic
    fun isMenagerieSolved(): Boolean {
        if (CREATURES.containsKey(Pen.HOLDING)) return false
        return Pen.values()
            .filter { it != Pen.HOLDING }
            .all { pen -> CREATURES[pen]?.second == pen.itemId }
    }

    /**
     * Handles taking a creature out of a pen, or putting the carried creature into an empty pen.
     */
    @JvmStatic
    fun handlePenInteraction(player: Player, pen: Pen) {
        val existing = CREATURES[pen]
        val held = heldCreature(player)

        if (existing != null) {
            if (held != null) {
                sendMessage(player, "You can't carry more than one creature at a time.")
                return
            }
            if (freeSlots(player) < 1) {
                sendMessage(player, "You don't have enough inventory space.")
                return
            }
            val (npc, storedItem) = existing
            val npcName = getNPCName(npc.id)
            CREATURES.remove(pen)
            npc.clear()
            if (storedItem != -1) addItem(player, storedItem)
            sendMessage(player, "You remove the $npcName from the pen.")
            return
        }

        if (held == null) {
            sendMessage(player, "You have no creatures to put into the pen.")
            return
        }
        placeCreature(player, pen, held)
    }

    /**
     * Puts the given (carried) creature into the given pen.
     */
    @JvmStatic
    fun placeCreature(player: Player, pen: Pen, creature: Pen) {
        if (CREATURES.containsKey(pen)) {
            sendMessage(player, "You can't put a creature into a pen that already has one inside.")
            return
        }
        if (creature.itemId == -1 || !removeItem(player, creature.itemId)) {
            sendMessage(player, "You don't have the creature to put in the pen.")
            return
        }

        spawnCreature(pen, creature)

        val npcName = getNPCName(creature.npcId)
        val pronoun = if (npcName in listOf("Rat", "Bat")) "He" else "She"
        val happy = pen != Pen.HOLDING && pen == creature
        sendMessage(
            player,
            if (happy) "You put the $npcName into the pen. $pronoun looks extremely happy here."
            else "You put the $npcName into the pen. $pronoun doesn't seem very happy here."
        )

        countMove(player)
    }

    /**
     * Counts a creature move. Too many moves resets the puzzle through Lottie.
     */
    private fun countMove(player: Player) {
        val moves = getAttribute(player, GameAttributes.QUEST_SWEPT_AWAY_CREATURE_INTER, 0) + 1
        setAttribute(player, GameAttributes.QUEST_SWEPT_AWAY_CREATURE_INTER, moves)
        if (moves <= MAX_CREATURE_MOVES) return

        Pen.values().filter { it.itemId != -1 }.forEach { if (inInventory(player, it.itemId)) removeAll(player, it.itemId) }
        setAttribute(player, GameAttributes.QUEST_SWEPT_AWAY_CREATURE_INTER, 0)
        sendMessages(
            player,
            "The creature leaps out of your grasp and scampers away.",
            "You can't take a creature into a chamber that has another creature inside."
        )
        sendPlayerDialogue(player, "I don't think I should take this little guy into a room that has another creature in it.")
        addDialogueAction(player) { p, _ -> openDialogue(p, SweptAwayPlugin.LottieSupportDialogue()) }
    }

    /**
     * Updates the magic slate interface with all current creatures.
     */
    @JvmStatic
    fun checkMagicSlate(player: Player) {
        openInterface(player, PEN_TABLE_INTERFACE)
        COMPONENT_TO_PEN.forEach { (component, penId) ->
            val pen = Pen.fromId(penId) ?: return@forEach
            val storedItem = CREATURES[pen]?.second
            val creature = if (storedItem == null || storedItem == -1) null else Pen.values().firstOrNull { it.itemId == storedItem }
            val modelId = creature?.modelId ?: -1
            sendModelOnInterface(player, PEN_TABLE_INTERFACE, component, modelId, -1)
        }
    }

    /**
     * Spawns all lines in clearing area.
     */
    fun spawnAllLines() {
        allLines.values.forEach { line ->
            addScenery(line.sceneryId, line.location, line.rotation, line.type)
        }
    }

    /**
     * Represents a single line objects.
     *
     * @property sceneryId the object id.
     * @property location the location.
     * @property rotation the rotation.
     * @property type the type.
     */
    data class LineScenery(
        val sceneryId: Int,
        val location: Location,
        val rotation: Int,
        val type: Int
    )

    val allLines: Map<Int, LineScenery> = listOf(
        LineScenery(39363, Location.create(3294, 4515, 0), 2, 22),
        LineScenery(39364, Location.create(3295, 4515, 0), 2, 22),
        LineScenery(39365, Location.create(3296, 4515, 0), 2, 22),
        LineScenery(39366, Location.create(3297, 4515, 0), 4, 22),

        LineScenery(39367, Location.create(3298, 4515, 0), 2, 22),
        LineScenery(39368, Location.create(3299, 4515, 0), 2, 22),
        LineScenery(39369, Location.create(3300, 4515, 0), 2, 22),
        LineScenery(39370, Location.create(3301, 4515, 0), 4, 22),

        LineScenery(39371, Location.create(3293, 4514, 0), 3, 22),
        LineScenery(39372, Location.create(3292, 4514, 0), 1, 22),
        LineScenery(39373, Location.create(3292, 4513, 0), 1, 22),

        LineScenery(39374, Location.create(3294, 4514, 0), 2, 22),
        LineScenery(39375, Location.create(3295, 4514, 0), 1, 22),
        LineScenery(39376, Location.create(3295, 4513, 0), 4, 22),

        LineScenery(39377, Location.create(3297, 4514, 0), 3, 22),
        LineScenery(39378, Location.create(3296, 4514, 0), 1, 22),
        LineScenery(39379, Location.create(3296, 4513, 0), 1, 22),

        LineScenery(39380, Location.create(3298, 4514, 0), 2, 22),
        LineScenery(39381, Location.create(3299, 4514, 0), 1, 22),
        LineScenery(39382, Location.create(3299, 4513, 0), 4, 22),

        LineScenery(39383, Location.create(3301, 4514, 0), 3, 22),
        LineScenery(39384, Location.create(3300, 4514, 0), 1, 22),
        LineScenery(39385, Location.create(3300, 4513, 0), 1, 22),

        LineScenery(39386, Location.create(3292, 4512, 0), 2, 22),
        LineScenery(39387, Location.create(3293, 4512, 0), 2, 22),
        LineScenery(39388, Location.create(3294, 4512, 0), 2, 22),
        LineScenery(39389, Location.create(3295, 4512, 0), 4, 22),

        LineScenery(39390, Location.create(3296, 4512, 0), 2, 22),
        LineScenery(39391, Location.create(3297, 4512, 0), 2, 22),
        LineScenery(39392, Location.create(3298, 4512, 0), 2, 22),
        LineScenery(39393, Location.create(3299, 4512, 0), 4, 22),

        LineScenery(39394, Location.create(3291, 4511, 0), 3, 22),
        LineScenery(39395, Location.create(3290, 4511, 0), 1, 22),
        LineScenery(39396, Location.create(3290, 4510, 0), 1, 22),

        LineScenery(39397, Location.create(3292, 4511, 0), 2, 22),
        LineScenery(39398, Location.create(3293, 4511, 0), 1, 22),
        LineScenery(39399, Location.create(3293, 4510, 0), 4, 22),

        LineScenery(39400, Location.create(3295, 4511, 0), 3, 22),
        LineScenery(39401, Location.create(3294, 4511, 0), 1, 22),
        LineScenery(39402, Location.create(3294, 4510, 0), 1, 22),

        LineScenery(39403, Location.create(3296, 4511, 0), 2, 22),
        LineScenery(39404, Location.create(3297, 4511, 0), 1, 22),
        LineScenery(39405, Location.create(3297, 4510, 0), 4, 22),

        LineScenery(39406, Location.create(3299, 4511, 0), 3, 22),
        LineScenery(39407, Location.create(3298, 4511, 0), 1, 22),
        LineScenery(39408, Location.create(3298, 4510, 0), 1, 22),

        LineScenery(39409, Location.create(3290, 4509, 0), 2, 22),
        LineScenery(39410, Location.create(3291, 4509, 0), 2, 22),
        LineScenery(39411, Location.create(3292, 4509, 0), 2, 22),
        LineScenery(39412, Location.create(3293, 4509, 0), 4, 22),

        LineScenery(39413, Location.create(3294, 4509, 0), 2, 22),
        LineScenery(39414, Location.create(3295, 4509, 0), 2, 22),
        LineScenery(39415, Location.create(3296, 4509, 0), 2, 22),
        LineScenery(39416, Location.create(3297, 4509, 0), 4, 22)
    ).associateBy { it.sceneryId }

    private val lineRows: List<List<LineScenery>> = listOf(
        listOf(39363, 39364, 39365, 39366),
        listOf(39367, 39368, 39369, 39370),
        listOf(39371, 39372, 39373),
        listOf(39374, 39375, 39376),
        listOf(39377, 39378, 39379),
        listOf(39380, 39381, 39382),
        listOf(39383, 39384, 39385),
        listOf(39386, 39387, 39388, 39389),
        listOf(39390, 39391, 39392, 39393),
        listOf(39394, 39395, 39396),
        listOf(39397, 39398, 39399),
        listOf(39400, 39401, 39402),
        listOf(39403, 39404, 39405),
        listOf(39406, 39407, 39408),
        listOf(39409, 39410, 39411, 39412),
        listOf(39413, 39414, 39415, 39416)
    ).map { row -> row.map { allLines.getValue(it) } }

    // Based on https://runescape.wiki/images/thumb/Pattern_triangles.png/250px-Pattern_triangles.png?aec80
    private val correctLineRows = setOf(2, 7, 8, 13)

    /**
     * Handles sweep logic.
     */
    fun sweepLines(player: Player, lineId: Int) {
        val rowIndex = lineRows.indexOfFirst { row -> row.any { it.sceneryId == lineId } }
        if (rowIndex < 0) return

        val row = lineRows[rowIndex]
        val sweptRows = player.getAttribute("swept_lines", mutableSetOf<Int>())

        if (!sweptRows.add(rowIndex)) {
            sendMessage(player, "You've already swept this line.")
            return
        }

        row.forEach { removeScenery(Scenery(it.sceneryId, it.location)) }
        visualize(player, SWEEP_ANIMATION, Graphics.DUST_BROOM_EMOTE_1866)
        player.setAttribute("swept_lines", sweptRows)

        checkPattern(player)
    }

    /**
     * Checks for correct pattern.
     */
    private fun checkPattern(player: Player) {
        val sweptRows = player.getAttribute("swept_lines", mutableSetOf<Int>())

        when {
            correctLineRows.all { it in sweptRows } -> {
                runTask(player, 2) {
                    visualize(player, -1, BROOM_ENCHANTMENT_GFX)
                    sendPlayerDialogue(player, "Woah, I felt that down to my toes!", FaceAnim.SCARED)
                    // Never move the quest backwards (or start it) from here.
                    val questStage = getQuestStage(player, Quests.SWEPT_AWAY)
                    if (questStage in 1..2) {
                        setQuestStage(player, Quests.SWEPT_AWAY, 3)
                    }
                }
                player.removeAttribute("swept_lines")
            }
            sweptRows.size >= 4 -> {
                sendNPCDialogue(player, NPCs.AGGIE_8207, "You've already swept away four lines. Come talk to me and I'll set the sand pattern up for you again.")
                player.removeAttribute("swept_lines")
            }
        }
    }

    /**
     * Resets all lines in the clearing area.
     */
    fun resetLines(player: Player) {
        allLines.values.forEach { line -> removeScenery(Scenery(line.sceneryId, line.location)) }
        allLines.values.forEach { addScenery(it.sceneryId, it.location, it.rotation, it.type) }
        player.removeAttribute("swept_lines")
    }

    /**
     * Gets the varbit which stores the label stuck on the given crate.
     */
    @JvmStatic
    fun crateVarbit(crateId: Int): Int = when (crateId) {
        T_CRATE, GUS_CRATES_LABELLED[0] -> VARBIT_TOAD_CRATE_LABEL
        N_CRATE, GUS_CRATES_LABELLED[1] -> VARBIT_NEWT_CRATE_LABEL
        NT_CRATE, GUS_CRATES_LABELLED[2] -> VARBIT_NEWT_AND_TOAD_CRATE_LABEL
        else -> -1
    }

    private fun labelValue(itemId: Int): Int = when (itemId) {
        Items.NEWT_LABEL_14065 -> 1
        Items.TOAD_LABEL_14066 -> 2
        Items.NEWTS_AND_TOADS_LABEL_14067 -> 3
        else -> 0
    }

    private fun labelItem(value: Int): Int = when (value) {
        1 -> Items.NEWT_LABEL_14065
        2 -> Items.TOAD_LABEL_14066
        3 -> Items.NEWTS_AND_TOADS_LABEL_14067
        else -> -1
    }

    /**
     * Sticks a label on a crate and checks the result once all three crates have one.
     */
    @JvmStatic
    fun labelCrate(player: Player, labelItem: Int, crateId: Int) {
        val varbit = crateVarbit(crateId)
        val value = labelValue(labelItem)
        if (varbit == -1 || value == 0) return
        handleCrateLabelling(player, labelItem, varbit, value)
        checkGusTask(player)
    }

    /**
     * Removes the label from a crate and gives it back to the player.
     */
    @JvmStatic
    fun removeCrateLabel(player: Player, crateId: Int) {
        val varbit = crateVarbit(crateId)
        if (varbit == -1) return
        val item = labelItem(getVarbit(player, varbit))
        if (item == -1) return
        if (freeSlots(player) < 1) {
            sendMessage(player, "You don't have enough inventory space.")
            return
        }
        setVarbit(player, varbit, 0, true)
        addItem(player, item)
        sendMessage(player, "You peel the label off the crate.")
    }

    /**
     * Checks for correct crate labelling (only once every crate has a label).
     */
    @JvmStatic
    fun checkGusTask(player: Player) {
        val newt = getVarbit(player, VARBIT_NEWT_CRATE_LABEL)
        val toad = getVarbit(player, VARBIT_TOAD_CRATE_LABEL)
        val both = getVarbit(player, VARBIT_NEWT_AND_TOAD_CRATE_LABEL)

        if (newt == 0 || toad == 0 || both == 0) return

        if (both == 3 && toad == 2 && newt == 1) {
            sendNPCDialogue(player, NPCs.GUS_8205, "Hurray! I do believe that you've labelled the crates correctly! Ms Hetty will be so pleased.")
            removeAttribute(player, GameAttributes.QUEST_SWEPT_AWAY_LABELS)
            setAttribute(player, GameAttributes.QUEST_SWEPT_AWAY_LABELS_COMPLETE, true)
        } else {
            openDialogue(player, SweptAwayPlugin.GusSupportDialogue())
        }
    }

    /**
     * Handles labelling a crate logic.
     */
    @JvmStatic
    fun handleCrateLabelling(player: Player, used: Int, varbit: Int, value: Int) {
        if (removeItem(player, used.asItem())) {
            sendMessage(player, "You place the label on the crate.")
            setVarbit(player, varbit, value, true)
        }
    }

    /**
     * Resets gus task.
     */
    @JvmStatic
    fun resetGusTask(player: Player) {
        lock(player, 6)
        GameWorld.Pulser.submit(
            object : Pulse() {
                var counter = 0

                override fun pulse(): Boolean {
                    when (counter++) {
                        0 -> openInterface(player, Components.FADE_TO_BLACK_120)
                        3 -> {
                            setVarbit(player, VARBIT_NEWT_CRATE_LABEL, 0, true)
                            setVarbit(player, VARBIT_TOAD_CRATE_LABEL, 0, true)
                            setVarbit(player, VARBIT_NEWT_AND_TOAD_CRATE_LABEL, 0, true)
                            LABEL_ITEMS.forEach { label ->
                                if (!inInventory(player, label)) addItemOrDrop(player, label, 1)
                            }
                        }
                        6 -> {
                            unlock(player)
                            openInterface(player, Components.FADE_FROM_BLACK_170)
                            return true
                        }
                    }
                    return false
                }
            }
        )
    }

    /**
     * Unlocks a Halloween emote and shows it in an interface.
     */
    @JvmStatic
    fun unlockHalloweenEmotes(player: Player, emote: Emotes, emoteName: String) {
        unlockEmote(player, emote)
        openInterface(player, Components.DOUBLEOBJBOX_131).also {
            sendModelOnInterface(player, Components.DOUBLEOBJBOX_131, 2, BROOM_MODEL, -1)
            sendAngleOnInterface(player, Components.DOUBLEOBJBOX_131, 2, 850, 200, 1500)
            sendString(player, "You've just learned the ${core.tools.DARK_RED}'$emoteName'</col> emote!", Components.DOUBLEOBJBOX_131, 1)
        }
    }
}
