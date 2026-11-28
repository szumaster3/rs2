package content.region.fremennik.rellekka.quest.viking.plugin

import core.api.*
import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.interaction.QueueStrength
import core.game.node.entity.impl.Projectile.getLocation
import core.game.world.map.Location
import core.game.world.update.flag.context.Animation
import core.tools.END_DIALOGUE
import core.tools.RandomFunction
import shared.consts.Animations
import shared.consts.Graphics
import shared.consts.Items
import shared.consts.Sounds

class PetRockPlugin : InteractionListener {
    override fun defineListeners() {
        on(Items.PET_ROCK_3695, IntType.ITEM, "interact") { player, _ ->
            if (player.inCombat()) {
                sendMessage(player, "You can't interact with your pet rock while being in combat.")
                return@on true
            }
            openDialogue(player, PetRockDialogue())
            return@on true
        }
    }
}


private class PetRockDialogue : DialogueFile() {

    private var randomDialogue: Int = -1
    private var subStage: Int = 0

    override fun handle(componentID: Int, buttonID: Int) {
        when (stage) {
            0 -> options("Talk", "Stroke", "Feed", "Fetch", "Stay").also { stage = 1 }
            1 -> when (buttonID) {
                1 -> {
                    randomDialogue = RandomFunction.random(0, 5)
                    subStage = 0
                    stage = 1000
                    handleDialogue()
                }
                2 -> handleStroke()
                3 -> handleFeed()
                4 -> handleFetch()
                5 -> handleStay()
            }

            1000 -> handleDialogue()
            2 -> handleThrow()
        }
    }

    private fun handleDialogue() {
        when (randomDialogue) {
            0 -> when (subStage++) {
                0 -> playerl(FaceAnim.FRIENDLY, "Good day, rock!")
                1 -> sendItemDialogue(player!!, Items.PET_ROCK_3695, "...")
                2 -> playerl(FaceAnim.FRIENDLY, "Oooh, I love jokes! Go on then!")
                3 -> sendItemDialogue(player!!, Items.PET_ROCK_3695, "...")
                4 -> playerl(FaceAnim.FRIENDLY, "Who's there?")
                5 -> sendItemDialogue(player!!, Items.PET_ROCK_3695, "...")
                6 -> playerl(FaceAnim.FRIENDLY, "Interrupting cow wh")
                7 -> sendItemDialogue(player!!, Items.PET_ROCK_3695, "...")
                8 -> playerl(FaceAnim.FRIENDLY, "Haha, good one!")
                9 -> end()
            }
            1 -> when (subStage++) {
                0 -> playerl(FaceAnim.FRIENDLY, "Hey there, rock! How are you settling into your new home?")
                1 -> sendItemDialogue(player!!, Items.PET_ROCK_3695, "...")
                2 -> playerl(FaceAnim.FRIENDLY, "I'm glad to hear it!")
                3 -> playerl(FaceAnim.FRIENDLY, "Erm, this is kind of awkward, but... one of the neighbours found a pile of pebbles on their lawn.")
                4 -> playerl(FaceAnim.FRIENDLY, "Now, I'm not saying it WAS you...")
                5 -> sendItemDialogue(player!!, Items.PET_ROCK_3695, "...")
                6 -> playerl(FaceAnim.FRIENDLY, "Alright, alright, I believe you! There's no need for that kind of language!")
                7 -> end()
            }
            2 -> when (subStage++) {
                0 -> playerl(FaceAnim.FRIENDLY, "Hello there, rock. How are things?")
                1 -> sendItemDialogue(player!!, Items.PET_ROCK_3695, "...")
                2 -> playerl(FaceAnim.FRIENDLY, "Hmmm, I don't know. Have you tried swamp tar? I hear that's good at clearing rashes.")
                3 -> sendItemDialogue(player!!, Items.PET_ROCK_3695, "...")
                4 -> stage = END_DIALOGUE
            }
            3 -> when (subStage++) {
                0 -> playerl(FaceAnim.FRIENDLY, "Hello there, rock. How are things?")
                1 -> sendItemDialogue(player!!, Items.PET_ROCK_3695, "...")
                2 -> playerl(FaceAnim.FRIENDLY, "Oh, what a lovely song! That was a nice surprise, rock!")
                3 -> end()
            }
            4 -> when (subStage++) {
                0 -> playerl(FaceAnim.FRIENDLY, "Hey there, rock! How are you settling into your new home?")
                1 -> sendItemDialogue(player!!, Items.PET_ROCK_3695, "...")
                2 -> playerl(FaceAnim.FRIENDLY, "Oh, I'm sorry to hear that.")
                3 -> sendItemDialogue(player!!, Items.PET_ROCK_3695, "...")
                4 -> playerl(FaceAnim.FRIENDLY, "I'll complain to the housing association and have them moved out of the neighbourhood!")
                5 -> end()
            }
            5 -> when (subStage++) {
                0 -> playerl(FaceAnim.FRIENDLY, "Good day, rock!")
                1 -> sendItemDialogue(player!!, Items.PET_ROCK_3695, "...")
                2 -> playerl(FaceAnim.FRIENDLY, "Oooh, I love jokes! Go on then!")
                3 -> sendItemDialogue(player!!, Items.PET_ROCK_3695, "...")
                4 -> playerl(FaceAnim.FRIENDLY, "I don't know, what is the difference between a cow and a goblin?")
                5 -> sendItemDialogue(player!!, Items.PET_ROCK_3695, "...")
                6 -> playerl(FaceAnim.FRIENDLY, "Rock! How could you! That's awful!")
                7 -> sendItemDialogue(player!!, Items.PET_ROCK_3695, "...")
                8 -> playerl(FaceAnim.FRIENDLY, "I don't care if the other rocks think it's funny, you're not to talk like that again!")
                9 -> end()
            }
        }
    }

