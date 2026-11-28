package content.region.asgarnia.port_sarim.plugin

import content.data.Quests

import content.region.asgarnia.port_sarim.dialogue.*
import core.api.*
import core.game.dialogue.DialogueFile
import core.game.global.action.DoorActionHandler
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.entity.npc.NPC
import core.game.world.map.Location
import core.tools.RandomFunction
import shared.consts.*

class PortSarimPlugin : InteractionListener {

    companion object {
        private val DOORS = intArrayOf(Scenery.CELL_DOOR_9563, Scenery.DOOR_9565)
        private val MONKS_OF_ENTRANA = intArrayOf(NPCs.SHIPMONK1_B_2728, NPCs.SHIPMONK_657, NPCs.SHIPMONK1_C_2729, 2730, NPCs.SHIPMONK2_C_2731, NPCs.SHIPMONK2_658)
        private val SEAMAN = intArrayOf(NPCs.CAPTAIN_TOBIAS_376, NPCs.SEAMAN_LORRIS_377, NPCs.SEAMAN_THRESNOR_378)
        private val GUARD_FORCE_CHAT =
            arrayOf(
                "Hmph... heh heh heh...",
                "Mmmm... big pint of beer... kebab...",
                "Mmmmmm... donuts...",
                "Guh.. mwww... zzzzzz...",
            )
    }

