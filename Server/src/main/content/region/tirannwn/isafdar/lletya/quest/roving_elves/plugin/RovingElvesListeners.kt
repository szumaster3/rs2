package content.region.tirannwn.isafdar.lletya.quest.roving_elves.plugin

import content.data.Quests

import content.region.tirannwn.isafdar.lletya.quest.roving_elves.RovingElves
import core.api.*
import core.game.interaction.InteractionListener
import core.game.node.Node
import core.game.system.task.Pulse
import core.game.world.GameWorld.Pulser
import core.game.world.map.Direction
import core.game.world.map.Location
import core.game.world.update.flag.context.Animation
import shared.consts.*

class RovingElvesListeners : InteractionListener {

    private fun nodeCenter(node: Node): Location =
        if (node.asScenery().rotation % 2 == 0)
            node.location.transform(1, 0, 0)
        else
            node.location.transform(0, 1, 0)

    companion object {
        val ANIMATION_DIG: Animation  = Animation.create(Animations.DIG_SPADE_830)
        val ANIMATION_DROP: Animation = Animation.create(Animations.HUMAN_BURYING_BONES_827)

        private val LEAF_SUCCESS_MSG  = "You safely jump across."
        private val LEAF_LADDER_MSG   = "You climb out of the pit."
        private val STICK_SUCCESS_MSG = "You manage to skillfully pass the trap."
        private val WIRE_SUCCESS_MSG  = "You successfully step over the tripwire."

        private val OVER       = Animation(Animations.CLIMB_OBJECT_839)
        private val THROUGH    = Animation(Animations.DEFEND_WALK_1237)
        private val STICK_TRAP = Animation(Animations.HUMAN_WALK_SHORT_819)
        private val LEAF_TRAP  = Animation(Animations.BA_PRESSURE_1115)
        private val WIRE_TRAP  = Animation(Animations.CROSS_TRIPWIRE_1236)

        private val LEAF_TRAP_CLIMB = Location(2274, 3172, 0)
        private val illegalJump = listOf(3174)
    }

    override fun defineListeners() {

        on(Scenery.TREE_8742, SCENERY, "pass") { player, node ->
            if (!hasRequirement(player, Quests.MOURNINGS_END_PART_I.questName)) {
                return@on false
            }

            val dir = Direction.getDirection(player.location, node.location)
            val dest = player.location.transform(dir, 2)

            forceMove(player, player.location, dest, 0, 60, null, -1)
            return@on true
        }

        /*
         * Handles enter to the dense forest.
         */

        on(intArrayOf(Scenery.DENSE_FOREST_3999, Scenery.DENSE_FOREST_3998, Scenery.DENSE_FOREST_3939, Scenery.DENSE_FOREST_3938, Scenery.DENSE_FOREST_3937), SCENERY, "enter") { player, node ->

            val dir = Direction.getDirection(player.location, node.location)
            val distance = when (node.id) {
                Scenery.DENSE_FOREST_3999, Scenery.DENSE_FOREST_3937 -> 3
                else -> 2
            }

            val dest = nodeCenter(node).transform(dir, distance)
            val anim = if (node.id == Scenery.DENSE_FOREST_3937) OVER else THROUGH

            forceMove(player, player.location, dest, 0, 60, null, anim.id)
            return@on true
        }

        /*
         * Handles step over through the tripwire scenery.
         */

        on(Scenery.TRIPWIRE_3921, SCENERY, "step-over") { player, node ->
            val dir = Direction.getDirection(player.location, node.location)
            val dest = node.location.transform(dir, 2)

            forceMove(player, player.location, dest, 0, 60, null, WIRE_TRAP.id)
            {
                sendMessage(player, WIRE_SUCCESS_MSG)
            }
            return@on true
        }

        /*
         * Handles pass through the stick scenery.
         */

        on(Scenery.STICKS_3922, SCENERY, "pass") { player, node ->
            val dir = Direction.getDirection(player.location, node.location)
            val dest = node.location.transform(dir, 2)

            forceMove(player, player.location, dest, 0, 60, null, STICK_TRAP.id)
            {
                sendMessage(player, STICK_SUCCESS_MSG)
            }
            return@on true
        }

        /*
         * Handles jump through leaves scenery.
         */

        on(intArrayOf(Scenery.LEAVES_3924, Scenery.LEAVES_3925), SCENERY, "jump") { player, node ->
            if (illegalJump.contains(player.location.y)) {
                return@on true
            }

            val dir = Direction.getDirection(player.location, node.location)
            val dest = node.location.transform(dir, 3)

            forceMove(player, player.location, dest, 0, 60, null, LEAF_TRAP.id)
            {
                sendMessage(player, LEAF_SUCCESS_MSG)
            }
            return@on true
        }

        /*
         * Handles climb through rocks scenery.
         */

        on(Scenery.PROTRUDING_ROCKS_3927, SCENERY, "climb") { player, _ ->
            forceMove(player, player.location, LEAF_TRAP_CLIMB, 0, 60, null, -1)
            {
                sendMessage(player, LEAF_LADDER_MSG)
            }
            return@on true
        }

        /*
         * Handles search the fire remains.
         */

        on(Scenery.FIRE_REMAINS_5252, SCENERY, "search") { player, _ ->
            sendDialogueLines(player, "The firepit is still warm, there must be travellers about.", "Maybe I should look for them.")
            return@on true
        }

        /*
         * Handles plant the consecration seed.
         */

        on(RovingElves.CONSECRATION_SEED_CHARGED, ITEM, "plant") { player, _ ->
            if (Location(2603, 9911).getDistance(player.location) > 3 || getQuestStage(player, Quests.ROVING_ELVES.questName) != 15) {
                return@on true
            }

            if (!inInventory(player, Items.SPADE_952, 1)) {
                sendMessage(player, "You need a spade to plant the seed.")
                return@on true
            }

            animate(player, ANIMATION_DIG)
            sendMessage(player, "You dig a small hole with your spade.")

            Pulser.submit(object : Pulse(1, player) {
                var counter = 0

                override fun pulse(): Boolean {
                    when (counter++)
                    {
                        3 -> {
                            sendMessage(player, "You drop the crystal seed in the hole.")
                            faceLocation(player, Location(2604, 9907, 0))
                            animate(player, ANIMATION_DROP)
                        }

                        6 -> {
                            removeItem(player, RovingElves.CONSECRATION_SEED_CHARGED)
                            setQuestStage(player, Quests.ROVING_ELVES.questName, 20)
                            sendGraphics(Graphics.GRAPHIC_719, Location(2604, 9907, 0))
                            sendMessage(player, "The seed vanishes in a puff of smoke.")
                            return true
                        }
                    }
                    return false
                }
            })

            return@on true
        }
    }

    override fun defineDestinationOverrides() {
        setDest(SCENERY, Scenery.DENSE_FOREST_3999) { _, _ -> Location(2188, 3162) }
        setDest(SCENERY, Scenery.DENSE_FOREST_3998) { _, _ -> Location(2188, 3171) }
    }
}