    private fun handleStroke() {
        end()
        val animDuration = animationDuration(Animation(Animations.VIKING_PET_ROCK_STROKE_1333))
        lock(player!!, animDuration)
        sendMessage(player!!, "You stroke your pet rock.")
        animate(player!!, Animations.VIKING_PET_ROCK_STROKE_1333, false)
        queueScript(player!!, animDuration, QueueStrength.SOFT) {
            sendMessage(player!!, "Your rock seems much happier.")
            return@queueScript stopExecuting(player!!)
        }
    }

    private fun handleFeed() {
        sendMessage(player!!, "You try and feed the rock.")
        sendMessage(player!!, "Your rock doesn't seem hungry.")
        end()
    }

    private fun handleFetch() {
        playerl(FaceAnim.FRIENDLY, "Want to fetch the stick, rock? Of course you do...")
        stage = 2
    }

    private fun handleStay() {
        playerl(FaceAnim.FRIENDLY, "Be a good rock...")
        sendMessage(player!!, "You wait a few seconds and pick your rock back up and pet it.", 6)
        visualize(player!!, Animations.PETROCK_HUMAN_STAY_6664, Graphics.PETROCK_SPOTANIM_STAY_1156)
        end()
    }

    private fun handleThrow() {
        end()
        val animDuration = animationDuration(Animation(Animations.PETROCK_HUMAN_STICK_6665))
        lock(player!!, duration = animDuration)
        lockInteractions(player!!, duration = animDuration)
        playAudio(player!!, Sounds.THROW_STICK_1942)
        visualize(player!!, Animations.PETROCK_HUMAN_STICK_6665, Graphics.PETROCK_SPOTANIM_STICK_1157)
        spawnProjectile(getLocation(player), Location.getRandomLocation(getLocation(player), 5, true), Graphics.PETROCK_PROJANIM_STICK_1158, 40, 0, 150, 250, 25)
    }
}
