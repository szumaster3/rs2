package content.region.misthalin.lumbridge.quest.sheep

import core.api.*
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.system.task.Pulse
import core.game.world.GameWorld
import core.game.world.map.Direction
import core.game.world.map.Location
import core.game.world.update.flag.context.Animation
import core.tools.RandomFunction
import shared.consts.*

class SheepShearerListener: InteractionListener {
    override fun defineListeners() {
        on(IntType.NPC, "shear") { player, node ->
            val sheep = node as NPC
            if (sheep.id == NPCs.SHEEP_3579) {
                if (getQuestStage(player, Quests.SHEEP_SHEARER) > 0) {
                    setAttribute(player, SheepShearer.ATTR_IS_PENGUIN_SHEEP_SHEARED, true)
                }
                animate(player, Animation(Animations.SHEAR_SHEEP_893))
                playAudio(player, Sounds.PENGUINSHEEP_ESCAPE_686)
                animate(sheep, Animation(3570))
                sheepBackAway(player, sheep, "The... whatever it is... manages to get away from you!")
                return@on true
            }
            if (!inInventory(player, Items.SHEARS_1735)) {
                sendMessage(player, "You need shears to shear a sheep.")
                return@on true
            }
            if (hasOption(sheep, "attack")) {
                sendMessage(player, "That one looks a little too violent to shear...")
                return@on true
            }
            if (freeSlots(player) == 0) {
                sendMessage(player, "You don't have enough space in your inventory to carry any wool you would shear.")
                return@on true
            }
            sheep.faceTemporary(player, 1)
            lock(sheep, 3)
            animate(player, Animation(Animations.SHEAR_SHEEP_893))
            val random = RandomFunction.random(1, 5)
            if (random != 4) {
                sheep.locks.lockMovement(2)
                sheep.transform(NPCs.SHEEP_5153)
                playAudio(player, Sounds.SHEAR_SHEEP_761)
                if (!addItem(player, Items.WOOL_1737)) {
                    return@on false
                }
                sendMessage(player, "You get some wool.")
                GameWorld.Pulser.submit(
                    object : Pulse(80, sheep) {
                        override fun pulse(): Boolean {
                            sheep.reTransform()
                            return true
                        }
                    },
                )
            } else {
                sheepBackAway(player, sheep, "The sheep manages to get away from you!")
            }
            return@on true
        }
    }

    private fun sheepBackAway(player: Player, sheep: NPC, messagePlayer: String) {
        val playerLocation = player.location
        val sheepLocation = sheep.location
        val sheepDirection = Direction.getDirection(sheepLocation, playerLocation)
        val sheepOppositeDirection = sheepDirection.opposite
        val xWalkLocation = sheepLocation.x + (sheepOppositeDirection.stepX * 3)
        val yWalkLocation = sheepLocation.y + (sheepOppositeDirection.stepY * 3)
        val sheepWalkToLocation = Location(xWalkLocation, yWalkLocation, sheepLocation.z)
        sendMessage(player, messagePlayer)
        unlock(sheep)
        forceWalk(sheep, sheepWalkToLocation, "DUMB")
    }
}