package content.region.misthalin.silvarea.dialogue

import core.api.*
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs
import shared.consts.Quests

/**
 * Represents the Drezel dialogue.
 */
@Initializable
class DrezelDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        if (getQuestStage(player, Quests.PRIEST_IN_PERIL) == 13) {
            playerl(FaceAnim.HALF_GUILTY, "Hello.")
            stage = 0
        } else if (getQuestStage(player, Quests.PRIEST_IN_PERIL) == 14) {
            npc(FaceAnim.HALF_GUILTY, "How does it adventurer? Any luck in finding the key to", "the cell or a way of stopping the vampire yet?")
            stage = 600
        } else if (getQuestStage(player, Quests.PRIEST_IN_PERIL) == 15) {
            player(FaceAnim.HALF_GUILTY, "The key fitted the lock! You're free to leave now!")
            stage = 701
        } else if (getQuestStage(player, Quests.PRIEST_IN_PERIL) == 16) {
            player(FaceAnim.HALF_GUILTY, "I poured the blessed water over the vampires coffin. I", "think that should trap him in there long enough for you", "to escape.")
            stage = 800
        }
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> npc(FaceAnim.HALF_GUILTY, "Oh! You do not appear to be on of those Zamorakians", "who imprisoned me here! Who are you and why are", "you here?").also { stage++ }
            1 -> player(FaceAnim.HALF_GUILTY, "My name's " + player.username + ". King Roald sent me to find", "out what was going on at the temple. I take it you are", "Drezel?").also { stage++ }
            2 -> npc(FaceAnim.HALF_GUILTY, "That is right! Oh, praise be to Saradomin! All is not yet", "lost! I feared that when those Zamorakians attacked this", "place and imprisoned").also { stage++ }
            3 -> npc(FaceAnim.HALF_GUILTY, "me up here, Misthalin would be doomed! If they should", "manage to desecrate the holy river Salve we would be", "defenceless against Morytania!").also { stage++ }
            4 -> player(FaceAnim.HALF_GUILTY, "How is a river a good defence then?").also { stage++ }
            5 -> npc(FaceAnim.HALF_GUILTY, "Well, it is a long tale, and I am not sure we have time!").also { stage++ }
            6 -> options("Tell me anyway.", "You're right, we don't.").also { stage++ }
            7 -> when (buttonId) {
                1 -> player(FaceAnim.HALF_GUILTY, "Tell me anyway. I'd like to know the full facts before", "acting any further.").also { stage++ }
                2 -> player(FaceAnim.HALF_GUILTY, "You're right, we don't.").also { stage = 500 }

            }
            8 -> npc(FaceAnim.HALF_GUILTY, "Ah, Saradomin has granted you wisdom I see. Well, the", "story of the river Salve and of how it protects Misthalin", "is the story of this temple,").also { stage++ }
            9 -> npc(FaceAnim.HALF_GUILTY, "and of the seven warrior priests who died here long ago,", "from whom I am descended. Once long ago Misthalin", "did not have the borders that").also { stage++ }
            10 -> npc(FaceAnim.HALF_GUILTY, "it currently does. This entire area, as far West as", "Varrock itself was under the control of an evil god.", "There was frequent skirmishing").also { stage++ }
            11 -> npc(FaceAnim.HALF_GUILTY, "along the borders, as the brave heroes of Varrock", "fought to keep the evil creatures that now are trapped", "on the eastern side of the River Salve from over", "running").also { stage++ }
            12 -> npc(FaceAnim.HALF_GUILTY, "the human encampments, who worshipped Saradomin.", "Then one day, Saradomin himself appeared to one of", "our mighty heroes, whose name has been forgotten by", "history,").also { stage++ }
            13 -> npc(FaceAnim.HALF_GUILTY, "and told him that should we be able to take the pass that", "this temple now stands in, Saradomin would use his", "power to bless this river, and make it").also { stage++ }
            14 -> npc(FaceAnim.HALF_GUILTY, "impassable to all creatures with evil in their hearts. This", "unknown hero grouped together all of the mightiest", "fighters whose hearts were pure").also { stage++ }
            15 -> npc(FaceAnim.HALF_GUILTY, "that he could find, and the seven of them rode here to", "make a final stand. The enemies swarmed across the", "Salve but they did not yield.").also { stage++ }
            16 -> npc(FaceAnim.HALF_GUILTY, "For ten days and nights they fought, never sleeping,", "never eating, fuelled by their desire to make the world a", "better place for humans to live.").also { stage++ }
            17 -> npc(FaceAnim.HALF_GUILTY, "On the eleventh day they were to be joined by", "reinforcements from a neighbouring encampment, but", "then those reinforcements arrived all they found").also { stage++ }
            18 -> npc(FaceAnim.HALF_GUILTY, "were the bodies of these seven brave but unknown", "heroes, surrounded by the piles of the dead creatures of", "evil that had tried to defeat them.").also { stage++ }
            19 -> npc(FaceAnim.HALF_GUILTY, "The men were saddened at the loss of such pure and", "mighty warriors, yet their sacrifice had not been in", "vain; for the water of the Salve").also { stage++ }
            20 -> npc(FaceAnim.HALF_GUILTY, "had indeed been filled with the power of Saradomin, and", "the evil creatures of Morytania were trapped beyond", "the river banks forever, by their own evil.").also { stage++ }
            21 -> npc(FaceAnim.HALF_GUILTY, "In memory of this brave sacrifice my ancestors built", "this temple so that the land would always be free of the", "evil creatures").also { stage++ }
            22 -> npc(FaceAnim.HALF_GUILTY, "who wish to destroy it, and laid the bodies of those brave", "warriors in tombs of honour below this temple with", "golden gifts on the tombs as marks of respect.").also { stage++ }
            23 -> npc(FaceAnim.HALF_GUILTY, "They also built a statue on the river mouth so that all", "who mighty try and cross into Misthalin from Morytania", "would know that these lands are protected").also { stage++ }
            24 -> npc(FaceAnim.HALF_GUILTY, "by the glory of Saradomin and that good will always", "defeat evil, no matter how the odds are stacked against", "them.").also { stage++ }
            25 -> player(FaceAnim.HALF_GUILTY, "Ok, I can see how the river protects the border, but I", "can't see how anything could affect that from this", "temple.").also { stage++ }
            26 -> npc(FaceAnim.HALF_GUILTY, "Well, as much as it saddens me to say so adventurer,", "Lord Saradomin's presence has not been felt on the", "land for many years now, and even").also { stage++ }
            27 -> npc(FaceAnim.HALF_GUILTY, "though all true Saradominists know that he watches over", "us, his power upon the land is not as strong as it once", "was.").also { stage++ }
            28 -> npc(FaceAnim.HALF_GUILTY, "I fear that should those Zamorakians somehow pollute", "the Salve and desecrate his blessing, his power might not", "be able to stop").also { stage++ }
            29 -> npc(FaceAnim.HALF_GUILTY, "the army of evil that lurks to the east, longing for the", "opportunity to invade and destroy us all!").also { stage++ }
            30 -> npc(FaceAnim.HALF_GUILTY, "So what do you say adventurer? Will you aid me and", "all of Misthalin in foiling this Zamorakian plot?").also { stage++ }
            31 -> options("Yes.", "No.").also { stage = 503 }
            500 -> npc(FaceAnim.HALF_GUILTY, "Well, let's just say if we cannot undo whatever damage", "has been done here the entire land is in grave peril!").also { stage++ }
            501 -> npc(FaceAnim.HALF_GUILTY, "So what do you say adventurer? Will you aid me and", "all of Misthalin in foiling this Zamorakian plot?").also { stage = 31 }
            503 -> when (buttonId) {
                1 -> player(FaceAnim.HALF_GUILTY, "Yes, of course. Any threat to Misthalin must be", "neutralised immediately. So what can I do to help?").also { stage = 506 }
                2 -> player(FaceAnim.HALF_GUILTY, "HA! NO! You can rot in there for all I care you", "stupid priest! All hail mighty Zamorak! Death to puny", "Misthalin!").also { stage = 504 }
            }
            504 -> npc(FaceAnim.HALF_GUILTY, "Oooooh... I knew it was too good to be true... then", "leave me to my fate villain, there's no need to taunt me", "as well as keeping me imprisoned.").also { stage = END_DIALOGUE }


            505 -> end()
            506 -> npc(FaceAnim.HALF_GUILTY, "Well, the immediate problem is that I am trapped in this", "cell. I know that the key to free me is nearby, for none", "of the Zamorakians").also { stage++ }
            507 -> npc(FaceAnim.HALF_GUILTY, "who imprisoned me here were ever gone for long", "periods of time. Should you find the key however, as", "you may have noticed,").also { stage++ }
            508 -> npc(FaceAnim.HALF_GUILTY, "there is a vampire in that coffin over there. I do not", "know how they managed to find it, but it is one of the", "ones that somehow").also { stage++ }
            509 -> npc(FaceAnim.HALF_GUILTY, "survived the battle here all those years ago, and is by", "now quite, quite, mad. It has been trapped on this side", "of the river for centuries,").also { stage++ }
            510 -> npc(FaceAnim.HALF_GUILTY, "and as those fiendish Zamorakians pointed out to me", "with delight, as a descendant of one of those who", "trapped it here, it will recognise").also { stage++ }
            511 -> npc(FaceAnim.HALF_GUILTY, "the smell of my blood should I come anywhere near it.", "It will of course then wake up and kill me, very", "probably slowly and painfully.").also { stage++ }
            512 -> player(FaceAnim.HALF_GUILTY, "Maybe I could kill it somehow then while it is asleep?").also { stage++ }
            513 -> npc(FaceAnim.HALF_GUILTY, "No adventurer, I do not think it would be wise for you", "to wake it at all. As I say, it is little more than a wild", "animal, and must").also { stage++ }
            514 -> npc(FaceAnim.HALF_GUILTY, "be extremely powerful to have survived until today. I", "suspect your best chance would be to incapacitate it", "somehow.").also { stage++ }
            515 -> player(FaceAnim.HALF_GUILTY, "Okay, find the key to your cell, and do something about", "the vampire.").also { stage++ }
            516 -> npc(FaceAnim.HALF_GUILTY, "When you have done both of those I will be able to", "inspect the damage which those Zamorakians have done", "to the purity of the Salve.").also { stage++ }
            517 -> npc(FaceAnim.HALF_GUILTY, "Depending on the severity of the damage, I may", "require further assistance from you in restoring its", "purity.").also { stage++ }
            518 -> player(FaceAnim.HALF_GUILTY, "Okay, well first thing's first; let's get you out of here.").also { stage++ }
            519 -> {
                setQuestStage(player, Quests.PRIEST_IN_PERIL, 14)
                end()
            }
            600 -> player(FaceAnim.HALF_GUILTY, "No, not yet...").also { stage++ }
            601 -> npc(FaceAnim.HALF_GUILTY, "Well don't give up adventurer! That key MUST be", "around here somewhere! I know none of those", "Zamorakians ever got very far from this building!").also { stage++ }
            602 -> player(FaceAnim.HALF_GUILTY, "How do you know that?").also { stage++ }
            603 -> npc(FaceAnim.HALF_GUILTY, "I could hear them laughing about some gullible fool that", "they tricked into killing the guard dog at the", "monument.").also { stage++ }
            604 -> player(FaceAnim.HALF_GUILTY, "Oh.").also { stage++ }
            605 -> npc(FaceAnim.HALF_GUILTY, "Honestly, what kind of idiot would go around killing", "things just because a stranger told them to? What kind", "of oafish, numb-skulled, dim-witted,").also { stage++ }
            606 -> player(FaceAnim.HALF_GUILTY, "Okay, OKAY, I get the picture!").also { stage = END_DIALOGUE }
            607 -> end()
            701 -> npc(FaceAnim.HALF_GUILTY, "Well excellent work adventurer! Unfortunately, as you", "know, I cannot risk waking that vampire in the coffin.").also { stage++ }
            702 -> stage = if (inInventory(player, Items.BUCKET_OF_WATER_2953, 1)) {
                player(FaceAnim.HALF_GUILTY, "I have some water from the Salve. It seems to have", "been desecrated though. Do you think you could bless", "it for me?")
                703
            } else {
                player(FaceAnim.HALF_ASKING, "Do you have any idea about dealing with vampire?")
                730
            }
            703 -> npc(FaceAnim.HALF_GUILTY, "Yes, good thinking adventurer! Give it to me, I will bless", "it!").also { stage++ }
            704 -> {
                end()
                if (!inInventory(player, Items.BUCKET_OF_WATER_2953)) {
                    stage = END_DIALOGUE
                    return true
                }
                if (removeItem(player, Items.BUCKET_OF_WATER_2953)) {
                    sendMessage(player, "The priest blesses the water for you.")
                    addItem(player, Items.BUCKET_OF_WATER_2954, 1)
                    stage = END_DIALOGUE
                }
            }
            730 -> npc(FaceAnim.HALF_GUILTY, "well, the water of the salve should still have enough", "power to work against the vampire despite what those", "Zamorakians might have done to it...").also { stage++ }
            731 -> npc(FaceAnim.HALF_ASKING, "Maybe you should try and get hold of some from", "somewhere?").also { stage = END_DIALOGUE }
            732 -> end()
            800 -> npc(FaceAnim.HALF_GUILTY, "Excellent work adventurer! I am free at last! Let me", "ensure that evil vampire is trapped for good. I will", "meet you down by the monument.").also { stage++ }
            801 -> npc(FaceAnim.HALF_GUILTY, "Look for me down there, I need to assess what damage", "has been done to our holy barrier by those evil", "Zamorakians!").also { stage++ }
            802 -> {
                setQuestStage(player, Quests.PRIEST_IN_PERIL, 17)
                end()
            }
        }
        return true
    }

    override fun getIds(): IntArray = intArrayOf(NPCs.DREZEL_7690)
}
