package content.region.kandarin.quest.luc2.plugin

import content.region.kandarin.quest.luc2.dialogue.KhazardLaundererDialogue
import core.api.*
import core.game.global.action.ClimbActionHandler
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.interaction.QueueStrength
import core.game.world.map.Location
import core.game.world.update.flag.context.Animation
import shared.consts.*

class WhileGuthixSleepsPlugin : InteractionListener {

    override fun defineListeners() {
        on(NPCs.KHAZARD_LAUNDERER_8428, IntType.NPC, "talk-to") { player, _ ->
            openDialogue(player, KhazardLaundererDialogue())
            return@on true
        }

        onUseWith(IntType.ITEM, Items.ENRICHED_SNAPDRAGON_14487, Items.TRUTH_SERUM_6952) { player, used, with ->
            sendMessages(
                player,
                "You mix the enriched snapdragon into the truth serum.",
                "It dissolves immediately and gives off a delicious, delicate aroma.",
            )
            animate(player, 3283)
            runTask(player, 1) {
                if (removeItem(player, used.asItem()) && removeItem(player, with.asItem())) {
                    addItem(player, Items.SUPER_TRUTH_SERUM_14488)
                }
            }
            return@onUseWith true
        }

        on(Scenery.TRAPDOOR_TABLE_40897, IntType.SCENERY, "open") { player, _ ->
            playAudio(player,  Sounds.TRAP_DOOR_OPEN_89)
            sendMessage(player, "You open the trapdoor.")
            setVarbit(player, 5511, 3)
            return@on true
        }

        on(Scenery.TRAPDOOR_40898, IntType.SCENERY, "climb-into", "close") { player, _ ->
            when (getUsedOption(player)) {
                "close" -> {
                    playAudio(player,  Sounds.TRAP_DOOR_CLOSE_88)
                    sendMessage(player, "You close the trapdoor.")
                    setVarbit(player, 5511, 2)
                    return@on true
                }

                else -> {
                    lock(player, 6)
                    queueScript(player,1,QueueStrength.SOFT) { stage ->
                        when (stage) {
                            0 -> {
                                openInterface(player, Components.FADE_TO_BLACK_115)
                                return@queueScript delayScript(player, 3)
                            }
                            1 -> {
                                teleport(player, Location(2035, 4379, 0))
                                return@queueScript delayScript(player, 1)
                            }
                            2 -> {
                                openInterface(player, Components.FADE_FROM_BLACK_170)
                                return@queueScript delayScript(player, 2)
                            }
                            3 -> {
                                closeOverlay(player)
                                player.musicPlayer.unlock(Music.DANGEROUS_LOGIC_579)
                                return@queueScript stopExecuting(player)
                            }
                            else -> return@queueScript stopExecuting(player)
                        }
                    }
                }
            }
            return@on true
        }

        /*
         * Handles interaction with tile at black knights fortress basement.
         */

        on(Scenery.TILE_40994, IntType.SCENERY, "look-at", "search") { player, _ ->
            val op = getUsedOption(player)
            when(op) {
                "look-at" -> sendMessages(player, "It appears to be an everyday sort of tile for decorating the floor, but it has some", "strange markings.")
                "search"  -> sendDialogueLines(player, "On closer examination, you notice an orb symbol carved into the tile.")
            }
            return@on true
        }

        /*
         * Handles climb down to the black knight Catacombs.
         */

        on(Scenery.TRAPDOOR_40995, IntType.SCENERY, "climb-down") { player, _ ->
            ClimbActionHandler.climb(player, Animation(Animations.HUMAN_BURYING_BONES_827), Location(3017, 9923, 1))
            return@on true
        }
    }
}