    override fun defineListeners() {

        /*
         * Handles taking Ahab's beer from the ground.
         */

        on(Items.AHABS_BEER_6561, IntType.GROUND_ITEM, "take") { player, node ->
            face(player, node)
            animate(player, Animations.HUMAN_PICKUPTABLE_832)
            player.dialogueInterpreter.open(NPCs.AHAB_2692, findNPC(NPCs.AHAB_2692), false)
            return@on true
        }

        /*
         * Handles paying the fare to the Seaman NPCs.
         */

        on(SEAMAN, IntType.NPC, "pay-fare") { player, node ->
            val npc = node as NPC
            player.dialogueInterpreter.open(npc.id, npc, true)
            return@on true
        }

        /*
         * Handles opening the Wydin store door.
         */

        on(Scenery.DOOR_2069, IntType.SCENERY, "open") { player, node ->
            val needsApron = !inEquipment(player, Items.WHITE_APRON_1005) && player.location.x == 3012

            if (needsApron) {
                player.dialogueInterpreter.open(NPCs.WYDIN_557, true, true)
            } else {
                DoorActionHandler.handleAutowalkDoor(player, node.asScenery())
            }

            return@on true
        }

        /*
         * Handles searching the Wydin banana crate.
         */

        on(Scenery.CRATE_2071, IntType.SCENERY, "search") { player, _ ->
            sendMessage(player, "There are lots of bananas in the crate.")

            val rumAvailable = player.getAttribute("wydin-rum", false)

            if (!rumAvailable) {
                setTitle(player, 2)
                sendOptions(player, "Do you want to take a banana?", "Yes.", "No.")
                addDialogueAction(player) { _, option ->
                    closeDialogue(player)
                    if (option == 2) {
                        if (freeSlots(player) == 0) {
                            sendMessage(player, "Not enough inventory space to take a banana.")
                            return@addDialogueAction
                        }
                        lock(player, 2)
                        animate(player, Animations.HUMAN_PICKUPTABLE_832)
                        sendMessage(player, "You take a banana.")
                        addItem(player, Items.BANANA_1963)
                    }
                }
            } else {
                if (freeSlots(player) == 0) {
                    sendMessage(player, "Not enough inventory space to take your rum.")
                    return@on true
                }
                lock(player, 2)
                animate(player, Animations.HUMAN_PICKUPTABLE_832)
                sendMessage(player, "You find your bottle of rum in amongst the bananas.")
                addItem(player, Items.KARAMJAN_RUM_431)
                removeAttributes(player, "wydin-rum", "stashed-rum")

            }
            return@on true
        }

        /*
         * Handles interacting with doors (open or pick-lock).
         */

        on(DOORS, IntType.SCENERY, "open", "pick-lock") { player, _ ->
            when (getUsedOption(player)) {
                "open" -> sendMessage(player, "The door is securely locked.")
                "pick-lock" -> {
                    if (player.location.y <= 3187) {
                        sendMessage(player, "You simply cannot find a way to pick the lock from this side.")
                    } else {
                        sendMessage(player, "The door is securely locked.")
                    }
                }
            }
            return@on true
        }

        /*
         * Handles taking a boat from the Monks of Entrana.
         */

        on(MONKS_OF_ENTRANA, IntType.NPC, "take-boat") { player, node ->
            openDialogue(player, TakeBoatDialogue(), node.asNpc())
            return@on true
        }

        /*
         * Handles talking to the sleeping guard NPC.
         */

        on(NPCs.JAIL_GUARD_SLEEPING_2704, IntType.NPC, "talk-to") { player, node ->
            sendChat((node as NPC), GUARD_FORCE_CHAT[RandomFunction.random(GUARD_FORCE_CHAT.size)])
            runTask(player, 1) {
                sendPlayerDialogue(player, "Maybe I should let him sleep.")
            }
            return@on true
        }

        /*
         * Handles attacking Wormbrain NPC.
         */

        on(NPCs.WORMBRAIN_745, IntType.NPC, "attack") { player, node ->
            if (getQuestStage(player, Quests.DRAGON_SLAYER.questName) != 20) {
                sendMessage(player, "The goblin is already in prison. You have no reason to attack him.")
            } else {
                player.properties.combatPulse.attack(node)
            }
            return@on true
        }

        /*
         * Handles entering the icy cavern.
         */

        on(Scenery.ICY_CAVERN_33174, IntType.SCENERY, "enter") { player, _ ->
            player.properties.teleportLocation = Location(3056, 9562, 0)
            sendMessage(player, "You leave the icy cavern.")
            return@on true
        }

        /*
         * Handles exiting the icy cavern.
         */

        on(Scenery.CAVE_33173, IntType.SCENERY, "exit") { player, _ ->
            openDialogue(player, EntranceDialogue())
            return@on true
        }

        /*
         * Handles talking to the npc around port.
         */

        on(NPCs.VC_BELLEMORDE_2942, IntType.NPC, "talk-to") { player, node ->
            openDialogue(player, CatBellemordeDialogue(), node.asNpc())
            return@on true
        }

        on(NPCs.GRUM_556, IntType.NPC, "talk-to") { player, node ->
            openDialogue(player, GrumDialogue(), node.asNpc())
            return@on true
        }

        on(NPCs.GERRANT_558, IntType.NPC, "talk-to") { player, node ->
            openDialogue(player, GerrantDialogue(), node.asNpc())
            return@on true
        }

        on(NPCs.JACK_SEAGULL_2690, IntType.NPC, "talk-to") { player, node ->
            openDialogue(player, JackSeagullDialogue(), node.asNpc())
            return@on true
        }

        on(NPCs.LONGBOW_BEN_2691, IntType.NPC, "talk-to") { player, node ->
            openDialogue(player, LongbowBenDialogue(), node.asNpc())
            return@on true
        }

        on(NPCs.MALIGNIUS_MORTIFER_2713, IntType.NPC, "talk-to") { player, node ->
            openDialogue(player, MaligniusMortiferDialogue(), node.asNpc())
            return@on true
        }

        on(intArrayOf(NPCs.SHIPMONK_657, NPCs.SHIPMONK2_658, NPCs.SHIPMONK1_B_2728, NPCs.SHIPMONK1_C_2729, NPCs.SHIPMONK2_B_2730, NPCs.SHIPMONK2_C_2731), IntType.NPC, "talk-to") { player, node ->
            openDialogue(player, MonkOfEntranaDialogue(), node.asNpc())
            return@on true
        }

        on(NPCs.CATCON_NPC_DELIVERY_DWARF_7115, IntType.NPC, "talk-to") { player, node ->
            openDialogue(player, ThakiTheDeliveryDwarfDialogue(), node.asNpc())
            return@on true
        }

    }

    private class EntranceDialogue : DialogueFile() {
        override fun handle(componentID: Int, buttonID: Int) {
            when(stage) {
                0 -> sendDialogueLines(player!!, "STOP! The creatures in this cave are VERY Dangerous. Are you", "sure you want to enter?").also { stage++ }
                1 -> options("Yes, I'm not afraid of death!", "No thanks, I don't want to die!").also { stage++ }
                2 -> when(buttonID) {
                    1 -> {
                        end()
                        val location = Location.create(3056, 9555, 0)
                        player!!.properties.teleportLocation = location
                        sendMessage(player!!, "You venture into the icy cavern.")
                    }
                    2 -> {
                        end()
                    }
                }
            }
        }
    }
}
