package content.region.misthalin.draynor.wizardstower.rc_guild.dialogue

import content.data.GameAttributes
import core.api.*
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.dialogue.IfTopic
import core.game.dialogue.Topic
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.node.entity.skill.Skills
import core.game.node.item.Item
import core.game.world.GameWorld
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.Components
import shared.consts.Items
import shared.consts.NPCs

/* TODO:
 * [ ] CHECK IF tiara is also accepted or if I made it up to myself at the time.:
 *     - Show every regular talisman or tiara available, excluding the elemental talisman for omni-talisman.
 * [x] - Restored npc descriptions.
 * [ ] - Check if task is possible to complete.
 * [ ] - Added onUseWith interaction [Talisman on NPC].
 * [ ] - Replace the name based Runecrafter robes check with item ids.
 */

/**
 * An established member of the Runecrafting Guild, a section of the
 * revolutionary Wizards' Tower in southern Misthalin. Elriss, apart
 * from managing the guild, is also its shop owner, selling goods in
 * exchange for Runecrafting guild tokens.
 */
@Initializable
class WizardElrissDialogue(player: Player? = null) : Dialogue(player) {

    /**
     * Experience reward for each shown talisman.
     */
    val xpPerTalisman = mapOf(
        Items.AIR_TALISMAN_1438 to 9.0,
        Items.MIND_TALISMAN_1448 to 9.0,
        Items.WATER_TALISMAN_1444 to 9.0,
        Items.EARTH_TALISMAN_1440 to 12.0,
        Items.FIRE_TALISMAN_1442 to 27.0,
        Items.BODY_TALISMAN_1446 to 50.0,
        Items.COSMIC_TALISMAN_1454 to 109.0,
        Items.CHAOS_TALISMAN_1452 to 171.0,
        Items.NATURE_TALISMAN_1462 to 531.0,
        Items.LAW_TALISMAN_1458 to 1428.0,
        Items.DEATH_TALISMAN_1456 to 4242.0,
        Items.BLOOD_TALISMAN_1450 to 6958.0,
    )

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        npcl(FaceAnim.HAPPY, "Welcome to the Runecrafting Guild.")
        stage = 0
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            // Main menu, page 1.
            0 -> showTopics(
                IfTopic("I have some tokens I'd like to cash in.", 1, inInventory(player, Items.RUNECRAFTING_GUILD_TOKEN_13650)),
                IfTopic("I have a talisman to show you.", 2, xpPerTalisman.keys.any { inInventory(player, it) }),
                IfTopic("I've lost my omni-talisman.", 4, !hasOmniTalisman() && completedTask()),
                Topic("What is this place?", 5),
                Topic("I have another question.", 100, true),
            )

            1 -> {
                end()
                openInterface(player, Components.RCGUILD_REWARDS_779)
            }

            // Show talismans.
            2 -> {
                if (completedTask()) {
                    npc(
                        "You've already shown me that you have all the",
                        "talismans, which is why I've already given you an",
                        "omni-talisman!",
                    )
                    stage = 60
                    return true
                }
                val newlyShown = xpPerTalisman.keys.filter { inInventory(player, it) && !talismanShown(it) }
                if (newlyShown.isEmpty()) {
                    npc("You don't have any new talismans for me. Come back when you find them.")
                    stage = END_DIALOGUE
                    return true
                }
                newlyShown.forEach {
                    setAttribute(player, GameAttributes.RC_GUILD_TALISMAN + "_$it", true)
                    rewardXP(player, Skills.RUNECRAFTING, xpPerTalisman.getValue(it))
                }
                sendItemDialogue(
                    player,
                    newlyShown.last(),
                    if (newlyShown.size == 1) "You show Elriss the ${getItemName(newlyShown.first())}." else "You show Elriss your talismans.",
                )
                stage = if (xpPerTalisman.keys.all { talismanShown(it) }) 65 else END_DIALOGUE
            }

            3 -> {
                sendItemDialogue(player!!, Items.OMNI_TALISMAN_13649, "Wizard Elriss gives you an omni-talisman.")
                addItemOrDrop(player!!, Items.OMNI_TALISMAN_13649)
                setAttribute(player!!, GameAttributes.RC_GUILD_TALISMAN_TASK_COMPLETE, true)
                end()
            }

