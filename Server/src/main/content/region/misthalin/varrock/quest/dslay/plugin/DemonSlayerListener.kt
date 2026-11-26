package content.region.misthalin.varrock.quest.dslay.plugin

import content.data.Quests

import core.api.*
import core.game.dialogue.FaceAnim
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.entity.npc.NPC
import core.game.node.item.Item
import core.game.world.map.Location
import core.game.world.update.flag.context.Animation
import shared.consts.*
import shared.consts.Scenery as Objects

class DemonSlayerListener : InteractionListener {

    override fun defineListeners() {

        /*
         * Handles interaction with manhole.
         */

        on(Objects.VARROCK_MANHOLE_CLOSED_881, IntType.SCENERY, "open") { _, node ->
            val manhole = node.asScenery()
            replaceScenery(manhole, manhole.id + 1, -1)
            return@on true
        }

        on(Objects.VARROCK_MANHOLE_OPEN_882, IntType.SCENERY, "climb-down", "close") { player, node
            ->
            val manhole = node.asScenery()
            when (getUsedOption(player)) {
                "climb-down" -> teleport(player, SEWER_LOCATION)
                "close" -> replaceScenery(manhole, Objects.VARROCK_MANHOLE_CLOSED_881, -1)
            }
            return@on true
        }

        /*
         * Handles interaction with drain.
         */

        on(Objects.VARROCK_PALACE_DRAIN_KEY_17424, IntType.SCENERY, "search") { player, _ ->
            val questStage = getQuestStage(player, Quests.DEMON_SLAYER.questName)
            if (questStage == 20) {
                player.dialogueInterpreter.open(883, 883, "key")
            } else {
                sendMessage(player, "You search the castle drain and find nothing of value.")
            }
            return@on true
        }

        on(Objects.VARROCK_PALACE_DRAIN_17423, IntType.SCENERY, "search") { player, _ ->
            sendMessage(player, "Nothing interesting seems to have been dropped down here today.")
            return@on true
        }

        on(Objects.VARROCK_SEWER_RUSTY_KEY_17429, IntType.SCENERY, "take") { player, _ ->
            val questStage = getQuestStage(player, Quests.DEMON_SLAYER.questName)
            if (questStage == 20) {
                if (player.inventory.add(DemonSlayerUtils.FIRST_KEY)) {
                    setVarbit(player, Vars.VARBIT_QUEST_DEMON_SLAYER_DRAIN_2568, 0, true)
                    removeAttributes(player, "demon-slayer:poured", "demon-slayer:just-poured")
                    sendItemDialogue(
                        player,
                        DemonSlayerUtils.FIRST_KEY.id,
                        "You pick up an old rusty key."
                    )
                } else {
                    sendMessage(player, "Not enough inventory space.")
                }
            }
            return@on true
        }

        /*
         * Handles interaction with Delrith NPC.
         */

        on(NPCs.DELRITH_879, IntType.NPC, "attack") { player, node ->
            if (!inEquipment(player, DemonSlayerUtils.SILVERLIGHT.id)) {
                sendMessage(player, "I'd better wield Silverlight first.")
            } else {
                player.face(node as NPC)
                player.properties.combatPulse.attack(node)
            }
            return@on true
        }

        on(NPCs.WEAKENED_DELRITH_880, IntType.NPC, "banish") { player, _ ->
            player.dialogueInterpreter.open(8427322)
            return@on true
        }

        /*
         * Handles using bucket on drain.
         */

        onUseWith(
            IntType.SCENERY,
            Items.BUCKET_OF_WATER_1929,
            Objects.VARROCK_PALACE_DRAIN_KEY_17424
        ) { player, _, _ ->
            val quest = getQuestStage(player, Quests.DEMON_SLAYER.questName)
            val hasAnKey = hasAnItem(player, DemonSlayerUtils.FIRST_KEY.id).container != null

            if (player.inventory.remove(BUCKET_OF_WATER)) {
                player.inventory.add(BUCKET)
                playAudio(player, Sounds.WATER_BEING_POURED_2982)
                player.animate(ANIMATION)
                sendMessage(player, "You pour the liquid down the drain.")

                if (quest < 20) return@onUseWith true
                if (player.getAttribute("demon-slayer:just-poured", false)) return@onUseWith true

                if (!hasAnKey) {
                    player.getSavedData().questData.demonSlayer[0] = false
                }
                if (
                    !player.hasItem(DemonSlayerUtils.FIRST_KEY) &&
                    !player.getSavedData().questData.demonSlayer[0]
                ) {
                    playAudio(player, Sounds.KEY_DROPPING_DOWN_DRAIN_2983)
                    sendPlayerDialogue(
                        player,
                        "OK, I think I've washed the key down into the sewer. I'd better go down and get it!",
                        FaceAnim.HAPPY
                    )
                    player.getSavedData().questData.demonSlayer[0] = true
                    setVarbit(player, Vars.VARBIT_QUEST_DEMON_SLAYER_DRAIN_2568, 1, true)
                    setAttribute(player, "/save:demon-slayer:just-poured", true)
                }
            }
            return@onUseWith true
        }
    }

    companion object {
        private val SEWER_LOCATION = Location(3237, 9858, 0)
        private val ANIMATION = Animation(Animations.HUMAN_BURYING_BONES_827)
        private val BUCKET_OF_WATER = Item(Items.BUCKET_OF_WATER_1929)
        private val BUCKET = Item(Items.BUCKET_1925)
    }

}