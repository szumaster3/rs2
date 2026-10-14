package content.region.morytania.port_phasmatys.quest.ahoy.dialogue

import content.region.morytania.port_phasmatys.quest.ahoy.plugin.GhostsAhoyUtils
import core.api.*
import core.game.dialogue.DialogueFile
import core.game.interaction.QueueStrength
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.node.item.Item
import core.game.world.update.flag.context.Animation
import core.tools.END_DIALOGUE
import shared.consts.Animations
import shared.consts.Items
import shared.consts.NPCs
import shared.consts.Quests

/**
 * Represents the Old Crone (Ghosts Ahoy quest) dialogue.
 */
class OldCroneAhoyDialogue : DialogueFile() {

    private fun hasAmulet(p: Player) = inEquipment(p, Items.GHOSTSPEAK_AMULET_552) || inInventory(p, Items.GHOSTSPEAK_AMULET_552)

    private fun hasEnchantItems(p: Player) = inInventory(p, Items.MYSTICAL_ROBES_4247) &&
        inInventory(p, Items.BOOK_OF_HARICANTO_4248) &&
        inInventory(p, Items.TRANSLATION_MANUAL_4249)

    /**
     * Performs the enchantment in one atomic step so nothing can be lost half-way.
     */
    private fun enchant(p: Player, consumeItems: Boolean): Boolean {
        if (consumeItems && !hasEnchantItems(p)) return false
        val wearing = inEquipment(p, Items.GHOSTSPEAK_AMULET_552)
        if (!wearing && !inInventory(p, Items.GHOSTSPEAK_AMULET_552)) return false

        if (consumeItems) {
            removeItem(p, Items.MYSTICAL_ROBES_4247)
            removeItem(p, Items.BOOK_OF_HARICANTO_4248)
            removeItem(p, Items.TRANSLATION_MANUAL_4249)
        }
        if (wearing) {
            removeItem(p, Item(Items.GHOSTSPEAK_AMULET_552, 1), Container.EQUIPMENT)
        } else {
            removeItem(p, Items.GHOSTSPEAK_AMULET_552)
        }
        addItemOrDrop(p, Items.GHOSTSPEAK_AMULET_4250, 1)
        GhostsAhoyUtils.advanceQuest(p, 80)

        findNPC(NPCs.OLD_CRONE_1695)?.let { animate(it, Animation(Animations.HUMAN_OLD_DANCE_818)) }
        queueScript(p, 3, QueueStrength.SOFT) {
            sendItemDialogue(p, Items.GHOSTSPEAK_AMULET_4250, "The ghostspeak amulet emits a green glow from its gem.")
            sendMessage(p, "The amulet of ghostspeak glows green from the crone's enchantment.")
            return@queueScript stopExecuting(p)
        }
        return true
    }