            // Lost omni-talisman.
            4 -> npcl(FaceAnim.THINKING, "You lost the talisman? The talisman that some might say was the culmination of my life's work. The only one of its kind. My magnum opus. That talisman?").also { stage = 66 }

            // What is this place?
            5 -> npc(FaceAnim.FRIENDLY, "This is the Runecrafting Guild, as I said. After the", "secret of Runecrafting was re-discovered, I set up the", "guild as a place for the most advanced runecrafters to", "work together.").also { stage++ }

            6 -> showTopics(
                Topic("Work together towards what?", 8),
                Topic("Acantha and Vief are hardly working together!", 61),
                Topic("Where are we exactly?", 38),
                Topic("What can I do here?", 19),
                Topic("Never mind.", END_DIALOGUE),
            )

            8 -> npcl(FaceAnim.FRIENDLY, "Towards a greater understanding of Runecrafting, of course. The basics of Runecrafting were preserved when the first Tower was destroyed, but many of the more advanced secrets remain unknown.").also { stage = 9 }

            9 -> showTopics(
                Topic("What secrets?", 11),
                Topic("Acantha and Vief are hardly working together!", 61),
                Topic("Where are we exactly?", 38),
                Topic("What can I do here?", 19),
                Topic("Never mind.", END_DIALOGUE),
            )

            11 -> npc(FaceAnim.FRIENDLY, "Oh, nothing to interest an adventurer such as yourself", "I'm sure.").also { stage++ }
            12 -> showTopics(
                Topic("I am interested.", 14),
                Topic("Yeah, I'm not really interested.", 72),
            )

            14 -> npc(FaceAnim.FRIENDLY, "We all have our projects, adventurer. Acantha and Vief", "are happy to involve junior runecrafters in their feud,", "but others prefer to keep their research private until it", "is revealed.").also { stage++ }
            15 -> npc(FaceAnim.SAD, "An idea may be subject to cruel ridicule if it is aired", "prematurely, as I have learned to my cost. You must", "forgive me if I am not so forthcoming again.").also { stage++ }
            16 -> showTopics(
                Topic("Never mind, then.", 0),
                Topic("Go on, tell me.", 18),
                IfTopic("[Charm] You can tell me.", 80, wearsCharosRing()),
            )

            18 -> npc(FaceAnim.FRIENDLY, "Leave me be!").also { stage = END_DIALOGUE }

            // What can I do here?
            19 -> npc("Wizard Acantha and Wizard Vief are running The", "Great Orb Project. It requires large numbers of", "runecrafters, so you should speak with them if you", "want something to do.").also { stage = 73 }
            73 -> npcl(FaceAnim.FRIENDLY, "Wizard Korvak has visited the Abyss and can repair abyssal pouches, I, myself, am working on a new kind of talisman: the omni-talisman.").also { stage = 20 }
            20 -> showTopics(
                Topic("Tell me about Acantha and Vief's project.", 22),
                Topic("Tell me about the omni-talisman.", 54),
                Topic("Tell me about Wizard Korvak's pouch repairs.", 58),
                Topic("Never mind.", END_DIALOGUE),
            )

            22 -> npc("The Orb Proj...I beg your pardon, The Great Orb", "Project? It's truly fascinating. Wizards Acantha and", "Vief have found that energy leaks out of some of the", "Runecrafting altars. They are recruiting teams of").also { stage++ }
            23 -> npc("experienced runecrafters such as yourself, to force the", "energy back in.").also { stage++ }
            24 -> npc("Join one of the teams by speaking to Wizard Acantha", "or Wizard Vief. When the wizards have enough helpers,", "I will open a portal to the Air Altar.").also { stage++ }
            25 -> npc("The energy appears in the form of floating orbs. These", "can be moved by means of wands that attract or repel", "them. Acantha or Vief will give you one of each wand.").also { stage++ }
            26 -> npc("Your goal is to move the correct colour orb to the altar", "stone, while keeping the other orbs away. Wizard", "Acantha favours the green orbs, while Wizard Vief", "favours the yellow ones.").also { stage++ }
            27 -> npc("You will also have a third wand, which allows you to", "create magical barriers to block the opposing team's", "orbs.").also { stage++ }
            28 -> npc("After two minutes, the team that absorbed the most orbs", "wins that altar. I then open the portal to the next altar", "in the sequence. After you have visited all eight altars,", "you will be returned here.").also { stage++ }
            29 -> showTopics(
                Topic("What's in it for me?", 46),
                Topic("Could you go over the instructions again?", 22),
                Topic("Which colour orb is best?", 31),
                Topic("Thanks.", END_DIALOGUE),
            )

