package content.global.activity.tog

import content.data.Quests

import content.data.GameAttributes
import content.region.fremennik.rellekka.quest.viking.FremennikTrials
import content.region.misthalin.lumbridge.quest.tog.TearsOfGuthix
import core.api.*
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.dialogue.Topic
import core.game.dialogue.splitLines
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.DARK_RED
import core.tools.END_DIALOGUE
import core.tools.Log
import shared.consts.Items
import shared.consts.NPCs
import kotlin.random.Random

/**
 * Represents the Juna dialogue for Tears of Guthix activity.
 * @author szu
 */
@Initializable
class JunaDialogue(player: Player? = null) : Dialogue(player) {

    private var dialogueNodes: List<DialogueNode> = emptyList()
    private var nodeIndex = 0

    override fun open(vararg args: Any?): Boolean {
        val wait = TearsOfGuthix.daysLeft(player) > 0
        val xpMissing = TearsOfGuthix.xpLeft(player) > 0
        val qpMissing = TearsOfGuthix.questPointsLeft(player) > 0

        if (wait) {
            npcl(FaceAnim.OLD_DEFAULT, "You must wait longer before entering the Tears of Guthix cave.")
            stage = 100
            return true
        }

        if (getQuestPoints(player) == 0 || (xpMissing && qpMissing)) {
            npcl(FaceAnim.OLD_DEFAULT, "You need more experience and quest points before entering the Tears of Guthix cave.")
            stage = 100
            return true
        }

        npc(FaceAnim.OLD_DEFAULT, "Tell me... a story...")
        stage = 2
        return true
    }

