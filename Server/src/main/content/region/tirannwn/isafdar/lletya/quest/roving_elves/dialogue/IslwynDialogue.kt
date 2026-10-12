package content.region.tirannwn.isafdar.lletya.quest.roving_elves.dialogue

import content.region.tirannwn.isafdar.lletya.quest.roving_elves.RovingElves
import core.api.*
import core.game.dialogue.Dialogue
import core.game.dialogue.DialogueInterpreter
import core.game.dialogue.FaceAnim
import core.game.node.entity.player.Player
import core.game.node.item.GroundItemManager
import core.game.node.item.Item
import shared.consts.Items
import shared.consts.NPCs
import shared.consts.Quests
import core.tools.END_DIALOGUE

class IslwynDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        when {
            isQuestComplete(player, Quests.ROVING_ELVES) -> {
                player(FaceAnim.HALF_GUILTY, "Hello Islwyn, I'm back.")
                stage = 31
            }
            hasStarted(player, Quests.ROVING_ELVES) && getQuestStage(player, Quests.ROVING_ELVES) >= 10 -> {
                player(FaceAnim.HALF_GUILTY, "Hello Islwyn.")
                stage = 0
            }
            getQuestStage(player, Quests.ROVING_ELVES) == 0 && isQuestComplete(player, Quests.WATERFALL_QUEST) -> {
                player(FaceAnim.HALF_GUILTY, "Hello there.")
                stage = 0
            }
            else -> {
                player(FaceAnim.HALF_GUILTY, "Hello there.")
                stage = 1000
            }
        }
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> {
                val questStage = getQuestStage(player, Quests.ROVING_ELVES)
                when (questStage) {
                    10, 15 -> {
                        interpreter.sendDialogues(NPCs.ISLWYN_1680, FaceAnim.HALF_GUILTY, "Have you spoken to Eluned yet?")
                        stage = 2000
                    }

                    20 -> {
                        interpreter.sendDialogues(NPCs.ISLWYN_1680, FaceAnim.HALF_GUILTY, "You have returned! Thank you for all you have done.", "Now both me and my grandmother can rest in peace.")
                        stage = 19
                    }
                    else -> {
                        interpreter.sendDialogues(NPCs.ISLWYN_1680, FaceAnim.HALF_GUILTY, "Leave me be, I have no time for easterners. Between", "your lot and them gnomes, all you do is take and", "destroy. No thought for others.")
                        stage = 1
                    }
                }
            }

            1 -> {
                player(FaceAnim.HALF_GUILTY, "...but...")
                stage = 2
            }

            2 -> {
                npc(FaceAnim.HALF_GUILTY, "Save your excuses young one! It was one of your", "species that disturbed my grandmother's remains. Will", "she never get the peace she deserves?")
                stage = 3
            }

            3 -> {
                player(FaceAnim.HALF_GUILTY, "Grandmother?")
                stage = 4
            }

            4 -> {
                npc(FaceAnim.HALF_GUILTY, "Yes! Someone took her ashes from her tomb. If it", "wasn't for them gnomes she'd have been left in peace.", "But now I can sense her restlessness.")
                stage = 5
            }

            5 -> {
                player(FaceAnim.HALF_GUILTY, "Gnomes?")
                stage = 6
            }

            6 -> {
                npc(FaceAnim.HALF_GUILTY, "Yes gnomes! One of those little pests took the key to", "my grandmother's tomb. He must've given it to the", "human that desecrated the tomb.")
                stage = 7
            }

            7 -> {
                player(FaceAnim.HALF_GUILTY, "Was your grandmother's name Glarial?")
                stage = 8
            }

            8 -> {
                npc(FaceAnim.HALF_GUILTY, "Yes... How did you know that?")
                stage = 9
            }

            9 -> {
                sendOptions(player, "Do you want to;", "Tell the truth?", "Lie?", "Leave the old elf be?")
                stage = 10
            }

            10 -> when (buttonId) {
                1 -> player(FaceAnim.HALF_GUILTY, "It's a bit of a long tale, but to cut the story short, her", "remains reside in Baxtorian's home. I thought it's where", "she'd want to be. It was I that removed your", "grandmother's ashes.").also { stage++ }
                2 -> player(FaceAnim.HALF_GUILTY, "I just guessed.", "Well, now that that's over, I really need to be", "going.").also { stage = END_DIALOGUE }
                3 -> player(FaceAnim.HALF_GUILTY, "On second thought, I really should be going.").also { stage = END_DIALOGUE }
            }

            11 -> {
                npc(FaceAnim.HALF_GUILTY, "You've been in grandfather's home? That's where we", "originally wanted to leave Glarial's ashes to rest, but we", "could not understand how to enter.")
                stage = 12
            }

            12 -> {
                npc(FaceAnim.HALF_GUILTY, "This is gravely concerning... Her resting place must be", "consecrated.")
                stage = 13
            }

            13 -> {
                options("Maybe I could help.", "Sounds like you've got a lot to do.")
                stage = 14
            }

            14 -> when (buttonId) {
                1 -> player(FaceAnim.HALF_GUILTY, "Maybe I could help. What needs doing to consecrate", "her new tomb?").also { stage++ }
                2 -> player(FaceAnim.HALF_GUILTY, "Sounds like you've got a lot to do.").also { stage = END_DIALOGUE }
            }

            15 -> {
                npc(FaceAnim.HALF_GUILTY, "Are you offering to help?!? Maybe not all humans are", "as bad as I thought.")
                stage = 16
            }

            16 -> {
                npc(FaceAnim.HALF_GUILTY, "I don't know the consecration process. You should speak", "with Eluned... she is wise in the ways of the ritual.")
                stage = 17
            }

            17 -> {
                player(FaceAnim.HALF_GUILTY, "I'll see what I can do.")
                stage = 18
            }

            18 -> {
                end()
                setQuestStage(player, Quests.ROVING_ELVES, 10)
            }

            19 -> {
                player(FaceAnim.HALF_GUILTY, "How did you know that I have consecrated the tomb?")
                stage = 20
            }

            20 -> {
                npc(FaceAnim.HALF_GUILTY, "Her restlessness has finally left me. Here - I should", "give you something for your effort.")
                stage = 21
            }

            21 -> {
                sendDoubleItemDialogue(player, RovingElves.CRYSTAL_BOW_FULL, RovingElves.CRYSTAL_SHIELD_FULL, "Islwyn shows you a crystal bow and a crystal shield.")
                stage = 22
            }

            22 -> {
                npc(FaceAnim.HALF_GUILTY, "Crystal equipment is at its best when new and", "previously unused. The bow does not require", "ammunition and reduces in strength the more it's fired.", "The shield decreases in defensive capabilities the more")
                stage = 23
            }

            23 -> {
                npc(FaceAnim.HALF_GUILTY, "it's hit. Both the shield and the bow I am carrying only", "have 500 uses before they revert to seed.")
                stage = 24
            }

            24 -> {
                player(FaceAnim.HALF_GUILTY, "Revert to seed? What do you mean?")
                stage = 25
            }

            25 -> {
                npc(FaceAnim.HALF_GUILTY, "Ahhh, young one. It was thousands of years before we", "fully understood that ourselves. All will be explained if", "we feel you are ready. Now which one of these crystal", "creations would you like?")
                stage = 26
            }

            26 -> {
                options("Shields are for wimps! Give me the bow!", "I don't like running and hiding behind mushrooms. Shield please!")
                stage = 27
            }

            27 ->
                when (buttonId) {
                    1 -> {
                        player(FaceAnim.HALF_GUILTY, "Shields are for wimps! Give me the bow!")
                        stage = 30
                    }

                    2 -> {
                        player(FaceAnim.HALF_GUILTY, "I don't like running and hiding behind mushrooms.", "Shield please!")
                        stage = 301
                    }
                }

            30 -> {
                if (!isQuestComplete(player, Quests.ROVING_ELVES)) {
                    if (!addItem(player, Items.CRYSTAL_BOW_2_10_4222, 1)) {
                        GroundItemManager.create(Item(Items.CRYSTAL_BOW_2_10_4222, 1), player)
                    }
                    finishQuest(player, Quests.ROVING_ELVES)
                }
                end()
            }

            301 -> {
                if (!isQuestComplete(player, Quests.ROVING_ELVES)) {
                    if (!addItem(player, Items.CRYSTAL_SHIELD_2_10_4233, 1)) {
                        GroundItemManager.create(Item(Items.CRYSTAL_SHIELD_2_10_4233, 1), player)
                    }
                    finishQuest(player, Quests.ROVING_ELVES)
                }
                end()
            }

            31 -> {
                npc(FaceAnim.HALF_GUILTY, "Welcome back to the land of the elves, friend!", "Do you need your seeds charged into equipment?")
                stage = 32
            }

            32 -> {
                options("I need to buy a new piece of equipment.", "I need to recharge my seeds into equipment.")
                stage = 33
            }

            33 ->
                when (buttonId) {
                    1 -> {
                        npc(FaceAnim.HALF_GUILTY, "Ah, very well.", "I will sell you a new bow or shield for 900,000 coins.")
                        stage = 37
                    }

                    2 -> {
                        player(FaceAnim.HALF_GUILTY, "I need to recharge my current seeds into equipment.")
                        stage = 34
                    }
                }

            34 -> {
                options("Recharge seed into bow", "Recharge seed into shield")
                stage = 35
            }

            35 -> when (buttonId) {

                1 -> {
                    player(FaceAnim.HALF_GUILTY,
                        "Recharge my seed into a bow, please.")
                    stage = 36
                }

                2 -> {
                    player(FaceAnim.HALF_GUILTY,
                        "Recharge my seed into a shield, please.")
                    stage = 3601
                }
            }

            36 -> {
                end()
                rechargeCrystal(Items.CRYSTAL_BOW_FULL_4214)
            }

            3601 -> {
                end()
                rechargeCrystal(Items.CRYSTAL_SHIELD_FULL_4225)
            }

            37 -> options("Purchase bow", "Purchase shield").also { stage++ }
            38 -> when (buttonId) {
                1 -> {
                    player(FaceAnim.HALF_GUILTY, "I'd like to buy a new bow.")
                    stage = 39
                }

                2 -> {
                    player(FaceAnim.HALF_GUILTY, "I'd like to buy a new shield.")
                    stage = 40
                }
            }
            39 -> {
                val price = crystalWeaponPrice(0)
                if (!removeItem(player, Item(Items.COINS_995, price))) {
                    playerl(FaceAnim.HALF_GUILTY, "Sorry but I don't have that much.")
                    stage = 41
                } else {
                    sendDialogue(player, "You hand over $price coins and get a crystal bow in return.")
                    addItemOrDrop(player, Items.NEW_CRYSTAL_BOW_4212)
                    stage = 43
                }
            }

            40 -> {
                val price = crystalWeaponPrice(0)
                if (!removeItem(player, Item(Items.COINS_995, price))) {
                    playerl(FaceAnim.HALF_GUILTY, "Sorry but I don't have that much.")
                    stage = 41
                } else {
                    sendDialogue(player, "You hand over $price coins and get a crystal shield in return.")
                    addItemOrDrop(player, Items.NEW_CRYSTAL_SHIELD_4224)
                    stage = 43
                }
            }

            41 -> npcl(FaceAnim.HALF_GUILTY, "Well sorry, but I can't let it go for anything less.").also { stage++ }
            42 -> npcl(FaceAnim.HALF_GUILTY, "Oh well... never mind then.").also { stage = END_DIALOGUE }
            43 -> npcl(FaceAnim.HAPPY, "Good hunting.").also { stage++ }
            44 -> playerl(FaceAnim.FRIENDLY, "Thanks... goodbye.").also { stage = END_DIALOGUE }

            1000 -> npc(FaceAnim.HALF_GUILTY, "Hello there, it's a lovely day for a walk in the woods.", "So what can I help you with?").also { stage++ }
            1001 -> player(FaceAnim.HALF_GUILTY, "I'm just looking around.").also { stage = END_DIALOGUE }
            2000 -> if (getQuestStage(player, Quests.ROVING_ELVES) == 15) {
                player(FaceAnim.HALF_GUILTY, "Yes I have! She explained that I have to visit", "Glarial's old tomb and obtain a consecration seed", "from the temple guardian in there.").also { stage++ }
            } else {
                player(FaceAnim.HALF_GUILTY, "Not yet, I'll be back when I have.").also { stage = END_DIALOGUE }
            }
            2001 -> {
                interpreter.sendDialogues(NPCs.ISLWYN_1680, FaceAnim.HALF_GUILTY, "Good luck against the guardian, adventurer.", "Do it in the name of my grandmother Glarial.").also { stage = END_DIALOGUE }
            }
        }
        return true
    }

    override fun getIds(): IntArray = intArrayOf(DialogueInterpreter.getDialogueKey("islwyn_dialogue"), NPCs.ISLWYN_1680)

    /**
     * Calculates the recharge cost of a crystal weapon based on how many times
     * it has previously been recharged.
     *
     * The price starts at 900,000 coins and decreases by 180,000 coins
     * per recharge, with a minimum cost of 180,000 coins.
     *
     * @param timesRecharged number of previous recharges
     * @return the calculated recharge price (never below 180,000)
     */
    private fun crystalWeaponPrice(timesRecharged: Int): Int =
        (900000 - 180000 * timesRecharged).coerceAtLeast(180000)


    /**
     * Recharges a crystal seed into the specified fully charged crystal item.
     *
     * Requirements:
     * - Player must have a crystal seed in inventory.
     * - Player must have enough coins based on recharge history.
     *
     * On success:
     * - Removes the seed and the required coins.
     * - Adds the fully charged crystal item.
     * - Increments the recharge counter attribute.
     * - Ends the dialogue.
     *
     * @param rewardItem item ID of the fully charged crystal weapon to grant
     */
    private fun rechargeCrystal(rewardItem: Int) {
        if (!inInventory(player, RovingElves.CRYSTAL_SEED)) {
            sendDialogue(player, "You don't have any seeds to recharge.")
            return
        }

        val price = crystalWeaponPrice(
            player.getAttribute("rovingelves:crystal-equip-recharges", 0)
        )

        if (!inInventory(player, Items.COINS_995, price)) {
            sendDialogue(player, "You don't have enough coins, you need $price.")
            return
        }

        if (removeItem(player, RovingElves.CRYSTAL_SEED)
            && removeItem(player, Item(Items.COINS_995, price))
        ) {
            addItem(player, rewardItem)
            player.incrementAttribute(
                "/save:rovingelves:crystal-equip-recharges",
                1
            )
        }

        stage = END_DIALOGUE
    }
}