            31 -> npc("Wizard Acantha believes that the green orbs are best.", "Wizard Vief believe that the yellow ones are. You", "should help out the wizard whose team you join.").also { stage++ }
            32 -> player("But what do you think?").also { stage++ }
            33 -> npc("Does it matter?").also { stage = 62 }

            // Where are we exactly?
            38 -> npc("You will notice that, whenever you use a Runecrafting", "altar, you enter another plane: a self-contained island, or", "cave, or some other place, which contains the true altar.").also { stage++ }
            39 -> npc("These temples are not exactly in " + GameWorld.settings!!.name + ". They are pocket", "dimensions unto themselves: areas of folded space created", "by the energy of the rune altar.").also { stage++ }
            40 -> showTopics(
                Topic("So we're in something similar?", 42),
                Topic("What does that have to do with the guild?", 45),
                Topic("Not the Astral Altar. That has no pocket dimension.", 43),
            )
            42 -> npcl(FaceAnim.FRIENDLY, "Quite right. This is a shadow of Wizard's Tower, created by our own magic - also known as the Runespan. What better place to study the mysteries of Runecrafting?").also { stage = END_DIALOGUE }
            43 -> npc("Quite right. I have heard of the Astral Altar,", "although I have not been there myself.").also { stage++ }
            44 -> npc("The lunar wizards have found a way to keep the altar open.", "Their magic has flattened out the space around the altar", "so the pocket dimension becomes part of normal space.").also { stage = END_DIALOGUE }
            45 -> npcl(FaceAnim.FRIENDLY, "Don't you see? The Runecrafting Guild exists in a similar pocket dimension, created by our own magic - also known as the Runespan. What better place to study the mysteries of Runecrafting?").also { stage = END_DIALOGUE }

            // Tokens.
            46 -> npc("A fair question. We have agreed on a token scheme", "that allows you to choose from several rewards. When", "you return from the last altar, your senior wizard will", "give you a number of tokens.").also { stage++ }
            47 -> npc("You will get 50 tokens per altar that your team captured,", "provided that you contributed to the capture in some way.", "You will get an extra 100 tokens if your team captured", "more altars overall, or 50 extra if it is a draw.").also { stage++ }
            48 -> npc("You can exchange the tokens for rewards by speaking to me.", "You may also find rune essence appearing in your inventory", "at the end of each round. This is a side-product of the", "absorption process and you are free to use it as you wish.").also { stage++ }
            49 -> showTopics(
                Topic("I have some tokens I'd like to cash in.", 1),
                Topic("What rewards are there?", 51),
                Topic("Could you go over the instructions again?", 22),
                Topic("Which colour orb is best?", 31),
                Topic("Thanks.", END_DIALOGUE),
            )

            51 -> npc("The rewards include runemaster robes, designed to", "protect you while Runecrafting. These robes also", "let you move orbs a little further - if you wear", "robes of the same colour as the orb.").also { stage++ }
            52 -> npc("Another reward is the Runecrafting staff. This", "can be combined with a talisman, in the same way", "that a tiara can.").also { stage++ }
            53 -> npc("I also offer teleport tablets to the various altars.", "You may also trade your tokens in for talismans and", "certificates you can exchange at a bank for", "rune essence.").also { stage = 74 }
            74 -> showTopics(
                Topic("I have some tokens I'd like to cash in.", 1),
                Topic("Could you go over the instructions again?", 22),
                Topic("Which colour orb is best?", 31),
                Topic("Thanks.", END_DIALOGUE),
            )

