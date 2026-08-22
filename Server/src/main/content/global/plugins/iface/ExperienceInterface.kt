package content.global.plugins.iface

import core.api.*
import core.game.interaction.InterfaceListener
import core.game.node.entity.player.Player
import core.game.node.entity.skill.Skills
import core.plugin.Plugin
import core.tools.Log
import shared.consts.Components
import shared.consts.Quests
import shared.consts.Sounds

/**
 * Represents the experience interface.
 * @author Ceikry
 */
class ExperienceInterface : InterfaceListener {

    override fun defineInterfaceListeners() {

        on(COMPONENT_ID) { player, _, _, buttonID, _, _ ->

            if (buttonID == 2) {
                val confirmedSkill = getAttribute(
                    player,
                    "exp_interface:skill",
                    -1
                )

                if (confirmedSkill == -1) {
                    sendMessage(player, "You must first select a skill.")
                } else {
                    removeAttribute(player, "exp_interface:skill")

                    when (confirmedSkill) {
                        Skills.HERBLORE -> {
                            if (!isQuestComplete(player, Quests.DRUIDIC_RITUAL)) {
                                sendMessage(player, "You need to have completed ${Quests.DRUIDIC_RITUAL} for this.")
                                return@on true
                            }
                        }

                        Skills.RUNECRAFTING -> {
                            if (!isQuestComplete(player, Quests.RUNE_MYSTERIES)) {
                                sendMessage(player, "You need to have completed Rune Mysteries for this.")
                                return@on true
                            }
                        }

                        Skills.SUMMONING -> {
                            if (!isQuestComplete(player, Quests.WOLF_WHISTLE)) {
                                sendMessage(player, "You need to have completed ${Quests.WOLF_WHISTLE} for this.")
                                return@on true
                            }
                        }
                    }

                    val caller = player.attributes["caller"]
                        ?: return@on true

                    if (caller is Plugin<*>) {
                        caller.handleSelectionCallback(confirmedSkill, player)
                    } else {
                        (caller as (Int, Player) -> Unit).invoke(confirmedSkill, player)
                    }

                    playAudio(player, SOUND)
                    closeInterface(player)
                }
            } else {
                val skill = when (buttonID) {
                    29 -> Skills.ATTACK
                    30 -> Skills.STRENGTH
                    31 -> Skills.DEFENCE
                    32 -> Skills.RANGE
                    35 -> Skills.MAGIC
                    39 -> Skills.CRAFTING
                    34 -> Skills.HITPOINTS
                    33 -> Skills.PRAYER
                    36 -> Skills.AGILITY
                    37 -> Skills.HERBLORE
                    38 -> Skills.THIEVING
                    43 -> Skills.FISHING
                    47 -> Skills.RUNECRAFTING
                    48 -> Skills.SLAYER
                    50 -> Skills.FARMING
                    41 -> Skills.MINING
                    42 -> Skills.SMITHING
                    49 -> Skills.HUNTER
                    52 -> Skills.SUMMONING
                    45 -> Skills.COOKING
                    44 -> Skills.FIREMAKING
                    46 -> Skills.WOODCUTTING
                    40 -> Skills.FLETCHING
                    51 -> Skills.CONSTRUCTION

                    else -> {
                        log(
                            this::class.java,
                            Log.WARN,
                            "EXP_INTERFACE: Invalid SKILL CHOICE BUTTON: $buttonID"
                        )
                        Skills.SLAYER
                    }
                }

                setAttribute(player, "exp_interface:skill", skill)
            }

            return@on true
        }
    }

    companion object {
        private const val SOUND = Sounds.TBCU_FINDGEM_1270
        const val COMPONENT_ID = Components.STATS_ADVANCEMENT_134
    }
}