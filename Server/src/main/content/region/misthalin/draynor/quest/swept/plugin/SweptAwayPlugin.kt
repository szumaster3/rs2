package content.region.misthalin.draynor.quest.swept.plugin

import content.data.GameAttributes
import core.api.*
import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.interaction.InterfaceListener
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.link.emote.Emotes
import core.tools.END_DIALOGUE
import core.tools.RandomFunction
import shared.consts.*

class SweptAwayPlugin : InteractionListener, InterfaceListener {

    init {
        SweptUtils.spawnAllLines()
    }

    override fun defineListeners() {

        /*
         * Handles opening the Betty's trapdoor.
         */

        on(Scenery.TRAPDOOR_39274, IntType.SCENERY, "open") { player, _ ->
            if (!isQuestComplete(player, Quests.SWEPT_AWAY)) {
                setVarbit(player, SweptUtils.VARBIT_BETTY_TRAPDOOR, 1)
            } else sendMessage(player, "It's locked.")
            return@on true
        }

        on(Scenery.TRAPDOOR_39275, IntType.SCENERY, "close") { player, _ ->
            setVarbit(player, SweptUtils.VARBIT_BETTY_TRAPDOOR, 0)
            return@on true
        }

        /*
         * Handles opening the Rimmington trapdoor if the quest is incomplete.
         */

        on(Scenery.TRAPDOOR_39330, IntType.SCENERY, "open") { player, _ ->
            if (!isQuestComplete(player, Quests.SWEPT_AWAY) && getQuestStage(player, Quests.SWEPT_AWAY) >= 1) {
                setVarbit(player, SweptUtils.VARBIT_RIMMINGTON_TRAPDOOR, 1)
            } else sendMessage(player, "It's locked.")
            return@on true
        }

        /*
         * Handles closing the Rimmington trapdoor.
         */

        on(Scenery.TRAPDOOR_39331, IntType.SCENERY, "close") { player, _ ->
            setVarbit(player, SweptUtils.VARBIT_RIMMINGTON_TRAPDOOR, 0)
            return@on true
        }

        /*
         * Handles talking to NPC Norman.
         */

        on(NPCs.NORMAN_8203, IntType.NPC, "talk-to") { player, _ ->
            sendNPCDialogue(player, NPCs.NORMAN_8203, "Moo-ooooooooooo?", FaceAnim.CHILD_NORMAL)
            addDialogueAction(player) { p, _ ->
                sendNPCDialogue(
                    p,
                    NPCs.MAGGIE_8078,
                    "I wouldn't pester Norman at the mo - he's a bit on edge, what with Babe being poorly. The sneezing really gets to him!",
                    FaceAnim.NEUTRAL
                )
            }
            return@on true
        }

        /*
         * Handles talking to NPC Babe.
         */

        on(NPCs.BABE_8204, IntType.NPC, "talk-to") { player, _ ->
            sendNPCDialogue(player, NPCs.BABE_8204, "*Sniff* *sniffle* *snort* Ah-choo!", FaceAnim.CHILD_NORMAL)
            addDialogueAction(player) { p, _ ->
                sendNPCDialogue(
                    p,
                    NPCs.MAGGIE_8078,
                    "I wouldn't get too close to her if I were you. I gave her a portion of that goulash, so she's on the mend, but she still might be a bit contagious.",
                    FaceAnim.NEUTRAL
                )
            }
            return@on true
        }

        /*
         * Handles stirring the cauldron at Maggie's camp.
         */

        on(Scenery.MAGGIE_S_CAULDRON_39230, IntType.SCENERY, "stir") { player, _ ->
            lock(player, 1)
            val questStage = getQuestStage(player, Quests.SWEPT_AWAY)
            if (questStage >= 4) {
                if (questStage == 4 && !inInventory(player, Items.BROOMSTICK_14057)) {
                    sendMessage(player, "You need the enchanted broomstick to stir the cauldron properly.")
                    return@on true
                }
                animate(player, SweptUtils.STIR_WITH_BROOMSTICK_ANIMATION)
                if (questStage == 4) {
                    setAttribute(player, GameAttributes.QUEST_SWEPT_AWAY_CAULDRON_STIRRED, true)
                }
                sendPlayerDialogue(player, "There, that should do it.", FaceAnim.NEUTRAL)
            } else {
                animate(player, SweptUtils.STIR_ANIMATION)
                sendPlayerDialogue(player, "What was that?", FaceAnim.EXTREMELY_SHOCKED)
                addDialogueAction(player) { p, _ -> openDialogue(p, MaggieSceneryInteraction()) }
            }
            return@on true
        }

        /*
         * Handles sweeping the lines of the area during the quest.
         */

        SweptUtils.allLines.forEach { (lineId, _) ->
            on(lineId, IntType.SCENERY, "sweep-away") { player, _ ->
                SweptUtils.sweepLines(player, lineId)
                return@on true
            }
        }

        /*
         * Handles extracting a creature from Gus's (wrongly labelled) crates.
         */

        on(SweptUtils.GUS_CRATES, IntType.SCENERY, "extract-creature") { player, node ->
            val rand = RandomFunction.random(0, 1)
            when (node.id) {
                SweptUtils.N_CRATE -> {
                    animate(player, SweptUtils.TAKE_NEWT_FROM_CRATE_ANIMATION)
                    sendMessage(player, "You pull out a newt. It wriggles out of your hand and back into the crate.")
                    sendPlayerDialogue(player, "Bleh! A newt!", FaceAnim.EXTREMELY_SHOCKED)
                }
                SweptUtils.T_CRATE -> {
                    animate(player, SweptUtils.TAKE_FROG_FROM_CRATE_ANIMATION)
                    sendMessage(player, "You pull out a toad. It wriggles out of your hand and back into the crate.")
                    sendPlayerDialogue(player, "Yuk! A toad!", FaceAnim.EXTREMELY_SHOCKED)
                }
                SweptUtils.NT_CRATE -> {
                    if (rand == 0) {
                        animate(player, SweptUtils.TAKE_FROG_FROM_CRATE_ANIMATION)
                        sendMessage(player, "You pull out a toad. It wriggles out of your hand and back into the crate.")
                        sendPlayerDialogue(player, "Yuk! A toad!", FaceAnim.EXTREMELY_SHOCKED)
                    } else {
                        animate(player, SweptUtils.TAKE_NEWT_FROM_CRATE_ANIMATION)
                        sendMessage(player, "You pull out a newt. It wriggles out of your hand and back into the crate.")
                        sendPlayerDialogue(player, "Bleh! A newt!", FaceAnim.EXTREMELY_SHOCKED)
                    }
                }
            }
            return@on true
        }

        /*
         * Handles extracting a creature from Gus's labelled crates.
         * Only the crate that really contains newts gives one to the player.
         */

        on(SweptUtils.GUS_CRATES_LABELLED, IntType.SCENERY, "extract-creature") { player, node ->
            if (!getAttribute(player, GameAttributes.QUEST_SWEPT_AWAY_LABELS_COMPLETE, false)) {
                sendNPCDialogueLines(
                    player, NPCs.GUS_8205,
                    FaceAnim.HALF_THINKING,false, "No! remember what I said: we can't waste the stock.",
                    "One look should be enough. We wouldn't want to get",
                    "Ms. Hetty Annoyed."
                )
                return@on true
            }
            when (node.id) {
                SweptUtils.GUS_CRATES_LABELLED[1] -> {
                    if (freeSlots(player) < 1) {
                        sendMessage(player, "You don't have enough inventory space.")
                    } else {
                        animate(player, SweptUtils.TAKE_NEWT_FROM_CRATE_ANIMATION)
                        sendMessage(player, "You fish around in the crate and extract a newt.")
                        addItem(player, Items.NEWT_14064)
                    }
                }
                SweptUtils.GUS_CRATES_LABELLED[0] -> {
                    animate(player, SweptUtils.TAKE_FROG_FROM_CRATE_ANIMATION)
                    sendMessage(player, "You pull out a toad. It wriggles out of your hand and back into the crate.")
                }
                else -> {
                    animate(player, SweptUtils.TAKE_FROG_FROM_CRATE_ANIMATION)
                    sendMessage(player, "You pull out a creature, but it wriggles out of your hand and back into the crate.")
                }
            }
            return@on true
        }

        /*
         * Handles removing the label from Gus's crates (the label goes back to the player).
         */

        on(SweptUtils.GUS_CRATES_LABELLED, IntType.SCENERY, "remove-label") { player, node ->
            SweptUtils.removeCrateLabel(player, node.id)
            return@on true
        }

        /*
         * Handles sticking any of the three labels on any of the three crates.
         */

        for (label in SweptUtils.LABEL_ITEMS) {
            onUseWith(IntType.SCENERY, label, *SweptUtils.GUS_CRATES) { player, used, with ->
                SweptUtils.labelCrate(player, used.id, with.id)
                return@onUseWith true
            }
        }

        /*
         * Handles viewing the magic slate. Opens the pen table interface.
         */

        on(Items.MAGIC_SLATE_14069, IntType.ITEM, "view") { player, _ ->
            SweptUtils.checkMagicSlate(player)
            return@on true
        }

        /*
         * Handles opening the lottie chest (only once the menagerie is in order).
         */

        on(Scenery.CHEST_39278, IntType.SCENERY, "open") { player, _ ->
            if (!SweptUtils.isMenagerieSolved()) {
                sendMessage(player, "The chest is magically sealed. Order must be restored to the menagerie first.")
            } else {
                setVarbit(player, SweptUtils.VARBIT_LOTTIE_CHEST, 1)
            }
            return@on true
        }

        /*
         * Handles searching the chest for Betty's wand.
         */

        on(Scenery.CHEST_39279, IntType.SCENERY, "search") { player, _ ->
            if (hasAnItem(player, Items.BETTYS_WAND_14068).container == null) {
                sendMessage(player, "You search the chest and find Betty's wand.")
                addItemOrDrop(player, Items.BETTYS_WAND_14068)
                setAttribute(player, GameAttributes.QUEST_SWEPT_AWAY_BETTY_WAND, true)
            } else sendMessage(player, "You search the chest but find nothing.")
            return@on true
        }

        /*
         * Handles closing the chest.
         */

        on(Scenery.CHEST_39279, IntType.SCENERY, "close") { player, _ ->
            setVarbit(player, SweptUtils.VARBIT_LOTTIE_CHEST, 0)
            return@on true
        }

        val penItems = SweptUtils.Pen.values().mapNotNull { if (it.itemId != -1) it.itemId else null }.toIntArray()
        val penIds = SweptUtils.Pen.values().map { it.penId }.toIntArray()

        /*
         * Handles using a creature with a pen.
         */

        penItems.forEach { itemId ->
            onUseWith(IntType.SCENERY, itemId, *penIds) { player, used, with ->
                val pen = SweptUtils.Pen.fromId(with.id) ?: return@onUseWith true
                val creature = SweptUtils.Pen.values().firstOrNull { it.itemId == used.id } ?: return@onUseWith true
                SweptUtils.placeCreature(player, pen, creature)
                return@onUseWith true
            }
        }

        /*
         * Handles moving the creature in the pen.
         */

        on(penIds, IntType.SCENERY, "move-creature") { player, node ->
            val pen = SweptUtils.Pen.fromId(node.id) ?: return@on true
            SweptUtils.handlePenInteraction(player, pen)
            return@on true
        }

        /*
         * Handles use broomstick on kardia.
         */

        onUseWith(IntType.NPC, Items.BROOMSTICK_14057, NPCs.KARDIA_992) { player, used, with ->
            if (inInventory(player, used.id) && getAttribute(player, GameAttributes.QUEST_SWEPT_AWAY_KARDIA_ENCH_RECEIVED, false)) {
                sendPlayerDialogue(player, "Oh, right - I already had this enchanted here. Sorry to bother you.")
            } else {
                openDialogue(player, with.id, with.id)
            }
            return@onUseWith true
        }
    }