    private fun buildQuestStories(): List<DialogueNode> {
        val possibleStories = mutableListOf<List<DialogueNode>>()

        val questStories = mapOf(
            Quests.ANIMAL_MAGNETISM.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and so the attractor works using an undead chicken and a magnet."),
                DialogueNode(DialogueType.NPC, "How odd.")
            ),
            Quests.ANOTHER_SLICE_OF_HAM.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and the Dorgeshuun city is now connected to Keldagrim by a rapid train line."),
                DialogueNode(DialogueType.NPC, "I am always glad to hear tales of the Dorgeshuun.")
            ),
            Quests.BACK_TO_MY_ROOTS.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...So I defeated the wild vine and learned how to care for my own Jade Vine.")
            ),
            Quests.BETWEEN_A_ROCK.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...I had to be shot out of a cannon into a rock! But I finally banished the spirit and Dondakan could mine his gold.")
            ),
            Quests.BIG_CHOMPY_BIRD_HUNTING.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...poor Rantz was so clumsy he couldn't shoot anything, but I managed to kill the Chompy bird.")
            ),
            Quests.BLACK_KNIGHTS_FORTRESS.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "So in the end the Black Knights were defeated by cabbage!"),
                DialogueNode(DialogueType.NPC, "One should never underestimate the uses of the vegetables of Guthix.")
            ),
            Quests.BIOHAZARD.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...it turned out there was no plague after all!"),
                DialogueNode(DialogueType.NPC, "Deception of the people by their rulers is a terrible thing.")
            ),
            Quests.CATAPULT_CONSTRUCTION.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...And that was how I repaired the catapult in the Tyras camp."),
                DialogueNode(DialogueType.NPC, "Catapults are terrible devices. They move rocks from their proper Guthix-assigned positions.")
            ),
            Quests.CABIN_FEVER.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "So I holed the enemy ship with another cannon shot, and the Adventurous sailed triumphantly into Mos Le'Harmless!"),
            ),
            Quests.CLOCK_TOWER.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "So it was I who repaired the clock tower."),
            ),
            Quests.COLD_WAR.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "So I told Larry about the penguins' plot!"),
                DialogueNode(DialogueType.NPC, "The paranoia and ambition of the penguins' leaders will not serve their people well.")
            ),
            Quests.COOKS_ASSISTANT.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and in the end I found all the ingredients, so the Duke of Lumbridge had a birthday cake after all."),
                DialogueNode(DialogueType.NPC, "Ah, a happy ending. It would not be good for such an anniversary to go unmarked.")
            ),
            Quests.CONTACT.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and after I'd defeated the giant scarab, the High Priest gave me a reward lamp to teach me more combat skills."),
            ),
            Quests.CREATURE_OF_FENKENSTRAIN.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "But in the end I stopped Fenkenstrain from continuing his horrible experiments."),
                DialogueNode(DialogueType.NPC, "I am sure you will have many more adventures on behalf of the Myreque. Then, perhaps, you will have even more stories to tell me.")
            ),
            Quests.DEATH_PLATEAU.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "So from then on the Imperial Guard was able to access Death Plateau safely and take the trolls by surprise."),
            ),
            Quests.DEATH_TO_THE_DORGESHUUN.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "Sigmund escaped again, but Zanik and I destroyed the machine and Dorgesh-Kaan was saved!"),
                DialogueNode(DialogueType.NPC, "Zanik still has her destiny to fulfil, and I have a feeling you will have a part in that.")
            ),
            Quests.DEFENDER_OF_VARROCK.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...The Shield of Arrav glowed with lightning, and Dimintheis used it to defeat the zombie army."),
            ),
            Quests.DEALING_WITH_SCABARAS.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...And that was how I dealt with the High Priest of Scabaras."),
                DialogueNode(DialogueType.NPC, "The desert gods are strange, but you must be sure to pay them the proper respect.")
            ),
            Quests.DEMON_SLAYER.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "So I destroyed the demon Delrith and saved Varrock!"),
                DialogueNode(DialogueType.NPC, "I remember Delrith. A most unpleasant character; I am glad he has been dispatched.")
            ),
            Quests.DESERT_TREASURE.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "In the pyramid I discovered a whole new set of magic spells!"),
                DialogueNode(DialogueType.NPC, "I suggest you think about the origin of those spells. Ancient magics are not to be toyed with.")
            ),
            Quests.DEVIOUS_MINDS.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "So it was me who accidentally brought a teleport beacon onto Entrana! But the Temple Knights are investigating so it should be all right.")
            ),
            Quests.THE_DIG_SITE.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and the examiner was very impressed that I had discovered an ancient altar of Zaros."),
                DialogueNode(DialogueType.NPC, "Zaros? I had not heard that name for a thousand years even before the start of my sojourn here.")
            ),
            Quests.DORICS_QUEST.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "So once I had got all the ores he wanted, Doric let me use his anvils."),
                DialogueNode(DialogueType.NPC, "Such a small task hardly seems worthy of the term 'quest'.")
            ),
            Quests.DRAGON_SLAYER.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "So with Elvarg the dragon dead, the master of the Champions' Guild let me in, and I was able to wear Rune Plate!")
            ),
            Quests.DREAM_MENTOR.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "So Cyrisus was free of his fear of combat, and he headed off to be an adventurer."),
                DialogueNode(DialogueType.NPC, "Fear is the most insidious enemy of all.")
            ),
            Quests.DRUIDIC_RITUAL.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "So Kaqemeex taught me how to use the Herblore skill."),
                DialogueNode(DialogueType.NPC, "A generous reward indeed.")
            ),
            Quests.DWARF_CANNON.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and that was how I fixed the Dwarf multicannon."),
                DialogueNode(DialogueType.NPC, "So war still rages in the world above? Will you never tire of creating machines of destruction?")
            ),
            Quests.EADGARS_RUSE.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "Eadgar's ruse was absurd, but it worked! The trolls ate a fake human, and I got the herb for the druids' ritual.")
            ),
            Quests.EAGLES_PEAK.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...so Nickolaus taught me how to catch a new ferret for the zoo.")
            ),
            Quests.ELEMENTAL_WORKSHOP_I.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and once I had repaired the Elemental Workshop I was able to make an Elemental Shield.")
            ),
            Quests.ELEMENTAL_WORKSHOP_II.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...so I instilled the power of my mind into the primed bar and smithed it into an Elemental Mind Helm!"),
                DialogueNode(DialogueType.NPC, "You should be careful about meddling with your own head.")
            ),
            Quests.ENAKHRAS_LAMENT.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and then Akthanakos and Enakhra went away to the north to continue fighting!"),
                DialogueNode(DialogueType.NPC, "A battle between two such powerful beings will take much time and unleash much energy. I hope that the natural world is not damaged in this conflict!")
            ),
            Quests.ENLIGHTENED_JOURNEY.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...so we landed the balloon in Taverley, and Auguste thanked me for helping him get his idea off the ground.")
            ),
            Quests.ERNEST_THE_CHICKEN.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "So once I had found all the parts for the machine, poor Ernest could be himself once more."),
                DialogueNode(DialogueType.NPC, "That was a good deed. It is a terrible thing to be locked out of one's natural form.")
            ),
            Quests.THE_EYES_OF_GLOUPHRIE.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...the machine revealed that the cute creatures were actually Arposandran spies! I destroyed them, and King Narnode gave me a crystal seed that belonged to Oaknock the Engineer.")
            ),
            Quests.FAIRYTALE_I_GROWING_PAINS.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "So I defeated the tanglefoot and returned the Fairy Queen's Enchanted Secateurs to the Fairy Godfather."),
                DialogueNode(DialogueType.NPC, "These fairies seem to be well versed in the powers of nature.")
            ),
            Quests.FAIRYTALE_II_CURE_A_QUEEN.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...the Fairy Queen awoke, and we realised that the Godfather had betrayed her!"),
                DialogueNode(DialogueType.NPC, "Politics is never straightforward.")
            ),
            Quests.FAMILY_CREST.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "So all three parts of the family crest were reunited.")
            ),
            Quests.THE_FEUD.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...So despite the failure of my original task, Ali Morrisane was very happy about his nephew's fortunes.")
            ),
            Quests.FIGHT_ARENA.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and I defeated General Khazard, and won the Servils their freedom!")
            ),
            Quests.FISHING_CONTEST.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and after I had won the fishing contest, the Dwarves let me go under White Wolf Mountain."),
                DialogueNode(DialogueType.NPC, "Fishing? A strange test of worthiness to pass through an underground tunnel!")
            ),
            Quests.FORGETTABLE_TALE.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "I think I did something with a drunken dwarf... but I can't remember what."),
                DialogueNode(DialogueType.NPC, "You must be careful not to let drink muddle your brain!")
            ),
            Quests.THE_FREMENNIK_ISLES.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...so I presented the head of the Troll King to the Burgher and he gave me his own helm!")
            ),
            Quests.THE_FREMENNIK_TRIALS.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and that was how I became an honourary member the Fremennik, and was given my Fremennik name, ${FremennikTrials.getFremennikName(player)}.")
            ),
            Quests.GARDEN_OF_TRANQUILLITY.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...so the garden at Varrock palace is all my work!"),
                DialogueNode(DialogueType.NPC, "It is good that you can take time from slaying and adventuring to grow a beautiful garden.")
            ),
            Quests.GERTRUDES_CAT.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...I returned Fluffs safely to Gertrude and she gave me a cat of my own!"),
                DialogueNode(DialogueType.NPC, "Cats are one of the most mysterious creatures of Guthix. I hope you take your responsibility seriously.")
            ),
            Quests.GHOSTS_AHOY.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "So the people of Port Phasmatys were finally able to rest.")
            ),
            Quests.THE_GIANT_DWARF.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "So the statue in Keldagrim was rebuilt, and I witnessed a meeting of the Consortium of mining companies."),
                DialogueNode(DialogueType.NPC, "I have a feeling that Keldagrim will be the setting of many great adventures for you.")
            ),
            Quests.GOBLIN_DIPLOMACY.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "So the goblins ended up wearing the armour colour they had to start off with!"),
                DialogueNode(DialogueType.NPC, "Poor silly goblins! Their race had such potential, if only they could rise above their petty squabbles.")
            ),
            Quests.THE_GOLEM.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and I had to reprogram the golem before it would believe that the demon was dead."),
                DialogueNode(DialogueType.NPC, "I remember well the battle of Uzer.")
            ),
            Quests.THE_GRAND_TREE.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "Glough fled like the coward he really is, and the Grand Tree was saved!")
            ),
            Quests.THE_GREAT_BRAIN_ROBBERY.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...so I defeated Mi-Gor's barrel-chested bodyguard, and the monks were saved.")
            ),
            Quests.GRIM_TALES.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and once I'd chopped down the beanstalk, Sylas let me keep Rupert's helmet."),
                DialogueNode(DialogueType.NPC, "Your tales are often very curious, ${player.username}.")
            ),
            Quests.THE_HAND_IN_THE_SAND.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "So that was how I gave Bert a hand solving the murder and returned the wizard's head to solve the riddle of the ever full sand pits!"),
                DialogueNode(DialogueType.NPC, "A victim dies and a murderer is punished. That is balance.")
            ),
            Quests.HAUNTED_MINE.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and from the legendary crystals I cut a salve shard to fight the undead.")
            ),
            Quests.HAZEEL_CULT.questName to listOf(
                DialogueNode(DialogueType.PLAYER, if(getAttribute(player, "hazeelcult:mahjarrat", false)) "So that was how I helped return Hazeel to his followers." else "So that was how I foiled the cultists in their plan to resurrect Hazeel."),
                DialogueNode(DialogueType.NPC, "The Mahjarrat are a vile race. You would do well to steer clear of them.")
            ),
            Quests.HEROES_QUEST.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "So after I had retrieved all the items, I became a member of the Heroes' Guild!")
            ),
            Quests.THE_HAND_IN_THE_SAND.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...so that was how I gave Bert a hand solving the murder and returned the wizard's head, solving the riddle of the ever-full sand pits!")
            ),
            Quests.HOLY_GRAIL.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and out of all the Knights of the Round Table, it was I who found the Holy Grail.")
            ),
            Quests.HORROR_FROM_THE_DEEP.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and that was the end of the horror from the deep!")
            ),
            Quests.ICTHLARINS_LITTLE_HELPER.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and with the amulet I received, I could understand the language of cats!"),
                DialogueNode(DialogueType.NPC, "It is a rare privilege to understand the language of those beings. You should listen carefully to all that they say - but not believe too much of it.")
            ),
            Quests.IMP_CATCHER.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "It took some time, but I finally got all four beads back, and Mizgog gave me my reward."),
                DialogueNode(DialogueType.NPC, "Imps! I remember the age of great war, when armies of Zamorak's imps bloodied the ankles of the other gods' creatures.")
            ),
            Quests.IN_AID_OF_THE_MYREQUE.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...Veliaf was very impressed by the Rod of Ivandis, and I can use it to kill juves and the juvinates!")
            ),
            Quests.IN_SEARCH_OF_THE_MYREQUE.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "So I suppose it was my fault that the young members of the Myreque were killed by Vanstrom.")
            ),
            Quests.JUNGLE_POTION.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and once I had gathered all the herbs, Trufitus Shakaya was able to commune with his gods.")
            ),
            Quests.KINGS_RANSOM.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...meanwhile Merlin used his magic to eject the Sinclairs from Camelot. King Arthur rewarded me for saving him.")
            ),
            Quests.KENNITHS_CONCERNS.questName to listOf(
                DialogueNode(DialogueType.PLAYER, 	"...And my last step was to bring Kennith his toy train.")
            ),
            Quests.THE_KNIGHTS_SWORD.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "So that was how I found the Imcando Dwarves and got the Knight a new sword.")
            ),
            Quests.LAND_OF_THE_GOBLINS.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "Oldak and I escaped from Yu'biusk before the portal closed, but Zanik was trapped in the box.")
            ),
            Quests.LEGACY_OF_SEERGAZE.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...I slew a Vyrewatch, as Safalaan had asked, and took the corpse to the Paterdomus Columbarium to cremate it. In gratitude the spirit left a key which allowed me to gain access to their worldly wealth."),
                DialogueNode(DialogueType.PLAYER, "My first prize was a blood talisman!"),
                DialogueNode(DialogueType.NPC, "With your mind so focused on material wealth, I worry whether the intrigues overshadowing your discoveries are being properly looked into."),
                DialogueNode(DialogueType.NPC, "Drakan, I fear, is a whole world of trouble for which you are ill-prepared.")
            ),
            Quests.LEGENDS_QUEST.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and when I had completed all the tasks, I became a member of the Legends' Guild!")
            ),
            Quests.LOST_CITY.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and when I entered the door carrying the Dramen Staff, I was transported to a whole new world -- a world populated by magical fairies!")
            ),
            Quests.THE_LOST_TRIBE.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "So Sigmund was dismissed, and the duke and ruler of the cave goblins signed a peace treaty."),
                DialogueNode(DialogueType.NPC, "The Dorgeshuun goblins have been good neighbors during my vigil here. They are a timid race, but not cowardly, and I am glad they have the peace they desire.")
            ),
            Quests.LUNAR_DIPLOMACY.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...so I defeated my mirror image, won the respect of the Moon Clan and persuaded them to communicate with the Fremenniks!"),
                DialogueNode(DialogueType.NPC, "The Moon Clan's reliance on magic is no more balanced than the Fremennik's hatred of it.")
            ),
            Quests.MAKING_HISTORY.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...so that's how the Outpost was saved and converted into a museum."),
                DialogueNode(DialogueType.NPC, "By remembering the past, one should be able to avoid repeating its mistakes. Yet the mortal races show no sign of abandoning their obsession with war and bloodshed.")
            ),
            Quests.MERLINS_CRYSTAL.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and when I told King Arthur that I had single-handedly freed Merlin from his crystal prison, he made me a knight of the Round Table!")
            ),
            Quests.MY_ARMS_BIG_ADVENTURE.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...My Arm is still up on that rooftop, growing goutweed in his little farming patch!"),
                DialogueNode(DialogueType.NPC, "it is good that a troll wishes to study the complex skill that is Farming.")
            ),
            Quests.MYTHS_OF_THE_WHITE_LANDS.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...I went through the snow imps' prank. Explorer Jack had no idea the 'riches' were really frozen yeti dung!"),
                DialogueNode(DialogueType.NPC, "It is pleasant to hear that the snow imps are still up to their old tricks.")
            ),
            Quests.MONKS_FRIEND.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and the monks threw a big birthday party for the child, with lots of wine."),
                DialogueNode(DialogueType.NPC, "I hope they supplied something more suitable for the child to drink.")
            ),
            Quests.MONKEY_MADNESS.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...so I defeated the Jungle Demon and stopped the Monkeys' plot to take over Karamja."),
                DialogueNode(DialogueType.NPC, "I see they couldn't make a monkey out of you!"),
                DialogueNode(DialogueType.PLAYER, "Well, actually, I was a monkey for a bit.")
            ),
            Quests.MOUNTAIN_DAUGHTER.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "I was too late to save the chieftain's daughter, but at least I gave her a proper burial."),
                DialogueNode(DialogueType.NPC, "Brief is life, but more precious if its passing is properly honoured.")
            ),
            Quests.MOURNINGS_END_PART_I.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "So that's how I found out about the lost temple in the mountains that the mourners were searching for.")
            ),
            Quests.MOURNINGS_END_PART_II.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "What kind of twisted mind would come up with the light-beam puzzle in that temple?")
            ),
            Quests.MURDER_MYSTERY.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and that's how I solved the murder of Lord Sinclair.")
            ),
            Quests.MY_ARMS_BIG_ADVENTURE.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "My Arm is still up on that rooftop, growing goutweed in his little farming patch!"),
                DialogueNode(DialogueType.NPC, "It is good that a troll wishes to study the complex skill that is Farming."),
                DialogueNode(DialogueType.PLAYER, "My Arm carried on growing goutweed in his little farming patch on that rooftop, until Drunken Dwarf's Leg took over."),
                DialogueNode(DialogueType.NPC, "The names of trolls never cease to amaze me.")
            ),
            Quests.NATURE_SPIRIT.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...thus Filliman Tarlock became a Nature Spirit!"),
                DialogueNode(DialogueType.NPC, "That is a fitting reward for a dedicated servant of Guthix. Thank you for helping him.")
            ),
            Quests.OBSERVATORY_QUEST.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and when I had fixed the telescope, I looked through and saw the stars."),
                DialogueNode(DialogueType.NPC, "It is long since I have seen the stars...")
            ),
            Quests.OLAFS_QUEST.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...where I found a note from Ulfric Longbeard and some treasure!")
            ),
            Quests.ONE_SMALL_FAVOUR.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "I still can't believe how long that 'one small favour' took me! I ended up walking all over the world doing small favours for all and sundry!")
            ),
            Quests.THE_PATH_OF_GLOUPHRIE.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...the room filled up with gas and I nearly died, but Hazelmere came to my rescue."),
                DialogueNode(DialogueType.NPC, "A truly exciting tale of failing and needing to be rescued.")
            ),
            Quests.PIRATES_TREASURE.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and when I dug in the middle of the park in Falador, I found the pirate's treasure!"),
                DialogueNode(DialogueType.NPC, "Such is ever the folly of pirates, they bury their loot in the ground so that another can dig it up.")
            ),
            Quests.PLAGUE_CITY.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and that was how I rescued Elena from West Ardougne.")
            ),
            Quests.PRIEST_IN_PERIL.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "But with Drezel's help I was able to purify the Salve.")
            ),
            Quests.IN_PYRE_NEED.questName to listOf(
                DialogueNode(DialogueType.PLAYER, 	"...And that was how I helped the phoenix to be reborn!"),
                DialogueNode(DialogueType.NPC, "You have performed a service to one of the creatures of Guthix, and for that I thank you.")
            ),
            Quests.PRINCE_ALI_RESCUE.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and I had to disguise Prince Ali in order to smuggle him out!")
            ),
            Quests.PERILS_OF_ICE_MOUNTAIN.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...So now Nurmof's pickaxe machine is powered by a windmill."),
                DialogueNode(DialogueType.NPC, "You acted as a true servant of Guthix in this matter.")
            ),
            Quests.RAG_AND_BONE_MAN.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "So I fetched all the bones the Odd Old Man wanted, and he gave me a reward!"),
                DialogueNode(DialogueType.NPC, "What could that man have wanted all those bones for? Collecting that many bones cannot be a balanced activity.")
            ),
            Quests.RATCATCHERS.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "Those rat-catchers are odd people. But by the end of it I had caught a huge number of rats all over Gielinor!"),
                DialogueNode(DialogueType.NPC, "Rats are a highly unbalanced creature and it is right that their numbers be controlled.")
            ),
            Quests.RECIPE_FOR_DISASTER.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "So after I had freed all the dinner guests I was finally able to face the Culinaromancer and put an end to his mad schemes!"),
                DialogueNode(DialogueType.NPC, "That evil wizard's time spells sent a ripple through the fabric of balance. I am glad he is no longer at large!")
            ),
            Quests.RECRUITMENT_DRIVE.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "So I figured out their weird puzzles, and the Temple Knights asked me to join as an initiate member!"),
                DialogueNode(DialogueType.NPC, "I have heard tales of these 'Temple Knights' from many years past. Be wary, their motivations are not what they might lead you to believe.")
            ),
            Quests.REGICIDE.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "I returned to tell King Lathas that the assassination was successful, and he rewarded me.")
            ),
            Quests.THE_RESTLESS_GHOST.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and once I returned the skull, the ghost was able to rest."),
                DialogueNode(DialogueType.NPC, "A strange attachment to an item that has no use after one's death.")
            ),
            Quests.ROMEO_JULIET.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "I gave the message to Romeo, but he misunderstood, so they never were together."),
                DialogueNode(DialogueType.NPC, "Never was a story of more woe\nThat this of Juliet and her Romeo.")
            ),
            Quests.ROVING_ELVES.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and when I planted the shard, it grew into a crystal tree!")
            ),
            Quests.ROCKING_OUT.questName to listOf(
                DialogueNode(DialogueType.PLAYER, 	"...And that was how I heard the tale of the death of Rabid Jack the pirate."),
                DialogueNode(DialogueType.NPC, "The lives of pirates often make for good tales.")
            ),
            Quests.ROYAL_TROUBLE.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "So I showed that the war was based on a misunderstanding, and now Miscellania and Etceteria were at peace again!"),
                DialogueNode(DialogueType.NPC, "War? What is it good for?"),
                DialogueNode(DialogueType.PLAYER, "Absolutely nothing!")
            ),
            Quests.RUM_DEAL.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "So that was how I helped keep the zombie pirates drunk!"),
                DialogueNode(DialogueType.NPC, "Drunken zombie pirates? What is the world coming to?")
            ),
            Quests.RUNE_MYSTERIES.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...So, I brought Aubury's notes to Sedridor the head wizard and, from then on, I was able to mine rune essence."),
                DialogueNode(DialogueType.NPC, "So, the mortals above have discovered magic once more? Very interesting.")
            ),
            Quests.SCORPION_CATCHER.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...I would never have thought that three scorpions would take that long to find!")
            ),
            Quests.SEA_SLUG.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and that was how I rescued Caroline's family from the sea slugs.")
            ),
            Quests.SHADES_OF_MORTTON.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...So Mort'ton returned pretty much to normal, and I was able to put the shades to rest.")
            ),
            Quests.SHADOW_OF_THE_STORM.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...Denath was Agrith-Naar all along! But I managed to summon the demon again and slay him."),
                DialogueNode(DialogueType.NPC, "Agrith-Naar was one of the demons my master banished at the end of the great war. It seems he found a way to work around the ban on direct intervention.")
            ),
            Quests.SHEEP_HERDER.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...so the sheep were safely killed and incinerated.")
            ),
            Quests.SHEEP_SHEARER.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "Out of sheer hard work, I managed to shear some sheep for farmer Fred. Then I got in a spin and created some balls of wool."),
            ),
            Quests.SHIELD_OF_ARRAV.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...So that's how I helped return the Shield of Arrav.")
            ),
            Quests.SHILO_VILLAGE.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...So what Rashiliyia wanted all along was to be reunited with her son in the afterlife.")
            ),
            Quests.SMOKING_KILLS.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...who would have thought there were so many monsters down the well in Pollnivneach?"),
                DialogueNode(DialogueType.NPC, "You will find that monsters can hide in the most surprising of places. Indeed, those with friendly faces can be the closest things to a monster. Be most careful in your desert adventures.")
            ),
            Quests.THE_SLUG_MENACE.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...when I told Sir Tiffy that I had accidentally freed the Mother Mallum, he promoted me to the rank of Proselyte within the Temple Knights."),
                DialogueNode(DialogueType.NPC, "That was a strange response to such an unfortunate event. But the motives of the Temple Knights have always been incomprehensibly unbalanced.")
            ),
            Quests.A_SOULS_BANE.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...So I defeated all the monsters in Tolna's subconscious and he escaped from the rift."),
                DialogueNode(DialogueType.NPC, "The most fearsome monsters are to be found in the darkness of our own minds.")
            ),
            Quests.SPIRIT_OF_SUMMER.questName to listOf(
                DialogueNode(DialogueType.PLAYER, 	"...I trapped the Spirit Beast and the family could talk again, but they said I had only made it more powerful."),
                DialogueNode(DialogueType.NPC, "I have heard of that Spirit Beast. You did very well to even survive your encounter with it.")
            ),
            Quests.SPIRITS_OF_THE_ELID.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...So it turned out that the people of Nardah were dying of thirst because the spirits of the Elid had cursed them for turning away from their goddess."),
                DialogueNode(DialogueType.NPC, "It is a terrible thing for people to turn away from their religion. I think the spirits taught the town a lesson.")
            ),
            Quests.SUMMERS_END.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and after I defeated the Spirit Beast, Summer and her family were finally able to die."),
                DialogueNode(DialogueType.NPC, "The balance of life and death have been restored. It is good.")
            ),
            Quests.SWEPT_AWAY.questName to listOf(
                DialogueNode(DialogueType.PLAYER,
                    if(!getAttribute(player, GameAttributes.MINI_PURPLE_CAT_COMPLETE, false) && Random.nextBoolean())
                        "...And that was how I helped the witches prepare their goulash."
                    else
                        "In exchange for helping Wendy the witch, and her best friend, Lottie, I learned how to permanently dye my pet cat purple.")
            ),
            Quests.SWAN_SONG.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...So the Wise Old Man said it was time for him to retire, and he left the Colony and went home. He gave me loads of XP, but he wouldn't let me have his hat!"),
                DialogueNode(DialogueType.NPC, "A hat is just a hat, ${player.username}."),
                DialogueNode(DialogueType.PLAYER, "Grrr!")
            ),
            Quests.TAI_BWO_WANNAI_TRIO.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...So that was how I helped the people of Tai Bwo Wannai village return.")
            ),
            Quests.A_TAIL_OF_TWO_CATS.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...now Bob and Neite are very happy together!"),
                DialogueNode(DialogueType.NPC, "Bob the Cat? I have heard of that strange creature but have never been able to fathom him. It is no small achievement for you to be able to!"),
                DialogueNode(DialogueType.PLAYER, "... So Bob and Neite were very happy together, at least for a while."),
                DialogueNode(DialogueType.NPC, "The loss of a friend is never easy. However, you should always remember to be thankful of the time you had together.")
            ),
            Quests.TEARS_OF_GUTHIX.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...so you said you'd look after my bowl for me, and let me access the Tears if I kept telling you stories."),
                DialogueNode(DialogueType.NPC, "I knew that story already, in fact, but I suppose your own perspective of the events is a valid tale in itself.")
            ),
            Quests.TEMPLE_OF_IKOV.questName to listOf(
                DialogueNode(DialogueType.PLAYER, if(getAttribute(player, GameAttributes.QUEST_IKOV_SELECTED_END, 0) == 1) "I refused to give Lucien the Staff of Armadyl and had to fight him." else "...Lucien said that the Staff of Armadyl I gave him had made him more powerful."),
            ),
            Quests.THRONE_OF_MISCELLANIA.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...So that was how I became ${if(player.isMale) "King" else "Queen"} of Miscellania!")
            ),
            Quests.THE_TOURIST_TRAP.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...Ana wasn't too happy to be cooped up in that barrel! But at least I got her out of the mining camp.")
            ),
            Quests.TOWER_OF_LIFE.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...So, it turned out that the alchemists had created a Homunculus - a mixture of logic and magic."),
                DialogueNode(DialogueType.NPC, "Ah, that is good. New forms of life should always be created according to a balance between different principles.")
            ),
            Quests.TREE_GNOME_VILLAGE.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "..And King Bolren thanked me for defeating the warlord and returning the orbs to the gnome people.")
            ),
            Quests.TRIBAL_TOTEM.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...So I returned the totem, and the Rantuki tribe rewarded me for my help.")
            ),
            Quests.TOKTZ_KET_DILL.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and that's how I defeated the TokTz-Ket-Dill and helped to write a Tzhaar play."),
                DialogueNode(DialogueType.NPC, "I wish I could have seen your play, Player. I am always glad to hear your stories.")
            ),
            Quests.TROLL_ROMANCE.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...so Ug and Aga lived happily ever after.")
            ),
            Quests.TROLL_STRONGHOLD.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and, after I rescued Godric, Dunstan gave me two magic lamps as a reward!")
            ),
            Quests.UNDERGROUND_PASS.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...I don't know who put all those traps in the underground pass!")
            ),
            Quests.VAMPIRE_SLAYER.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and once the vampyre was dead, the people of Draynor no longer lived in fear.")
            ),
            Quests.WANTED.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and after I finally managed to track him down, let me tell you, that Solus Dellagar fellow was almost as powerful as he was insane!"),
                DialogueNode(DialogueType.NPC, "Yes, it seems he sacrificed his sanity for power. Power often comes with a cost, and the highest cost is often the one you do not immediately see...")
            ),
            Quests.WATCHTOWER.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and with the shield generator working, the ogres could never threaten Yanille again.")
            ),
            Quests.WATERFALL_QUEST.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...and that was how I retrieved the treasure from the waterfall.")
            ),
            Quests.WHAT_LIES_BELOW.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...so King Roald was freed from Surok's spell, and Rat thanked me for all my help.")
            ),
            Quests.WOLF_WHISTLE.questName to listOf(
                DialogueNode(DialogueType.PLAYER, 	"...And that was how I learned the secrets of summoning.")
            ),
            Quests.WHILE_GUTHIX_SLEEPS.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...but Lucien took the Stone of Jas and teleported away!"),
                DialogueNode(DialogueType.NPC, "The last time a Mahjarrat was in possession of the Stone, this world saw destruction unlike anything imaginable. Still, good may yet come of this. We shall see.")
            ),
            Quests.WITCHS_HOUSE.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...All that trouble just to get a ball out of someone's garden!"),
                DialogueNode(DialogueType.NPC, "It is often hard to know how long a task will take when we begin it.")
            ),
            Quests.WITCHS_POTION.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...And once I got her all the ingredients, Hetty's potion increased my magical power!"),
                DialogueNode(DialogueType.NPC, "I see you are on your way to becoming strong in the magical arts.")
            ),
            Quests.ZOGRE_FLESH_EATERS.questName to listOf(
                DialogueNode(DialogueType.PLAYER, "...Those Zogres were disgusting! But I learned how to fight them with brutal arrows and disease balm."),
                DialogueNode(DialogueType.NPC, "Zombie Ogres!? Some of the foulest creatures of Zamorak. In the great wars the zogre armies could repel the forces of Saradomin with their stench alone!")
            ),

            // TODO: miniquests.

            // getVarbit(player, 492) >= 4
            // "I gained access to the Abyss."

            // BarcrawlManager.getInstance(player).isFinished
            // "I completed my bar crawl card and earned access to the Barbarian Outpost agility course."

            // GameAttributes.ZAROS_COMPLETE
            // "I communed with six spirits, and learned more of an ancient entity whose name I probably shouldn't speak aloud. I also acquired ghostly robes."

            // Desert Slayer Dungeon
            // "In the Pollnivneach Slayer Dungeon, I defeated a monstrous cave crawler, basilisk boss, mightiest turoth and kurask overlord."

            // Hunt for Surok
            // "I hunted down Surok Magis in the Chaos Tunnels, and encountered a big ork."

            // Tarns lair
            // "I defeated Tarn Razorlor and learned his secret knowledge by reading the diary in his living quarters."

            // GameAttributes.RETURNING_CLARENCE_COMPLETE
            // "I lent the Yanille wizards' guild another hand by finding (most of) Clarence's remaining body parts. We managed to put all but one foot in the grave, and I had to hand it to the Yanille wizards...it was quite a touching funeral."

            // Rag and Bone man 2
            // "... So I fetched all the bones on the old man's wish-list, as well as the ones I'd brought him earlier."
        )

        questStories.forEach { (quest, story) ->
            if (hasRequirement(player, quest, false)) possibleStories.add(story)
        }

        if (possibleStories.isEmpty()) return emptyList()
        return possibleStories.random()
    }

    private fun showNextNode() {
        if (nodeIndex >= dialogueNodes.size) {
            stage = 20
            val safeStage = 20
            stage = safeStage
            handle(0, 0)
            return
        }

        val node = dialogueNodes[nodeIndex]
        when (node.type) {
            DialogueType.PLAYER -> player(*splitLines(node.text))
            DialogueType.NPC    -> npc(FaceAnim.OLD_NORMAL, *splitLines(node.text))
        }

        nodeIndex++
        stage = 30
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        val daysLeft = TearsOfGuthix.daysLeft(player)
        val xpLeft = TearsOfGuthix.xpLeft(player)
        val qpLeft = TearsOfGuthix.questPointsLeft(player)
        when (stage) {
            2 -> showTopics(
                Topic("Okay...", 31),
                Topic("A story?", 4),
                Topic("Not now.", 100),
                Topic("You tell me a story", 10),
            )
            31 -> sendDialogue(player, "You tell Juna some stories of your adventures.").also { stage++ }
            32 -> {
                dialogueNodes = buildQuestStories()
                if (dialogueNodes.isEmpty()) {
                    player("I... actually don't have any good stories right now.").also {
                        log(this::class.java, Log.WARN, "UNHANDLED QUEST STORY")
                    }
                    stage = 100
                } else {
                    nodeIndex = 0
                    showNextNode()
                }
            }

            30 -> showNextNode()

            4 -> npc(FaceAnim.OLD_CALM_TALK1, "I have been waiting here three thousand years,", "guarding the Tears of Guthix. I serve my", "master faithfully, but I am bored.").also { stage++ }
            5 -> npc(FaceAnim.OLD_CALM_TALK1, "An adventurer such as yourself must have many tales", "to tell. If you can entertain me, I will let you into the", "cave for a time.").also { stage++ }
            6 -> npc(FaceAnim.OLD_CALM_TALK1, "The more I enjoy your story, the more time I will give", "you in the cave.").also { stage++ }
            7 -> npc(FaceAnim.OLD_CALM_TALK1, "Then you can drink of the power of balance, which will", "make you stronger in whatever area you are weakest.").also { stage++ }
            8 -> showTopics(
                Topic("Okay...", 31),
                Topic("Not now.", 100),
                Topic("What are the Tears of Guthix?", 10),
            )
            10 -> npc(FaceAnim.OLD_CALM_TALK1, "The Third Age of the world was a time of great", "conflict, of destruction never seen before or since, when", "all the gods save Guthix warred for control.").also { stage++ }
            11 -> npc(FaceAnim.OLD_CALM_TALK1, "The colossal wyrms, of whom today's dragons are a", "pale reflection, turned all the sky to fire, while on the", "ground armies of foot soldiers, goblins and trolls and", "humans, filled the valleys and plains with blood.").also { stage++ }
            12 -> npc(FaceAnim.OLD_CALM_TALK1, "In time the noise of the conflict woke Guthix from His", "deep slumber, and He rose and stood in the centre of", "the battlefield so that the splendour of His wrath filled", "the world, and He called for the conflict to cease!").also { stage++ }
            13 -> npc(FaceAnim.OLD_CALM_TALK1, "Silence fell, for the gods knew that none could challenge", "the power of the mighty Guthix; for His power is that", "of nature itself, to which all other things are subject, in", "the end.").also { stage++ }
            14 -> npc(FaceAnim.OLD_CALM_TALK1, "Guthix reclaimed that which had been stolen from Him,", "and went back underground to return to His sleep and", "continue to draw the world's power into Himself.").also { stage++ }
            15 -> npc(FaceAnim.OLD_CALM_TALK1, "But on His way into the depths of the earth He sat and", "rested in this cave; and, thinking of the battle-scarred", "desert that now stretched from one side of His world to", "the other, He wept.").also { stage++ }
            16 -> npc(FaceAnim.OLD_CALM_TALK1, "And so great was His sorrow, and so great was His life-", "giving power, that the rocks themselves began to weep", "with Him.").also { stage++ }
            17 -> npc(FaceAnim.OLD_CALM_TALK1, "Later, Guthix noticed that the rocks continued to weep,", "and that their tears were infused with a small part of", "His power.").also { stage++ }
            18 -> {
                npc(FaceAnim.OLD_NORMAL, "So He set me, His servant, to guard the cave, and He", "entrusted to me the task of judging who was and was", "not worthy to access the tears.")
                setQuestStage(player, Quests.TEARS_OF_GUTHIX.questName, 1)
                stage = 100
            }
            20 -> npc(FaceAnim.OLD_HAPPY, "Your stories have entertained me. I will let you into", "the cave for a short time.").also {
                stage = if (!isQuestComplete(player, Quests.TEARS_OF_GUTHIX.questName)) {
                    if (inInventory(player, Items.STONE_BOWL_4704)) 25 else 21
                } else {
                    26
                }
            }
            21 -> npc(FaceAnim.OLD_CALM_TALK1, "But first you will need to make a bowl in which to", "collect the tears.").also { stage++ }
            22 -> npc(FaceAnim.OLD_CALM_TALK1, "There is a cave on the south side of the chasm that is", "infused with Guthix's power. Mine stone from there and", "craft a bowl.").also { stage++ }
            23 -> {
                npcl(FaceAnim.OLD_CALM_TALK1, "Bring the bowl to me, and then I will let you collect the tears.")
                setQuestStage(player, Quests.TEARS_OF_GUTHIX.questName, 1)
                stage = 100
            }
            24 -> {
                npc(FaceAnim.OLD_CALM_TALK1, "Mine some stone from that cave, make it into a bowl,", "and bring it to me.")
                setQuestStage(player, Quests.TEARS_OF_GUTHIX.questName, 1)
                stage = 100
            }

            25 -> player("I have a bowl.").also { stage++ }
            26 -> {
                if (!isQuestComplete(player, Quests.TEARS_OF_GUTHIX.questName)) {
                    npc(FaceAnim.OLD_CALM_TALK1, "I will keep your bowl for you, so that you may collect", "the tears many times in the future.")
                    stage = 27
                } else {
                    when {
                        daysLeft > 0 && xpLeft > 0 && qpLeft > 0 -> {
                            npcl(FaceAnim.OLD_CALM_TALK1, "Your stories have entertained me. But I will not permit any adventurer to access the tears more than once a week. Come back in $daysLeft days.")
                            stage = 29
                        }
                        xpLeft > 0 && qpLeft > 0 -> {
                            npc(FaceAnim.OLD_CALM_TALK1, "Your story has entertained me. But it is a poor sort", "of adventurer who only tells stories of the past and", "does not find new stories to tell. I will not let you", "into the cave again until you have had more adventures.")
                            stage = 29
                        }
                        daysLeft > 0 -> {
                            npcl(FaceAnim.OLD_CALM_TALK1, "Your stories have entertained me. But I will not permit any adventurer to access the tears more than once a week. Come back in $daysLeft days.")
                            stage = 100
                        }
                        !hasHandsFree(player) -> {
                            npc(FaceAnim.OLD_NORMAL, "But you must have both hands free to carry the bowl.", "Speak to me again when your hands are free.")
                            stage = 100
                        }
                        else -> {
                            npc(FaceAnim.OLD_CALM_TALK1, "Collect as much as you can from the blue streams. If", "you let in water from the green streams, it will take", "away from the blue. For Guthix is god of balance, and", "balance lies in the juxtaposition of opposites.")
                            stage = 28
                        }
                    }
                }
            }

            27 -> {
                if(removeItem(player, Items.STONE_BOWL_4704)) {
                    finishQuest(player, Quests.TEARS_OF_GUTHIX.questName)
                }
                stage = 100
            }

            28 -> {
                TearsOfGuthixActivity.startGame(player)
                stage = 100
            }

            29 -> {
                sendDialogue(player, "You cannot enter the cave again until you have gained either ${DARK_RED}one quest point</col> or ${DARK_RED}$xpLeft total XP</col>.")
                stage = 100
            }

            100 -> {
                end()
                stage = END_DIALOGUE
            }
        }

        return true
    }

    override fun getIds(): IntArray = intArrayOf(NPCs.TOG_JUNA_DUMMY_2023)

    enum class DialogueType { PLAYER, NPC }

    data class DialogueNode(val type: DialogueType, val text: String)
}