            // Omni-talisman.
            54 -> npcl(FaceAnim.FRIENDLY, "I have spent many years studying the Runecrafting talismans, and I believe I can create a new form of talisman that combines the properties of all of them.").also { stage++ }
            55 -> npcl(FaceAnim.FRIENDLY, "This omni-talisman will allow you to access any of the Runecrafting altars. It can be combined with a tiara or a staff, just like an ordinary talisman.").also { stage++ }
            56 -> npc("If you show me each type of known talisman, I will", "create an omni-talisman for you. For each talisman you", "show me, I will also teach you a bit about Runecrafting.").also {
                setAttribute(player, GameAttributes.RC_GUILD_TALISMAN_TASK_START, true)
                stage = 57
            }
            57 -> showTopics(
                Topic("I have a talisman to show you.", 2),
                Topic("Never mind.", END_DIALOGUE),
            )

            // Korvak.
            58 -> npc("Wizard Korvak is the only one of us to have", "visited the Abyss. He learned about rune pouches", "and how to repair them.").also { stage++ }
            59 -> npc("None of us quite knows how he does it, and", "I'm not sure he does either, but it seems to work.").also { stage = 0 }

            60 -> player("Oh yeah.").also { stage = END_DIALOGUE }

            // Acantha and Vief are hardly working together!
            61 -> npcl(FaceAnim.THINKING, "Aren't they? They think that their debate about orb colour is so important, but do you really think it matters which team you join?").also { stage++ }
            62 -> showTopics(
                Topic("Of course it matters.", 63),
                Topic("No, I suppose not.", 64),
                Topic("Never mind.", END_DIALOGUE),
            )
            63 -> npcl(FaceAnim.FRIENDLY, "Of course it does, of course it does. Be careful which team you join, then. I'll accept your reward tokens, either way.").also { stage = 0 }
            64 -> npcl(FaceAnim.FRIENDLY, "No. The important thing is that the orbs get pushed back into the altars, whatever colour they are.").also { stage = 0 }

            // Omni-talisman reward.
            65 -> npc("Excellent! You've shown me enough talismans. I can", "give you an omni talisman now.").also { stage = 3 }

            // Lost omni-talisman.
            66 -> player("Yes.").also { stage++ }
            67 -> npcl(FaceAnim.FRIENDLY, "I suppose I can make you a new one, but the materials need to be paid for. I suppose I could part with another one for 50,000 coins.").also { stage++ }
            68 -> showTopics(
                Topic("Pay the 50,000 coins.", 69),
                Topic("Not right now.", 70),
            )
            69 -> {
                if (removeItem(player!!, Item(Items.COINS_995, 50000))) {
                    addItemOrDrop(player!!, Items.OMNI_TALISMAN_13649)
                    npc("Oh, well. Here's another one. Do try to be careful with", "it this time.").also { stage = END_DIALOGUE }
                } else {
                    sendMessage(player!!, "You don't have enough coins for that.")
                    end()
                }
            }
            70 -> npcl(FaceAnim.FRIENDLY, "Oh, well. If you change your mind I will be here.").also { stage = END_DIALOGUE }

            72 -> npc(FaceAnim.FRIENDLY, "Was there something else you wanted?").also { stage = 0 }

            // Main menu, page 2.
            100 -> showTopics(
                Topic("What can I do here?", 19),
                Topic("Can I buy some tokens?", 200),
                Topic("Never mind.", END_DIALOGUE),
            )