    override fun defineInterfaceListeners() {
        on(SweptUtils.PEN_TABLE_INTERFACE) { player, _, _, buttonID, _, _ ->
            if (buttonID == 4) closeInterface(player)
            return@on true
        }
    }

    /*
     * Handles interactions with the Maggie scenery during the quest.
     * Unlocks the next missing Halloween emote each time the cauldron is stirred early.
     */

    class MaggieSceneryInteraction : DialogueFile() {
        override fun handle(componentID: Int, buttonID: Int) {
            val emotes = listOf(
                Pair(Emotes.TRICK, "Trick"),
                Pair(Emotes.SCARED, "Terrified"),
                Pair(Emotes.ZOMBIE_HAND, "Zombie hand")
            )
            val next = emotes.firstOrNull { !hasEmote(player!!, it.first) }
            if (next == null) {
                end()
                return
            }
            SweptUtils.unlockHalloweenEmotes(player!!, next.first, next.second)
        }
    }

    /*
     * Handles restart task at the basement with Lottie.
     */

    class LottieSupportDialogue : DialogueFile() {
        override fun handle(componentID: Int, buttonID: Int) {
            npc = NPC(NPCs.LOTTIE_8206)
            when (stage) {
                0 -> npc(FaceAnim.SAD, "Eh? Something looks very wrong. Not all the creatures", "are there!").also { stage++ }
                1 -> npc(FaceAnim.SAD, "I'll just put things back to how they were and have a", "look for the missing critters.").also { stage++ }
                2 -> {
                    openInterface(player!!, Components.FADE_TO_BLACK_120)
                    openInterface(player!!, Components.FADE_FROM_BLACK_170)
                    SweptUtils.resetBettyBasementNPCs()
                    SweptUtils.Pen.values().mapNotNull { if (it.itemId != -1) it.itemId else null }
                        .forEach { id -> if (inInventory(player!!, id)) removeAll(player!!, id) }
                    setAttribute(player!!, GameAttributes.QUEST_SWEPT_AWAY_CREATURE_INTER, 0)
                    stage++
                    handle(componentID, buttonID)
                }
                3 -> npc(FaceAnim.HAPPY, "Not to worry - I found everyone! All the critters are", "accounted for, you're fine to start over.").also { stage = END_DIALOGUE }
            }
        }
    }

    /*
     * Handles interactions with Gus for the creature pen task.
     */

    class GusSupportDialogue : DialogueFile() {
        override fun handle(componentID: Int, buttonID: Int) {
            npc = NPC(NPCs.GUS_8205)
            when (stage) {
                0 -> npc(
                    "I see you've put new labels on all the crates. Oh, newts!",
                    "This is so confusing, but I think something is wrong",
                    "with the labelling."
                ).also { stage++ }
                1 -> npc(
                    "Please can you give it another go? Do you want me to",
                    "put everything back to how it was for you?"
                ).also { stage++ }
                2 -> options("Yes, please.", "No, I think I can solve this.", "What's wrong with the way I did it?", "But, Gus...").also { stage++ }
                3 -> when (buttonID) {
                    1 -> player("Yes, please.").also { stage++ }
                    2 -> player("No, I think I can solve this.").also { stage = END_DIALOGUE }
                    3 -> player("What's wrong with the way I did it?").also { stage = END_DIALOGUE }
                    4 -> player("But, Gus...").also { stage = END_DIALOGUE }
                }
                4 -> {
                    end()
                    SweptUtils.resetGusTask(player!!)
                }
            }
        }
    }
}