    override fun handle(componentID: Int, buttonID: Int) {
        val p = player!!
        val questStage = getQuestStage(p, Quests.GHOSTS_AHOY)
        val enlightened = getAttribute(p, GhostsAhoyUtils.croneEnlightened, false)
        npc = NPC(NPCs.OLD_CRONE_1695)

        if (questStage < 3 || questStage > 99) {
            end()
            return
        }

        when (stage) {
            0 -> when {
                // ---- the enchanted amulet has been made ----
                questStage >= 80 -> when {
                    hasAnItem(p, Items.GHOSTSPEAK_AMULET_4250).container != null ->
                        npc("Use the amulet on Necrovarus while wearing it.", "It will work only once, so choose your command well.").also { stage = END_DIALOGUE }

                    hasAmulet(p) ->
                        player("I've lost the enchanted amulet, but I have another", "amulet of ghostspeak.").also { stage = 56 }

                    else ->
                        npc("You have lost the enchanted amulet? Bring me a plain", "amulet of ghostspeak and I will perform the ritual again.").also { stage = END_DIALOGUE }
                }

                // ---- tea quest ----
                !enlightened && inInventory(p, Items.CUP_OF_TEA_4246) ->
                    player("Here's a lovely cup of milky tea for you, in your", "own special cup.").also { stage = 1 }

                !enlightened && inInventory(p, Items.CUP_OF_TEA_4245) ->
                    player("Here's a lovely cup of tea for you, in your own special cup.").also { stage = 90 }

                !enlightened && inInventory(p, Items.NETTLE_TEA_4239) ->
                    player("Here's some tea for you, like you asked.").also { stage = 60 }

                !enlightened && questStage == 3 ->
                    player("Were you once a disciple of Necrovarus in the Temple", "of Phasmatys, old woman?").also { stage = 70 }

                !enlightened ->
                    player("Remind me - what did you want me to bring you?").also { stage = 80 }

                // ---- enlightened ----
                questStage == 5 ->
                    player("Good news! I have found your son!").also { stage = 40 }

                hasEnchantItems(p) && hasAmulet(p) ->
                    player("I have something for you.").also { stage = 50 }

                hasEnchantItems(p) ->
                    npc("You have everything I need, but I will also need your", "amulet of ghostspeak to enchant it.").also { stage = END_DIALOGUE }

                else ->
                    npc("Hello again. What can I do for you?").also { stage = 18 }
            }

            // ---- enlightenment ----
            1 -> sendDialogue(p, "As the old woman drinks the cup of milky tea, enlightenment glows from within her eyes.").also {
                removeItem(p, Items.CUP_OF_TEA_4246)
                if (hasAnItem(p, Items.PORCELAIN_CUP_4244).container == null) {
                    addItemOrDrop(p, Items.PORCELAIN_CUP_4244)
                }
                setAttribute(p, GhostsAhoyUtils.croneEnlightened, true)
                GhostsAhoyUtils.advanceQuest(p, 4)
                stage++
            }

            2 -> npc("Ah, that's better. Now, let me see... Yes, I was once a", "disciple of Necrovarus.").also { stage++ }
            3 -> player("I have come from Velorina. She needs your help.").also { stage++ }
            4 -> npc("Velorina? That name sounds familiar...").also { stage++ }
            5 -> npc("Yes, Velorina was once a very good friend! It has been", "many years since we spoke last. How is she?").also { stage++ }
            6 -> player("She's a ghost, I'm afraid.").also { stage++ }
            7 -> npc("They are all dead, then. Even Velorina. I should have", "done something to stop what was happening, instead of", "running away.").also { stage++ }
            8 -> player("She and many others want to pass on into the next", "world, but Necrovarus will not let them. Do you know", "of any way to help them?").also { stage++ }
            9 -> npc("There might be one way...").also { stage++ }
            10 -> npc("Do you have such a thing as a ghostspeak amulet?").also { stage++ }
            11 -> if (!hasAmulet(p)) {
                player("I don't have that on me.").also { stage++ }
            } else {
                player("Yes, I have one with me.").also { stage++ }
            }

            12 -> if (!hasAmulet(p)) {
                npc("Well, that's bad luck.").also { stage++ }
            } else {
                npc("Well, that's a stroke of luck.").also { stage++ }
            }

            13 -> npc("There is an enchantment that I can perform on such", "an amulet that will give it the power of command over", "ghosts. It will work only once, but it will enable you to", "command Necrovarus to let the ghosts pass on.").also { stage++ }
            14 -> player("What do you need to perform the enchantment?").also { stage++ }
            15 -> npc("Necrovarus had a magical robe for which he must have", "no more use. Only these robes hold the power needed", "to perform the enchantment.").also { stage++ }
            16 -> npc("All his rituals come from a book written by an ancient", "sorcerer from the East called Haricanto. Bring me this", "strange book.").also { stage++ }
            17 -> npc("I cannot read the strange letters of the eastern lands.", "I will need something to help me translate the book.").also { stage++ }

            // ---- menu ----
            18 -> if (questStage < 5 && hasAnItem(p, Items.MODEL_SHIP_4253).container == null && hasAnItem(p, Items.MODEL_SHIP_4254).container == null) {
                options("You are doing so much for me - is there anything I can do for you?", "Remind me - what can I do about Necrovarus?", "What did you want me to get for you?", "I'll go and search for those items you asked for.").also { stage = 19 }
            } else {
                options("Remind me - what can I do about Necrovarus?", "What did you want me to get for you?", "I'll go and search for those items you asked for.").also { stage = 31 }
            }

            19 -> when (buttonID) {
                1 -> player("You are doing so much for me - is there anything", "I can do for you?").also { stage = 20 }
                2 -> player("Remind me - what can I do about Necrovarus?").also { stage = 32 }
                3 -> player("What did you want me to get for you?").also { stage = 33 }
                4 -> player("I'll go and search for those items you asked for.").also { stage = END_DIALOGUE }
            }

            20 -> npc("I have lived here on my own for many years, but not", "always. When I left Port Phasmatys, I took my", "son with me. He grew up to be a fine boy, always in love", "with the sea.").also { stage++ }
            21 -> npc("He was about twelve years old when he ran away with", "some pirates to be a cabin boy. I never saw him again.").also { stage++ }
            22 -> player("That's the second saddest story I have heard today.").also { stage++ }
            23 -> npc("If you ever see him, please give him this...and tell him", "that his mother still loves him.").also { stage++ }
            24 -> {
                if (hasAnItem(p, Items.MODEL_SHIP_4253).container == null && hasAnItem(p, Items.MODEL_SHIP_4254).container == null) {
                    addItemOrDrop(p, Items.MODEL_SHIP_4253)
                    sendItemDialogue(p, Items.MODEL_SHIP_4253, "The old woman gives you a toy boat.")
                    stage++
                } else {
                    stage = 25
                    handle(componentID, buttonID)
                }
            }
            25 -> player("Was this his boat?").also { stage++ }
            26 -> npc("Yes, he made it himself. It is a model of the very ship", "in which he sailed away. The paint has peeled off and it", "has lost its flag, but I could never throw it away.").also { stage++ }
            27 -> player("If I find him, I will pass it on.").also { stage = END_DIALOGUE }

            31 -> when (buttonID) {
                1 -> player("Remind me - what can I do about Necrovarus?").also { stage = 32 }
                2 -> player("What did you want me to get for you?").also { stage = 33 }
                3 -> player("I'll go and search for those items you asked for.").also { stage = END_DIALOGUE }
            }

            32 -> npc("There is an enchantment that I can perform on such", "an amulet that will give it the power of command over", "ghosts. It will work only once, but it will enable you to", "command Necrovarus to let the ghosts pass on.").also { stage = END_DIALOGUE }
            33 -> npc("Necrovarus had a magical robe for which he must have", "no more use. Only these robes hold the power needed", "to perform the enchantment.").also { stage++ }
            34 -> npc("All his rituals come from a book written by an ancient", "sorcerer from the East called Haricanto. Bring me this", "strange book.").also { stage++ }
            35 -> npc("I cannot read the strange letters of the eastern lands.", "I will need something to help me translate the book.").also { stage = END_DIALOGUE }

            // ---- son found ----
            40 -> npc("Goodness! Where is he?").also {
                GhostsAhoyUtils.advanceQuest(p, 6)
                stage++
            }
            41 -> player("He lives on a shipwreck to the east of here.", "He remembers you and wishes you well.").also { stage++ }
            42 -> npc("Oh thank you! I will travel to see him as soon as", "I am able!!").also { stage = END_DIALOGUE }

            // ---- enchantment ----
            50 -> npc("Good - the robes of Necrovarus.").also { stage++ }
            51 -> npc("The Book of Haricanto! I have no idea how you came", "by this, but well done!!").also { stage++ }
            52 -> npc("A translation manual - yes, this should do the job.").also { stage++ }
            53 -> npc("Wonderful; that's everything I need.").also { stage++ }
            54 -> npc("I will now perform the ritual of enchantment.").also { stage++ }
            55 -> {
                end()
                enchant(p, true)
            }
            56 -> npc("Hand it over. The magic has not left this place, I", "can perform the ritual again.").also { stage++ }
            57 -> {
                end()
                enchant(p, false)
            }

            // ---- nettle tea without the special cup ----
            60 -> npc("Yes, but it's not in my special cup! I only ever drink", "from my special cup!").also { stage++ }
            61 -> player("I see. Can I have this special cup, then?").also { stage++ }
            62 -> {
                if (hasAnItem(p, Items.PORCELAIN_CUP_4244).container == null) {
                    addItemOrDrop(p, Items.PORCELAIN_CUP_4244)
                    sendItemDialogue(p, Items.PORCELAIN_CUP_4244, "The old crone gives you her special cup.")
                } else {
                    sendDialogue(p, "You already have her special cup.")
                }
                stage = END_DIALOGUE
            }

            // ---- first meeting ----
            70 -> npc("I don't remember; I am so old and my memory goes", "back only so far...").also { stage++ }
            71 -> player("Is there anything that can help to refresh your", "memory?").also { stage++ }
            72 -> npc("Yes, I would love a nice hot cup of nettle tea.").also { stage++ }
            73 -> player("Do you know where I can find nettles around here?").also { stage++ }
            74 -> npc("I believe they grow wild in the Haunted Forest.").also { stage = END_DIALOGUE }

            // ---- reminder ----
            80 -> npc("A nice hot cup of nettle tea, in my special cup, with", "milk. I only ever drink from my special cup!").also { stage = END_DIALOGUE }

            // ---- tea without milk ----
            90 -> npc("Oh no, it hasn't got milk in it. I only drink", "tea with milk, I'm afraid.").also { stage++ }
            91 -> {
                end()
                GhostsAhoyUtils.advanceQuest(p, 4)
            }
        }
    }
}
