package content.region.misthalin.dig_site.quest.itexam.plugin

import core.api.*
import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.node.item.Item
import core.tools.START_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs
import shared.consts.Quests

class ArchaeologicalExpertPlugin : InteractionListener {

    companion object {
        val ITEMS = intArrayOf(
            Items.ANCIENT_STAFF_4675,
            Items.UNIDENTIFIED_LIQUID_702,
            Items.NITROGLYCERIN_703,
            Items.CHEMICAL_POWDER_700,
            Items.AMMONIUM_NITRATE_701,
            Items.NUGGETS_680,
            Items.NEEDLE_1733,
            Items.ROTTEN_APPLE_1984,
            Items.BROKEN_GLASS_1469,
            Items.BROKEN_ARROW_687,
            Items.PANNING_TRAY_677,
            Items.BONES_526,
            Items.BUTTONS_688,
            Items.CRACKED_SAMPLE_674,
            Items.OLD_TOOTH_695,
            Items.RUSTY_SWORD_686,
            Items.BROKEN_STAFF_689,
            Items.BROKEN_ARMOUR_698,
            Items.DAMAGED_ARMOUR_697,
            Items.CERAMIC_REMAINS_694,
            Items.BELT_BUCKLE_684,
            Items.ANIMAL_SKULL_671,
            Items.SPECIAL_CUP_672,
            Items.TEDDY_673,
            Items.STONE_TABLET_699
        )
    }

    override fun defineListeners() {
        onUseAnyWith(IntType.NPC, NPCs.ARCHAEOLOGICAL_EXPERT_619) { player, used, with ->
            openDialogue(player, ArchaeologicalExpertListenerDialogueFile(used.id), with as NPC)
            return@onUseAnyWith false
        }
    }
}

/**
 * Represents Archaeological Expert dialogue.
 */
class ArchaeologicalExpertListenerDialogueFile(val it: Int, ) : DialogueFile() {

    companion object {
        fun replaceAll(player: Player, originalItem: Int, newItem: Int, ) {
            for (a in 0..amountInInventory(player, originalItem)) {
                if (inInventory(player, originalItem)) {
                    replaceSlot(
                        player,
                        player.inventory.getSlot(Item(originalItem)),
                        Item(newItem),
                    )
                }
            }
        }
    }

    override fun handle(componentID: Int, buttonID: Int) {
        when (stage) {
            START_DIALOGUE -> {
                when (it) {
                    Items.UNIDENTIFIED_LIQUID_702 -> {
                        player(FaceAnim.THINKING, "Do you know what this is?")
                        stage = 1
                    }

                    Items.NITROGLYCERIN_703 -> {
                        player(FaceAnim.THINKING, "Can you tell me any more about this?")
                        stage = 10
                    }

                    Items.CHEMICAL_POWDER_700 -> {
                        player(FaceAnim.THINKING, "Do you know what this powder is?")
                        stage = 20
                    }

                    Items.ANCIENT_TALISMAN_681 -> {
                        npcl(FaceAnim.FRIENDLY, "Unusual... This object doesn't appear right...")
                        stage = 30
                    }

                    Items.STONE_TABLET_699 -> {
                        playerl(FaceAnim.FRIENDLY, "I found this in a hidden cavern beneath the site.")
                        stage = 50
                    }

                    else -> {
                        npcl(FaceAnim.FRIENDLY, "I don't think that has any archaeological significance.")
                        stage = 100
                    }
                }
            }

            1 -> {
                npcl(FaceAnim.WORRIED, "Where did you get this?")
                stage = 2
            }

            2 -> {
                player("From one of the barrels at the digsite.")
                stage = 3
            }

            3 -> {
                npcl(
                    FaceAnim.WORRIED,
                    "This is a VERY dangerous liquid called nitroglycerin. Be careful how you handle it. Don't drop it or it will explode!",
                )
                stage = 4
            }

            4 -> {
                replaceAll(player!!, Items.UNIDENTIFIED_LIQUID_702, Items.NITROGLYCERIN_703)
                endFile()
            }

            10 -> {
                npcl(
                    FaceAnim.WORRIED,
                    "Nitroglycerin! This is a dangerous substance. This is normally mixed with other chemicals to produce a potent compound.",
                )
                stage = 11
            }

            11 -> {
                npcl(FaceAnim.WORRIED, "Be sure not to drop it! That stuff is highly volatile...")
                stage = 12
            }

            12 -> endFile()

            20 -> {
                npcl(
                    FaceAnim.WORRIED,
                    "Really, you do find the most unusual items. I know what this is - it's a strong chemical called ammonium nitrate. Why you want this I'll never know...",
                )
                stage = 21
            }

            21 -> {
                replaceAll(player!!, Items.CHEMICAL_POWDER_700, Items.AMMONIUM_NITRATE_701)
                endFile()
            }

            30 -> {
                npcl(FaceAnim.FRIENDLY, "Hmmm...")
                stage = 31
            }

            31 -> {
                npcl(FaceAnim.FRIENDLY, "I wonder... Let me check my guide... Could it be? Surely not!")
                stage = 32
            }

            32 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "From the markings on it, it seems to be a ceremonial ornament to a god named...",
                )
                stage = 33
            }

            33 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "...Zaros? I haven't heard much about him before. This is a great discovery; we know very little of the ancient gods that people worshipped.",
                )
                stage = 34
            }

            34 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "There is some strange writing embossed upon it - it says, 'Zaros will return and wreak his vengeance upon Zamorak the pretender.'",
                )
                stage = 35
            }

            35 -> {
                npcl(FaceAnim.FRIENDLY, "I wonder what it means by that. Some silly superstition, probably.")
                stage = 36
            }

            36 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "Still, I wonder what this is doing around here. I'll tell you what; as you have found this, I will allow you to use the private dig shafts.",
                )
                stage = 37
            }

            37 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "You obviously have a keen eye. Take this letter and give it to one of the workmen, and they will allow you to use them.",
                )
                stage = 38
            }

            38 -> {
                if (removeItem(player!!, Items.ANCIENT_TALISMAN_681)) {
                    addItemOrDrop(player!!, Items.INVITATION_LETTER_696)
                }
                if (getQuestStage(player!!, Quests.THE_DIG_SITE) == 6) {
                    setQuestStage(player!!, Quests.THE_DIG_SITE, 7)
                }
                endFile()
            }

            50 -> {
                npcl(FaceAnim.FRIENDLY, "Incredible!")
                stage = 51
            }

            51 -> {
                playerl(FaceAnim.FRIENDLY, "There is an altar down there. The place is crawling with skeletons!")
                stage = 52
            }

            52 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "Yuck! This is an amazing discovery! All this while we were convinced that no other race had lived here.",
                )
                stage = 53
            }

            53 -> {
                npcl(
                    "It seems the followers of Saradomin have tried to cover up the evidence of this Zaros altar. This whole city must have been built over it!",
                )
                stage = 54
            }

            54 -> {
                npcl(
                    "Thanks for your help; your sharp eyes have spotted what many have missed. Here, take this gold as your reward.",
                )
                stage = 55
            }

            55 -> {
                sendItemDialogue(player!!,Items.GOLD_BAR_2357, "The expert gives you two gold bars as payment.")
                stage = 56
            }

            56 -> {
                if (removeItem(player!!, Items.STONE_TABLET_699)) {
                    if (getQuestStage(player!!, Quests.THE_DIG_SITE) == 11) {
                        finishQuest(player!!, Quests.THE_DIG_SITE)
                    }
                }
                endFile()
            }

            100 -> endFile()
        }
    }
}