            // [Charm] Ring of charos (a) branch.
            80 -> npcl(FaceAnim.FRIENDLY, "You do seem trustworthy. I suppose it can't hurt to tell you a little.").also { stage++ }
            81 -> npcl(FaceAnim.FRIENDLY, "I am searching for an artefact that gives off a very specific type of energy. After many years of research, I think I'm finally drawing near.").also { stage++ }
            82 -> showTopics(
                Topic("How are you looking for it?", 83),
                Topic("What artefact are you looking for?", 93),
                Topic("Well, good luck with that.", 0),
            )
            83 -> if (!wearsRunecrafterRobes()) {
                npcl(FaceAnim.FRIENDLY, "You are barely a member of this guild. Anyone can see you don't fit in here. I don't think I need to explain my methods to you.").also { stage = 84 }
            } else {
                npcl(FaceAnim.FRIENDLY, "You have already been helping me, actually. I am using the Runecrafting altars to triangulate the artefact's position. Every time Acantha and Vief send teams through around the altars, the signal's position becomes a little clearer.").also { stage = 85 }
            }
            84 -> showTopics(
                Topic("What artefact are you looking for?", 93),
                Topic("Well, good luck with that.", 0),
            )
            85 -> player(FaceAnim.ANNOYED, "You were using me!").also { stage++ }
            86 -> npcl(FaceAnim.FRIENDLY, "People use one another. That is how life is. Years ago, a treasure hunter used me on his quest for this artefact - used me in a far more hurtful way than I've used you.").also { stage++ }
            87 -> npcl(FaceAnim.FRIENDLY, "After he left, however, I worked out what he was after and it set me on this line of research. If you are strong enough, you will learn from this. If not, I have no time for you and no sympathy.").also { stage++ }
            88 -> playerl(FaceAnim.HALF_ASKING, "Who was it? What happened?").also { stage++ }
            89 -> npcl(FaceAnim.SAD, "We were apprentices together here. He left after graduating, but a few years later he came back. I was young and naive and fell in love. I thought he would stay at the tower but he never wanted me to be a proper wizard.").also { stage++ }
            90 -> npcl(FaceAnim.SAD, "He was a treasure hunter, stealing magical artefacts from tombs and selling them for base gold. He used me to get at the tower's records and was never interested in me. He only wanted to look in the records for clues about the artefact's location.").also { stage++ }
            91 -> npcl(FaceAnim.SAD, "I saw what he was reading and tried to help him, but he was angry. He wanted to keep his research secret from me! Once he had what he was looking for, he left without a word.").also { stage++ }
            92 -> npcl(FaceAnim.ANGRY, "Well, now I have no use for him. I went over what he had been reading and found the clues he had found. I will find that artefact, and I will use its power to crush him like an insect.").also { stage = 82 }
            93 -> if (!wearsRunecrafterRobes()) {
                npcl(FaceAnim.FRIENDLY, "Even if I told you, it would mean nothing to you. You are not yet ready for Runecrafting's inner secrets.").also { stage = 94 }
            } else {
                npcl(FaceAnim.FRIENDLY, "I know you are an experienced runecrafter, unlike the wizards of the tower. Perhaps you will understand.").also { stage = 95 }
            }
            94 -> showTopics(
                Topic("How are you looking for it?", 83),
                Topic("Well, good luck with that.", 0),
            )
            95 -> npcl(FaceAnim.FRIENDLY, "Have you ever wondered how the rune essence and rune altars came to be? How the altars and rune essence can work together when they are so spread out?").also { stage++ }
            96 -> npcl(FaceAnim.FRIENDLY, "The wizards called this the Eye of Saradomin. That is what I am looking for. None of the other wizards believe it is possible, which is why I am working secretly. When I find it, its power will be all mine.").also { stage = 82 }

            // Buying tokens.
            200 -> npcl(FaceAnim.FRIENDLY, "Sure, there will be no problem with it, the cost of one token is 100 coins, how many will you need?").also { stage = 201 }
            201 -> {
                setTitle(player, 4)
                sendOptions(player, "How many tokens do you need?", "50", "250", "1000", "5000").also { stage++ }
            }
            202 -> {
                val amount = when (buttonId) {
                    1 -> 50
                    2 -> 250
                    3 -> 1000
                    4 -> 5000
                    else -> 0
                }
                if (amount > 0) {
                    if (removeItem(player!!, Item(Items.COINS_995, 100 * amount))) {
                        addItemOrDrop(player!!, Items.RUNECRAFTING_GUILD_TOKEN_13650, amount)
                    } else {
                        sendMessage(player!!, "You don't have enough coins for that.")
                    }
                }
                end()
            }
        }
        return true
    }

    private fun hasOmniTalisman() = hasAnItem(player, Items.OMNI_TALISMAN_13649).container != null

    private fun completedTask() = getAttribute(player, GameAttributes.RC_GUILD_TALISMAN_TASK_COMPLETE, false)

    private fun talismanShown(talisman: Int) = getAttribute(player, GameAttributes.RC_GUILD_TALISMAN + "_$talisman", false)

    /**
     * Ring of charos (a).
     */
    private fun wearsCharosRing() = inEquipment(player!!, 6465)

    /**
     * Any colour of the Runecrafter robes (checked by item name).
     */
    private fun wearsRunecrafterRobes() = player!!.equipment.toArray().count { it != null && it.name.startsWith("Runecrafter") } >= 3

    override fun getIds(): IntArray = intArrayOf(NPCs.WIZARD_ELRISS_8032)
}