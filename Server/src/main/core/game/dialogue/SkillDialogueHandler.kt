package core.game.dialogue

import core.api.sendItemZoomOnInterface
import core.api.sendRepositionOnInterface
import core.api.sendString
import core.game.node.entity.player.Player
import core.game.node.item.Item
import core.net.packet.PacketRepository
import core.net.packet.context.ChildPositionContext
import core.net.packet.out.RepositionChild
import core.tools.StringUtils
import shared.consts.Components

/**
 * Handles dialogues related to skills and item interactions.
 */
open class SkillDialogueHandler(val player: Player, val type: SkillDialogue?, vararg data: Any) {
    @Suppress("UNCHECKED_CAST")
    val data: Array<Any> = data as Array<Any>

    fun open() = player.dialogueInterpreter.open(SKILL_DIALOGUE, this)

    fun display() { type?.display(player, this) ?: player.debug("Error! Type is null.") }

    open fun create(amount: Int, index: Int) = Unit

    open fun getAll(index: Int): Int = player.inventory.getAmount(data[0] as Item)

    protected open fun getName(item: Item): String = StringUtils.formatDisplayName(item.name.replace("Unfired", ""))

    private fun item(index: Int): Item = data[index] as Item

    enum class SkillDialogue(
        val interfaceId: Int,
        private val baseButton: Int,
        private val length: Int
    ) {
        ONE_OPTION(Components.SKILL_MULTI1_309, 5, 1) {
            override fun display(player: Player, handler: SkillDialogueHandler) {
                val item = handler.item(0)
                sendString(player, "<br><br><br><br>${item.name}", interfaceId, 6)
                sendItemZoomOnInterface(player, interfaceId, 2, item.id, 160)
                reposition(player, interfaceId, ONE_OPTION_POSITIONS)
            }

            override fun getAmount(handler: SkillDialogueHandler, buttonId: Int): Int = when (buttonId) {
                5 -> 1
                4 -> 5
                3 -> -1
                else -> handler.getAll(getIndex(handler, buttonId))
            }
        },

        MAKE_SET_ONE_OPTION(Components.SKILL_MULTI1_SMALL_582, 4, 1) {
            override fun display(player: Player, handler: SkillDialogueHandler) {
                val item = handler.item(0)
                sendItemZoomOnInterface(player, interfaceId, 2, item.id, 160)
                sendString(player, "<br><br><br><br>${item.name}", interfaceId, 5)
                reposition(player, interfaceId, MAKE_SET_POSITIONS)
            }

            override fun getAmount(handler: SkillDialogueHandler, buttonId: Int): Int = when (buttonId) {
                5, 4 -> 1
                3 -> 5
                else -> 10
            }
        },

        TWO_OPTION(Components.SKILL_MAKE_303, 7, 2) {
            override fun display(player: Player, handler: SkillDialogueHandler) {
                reposition(player, interfaceId, STANDARD_POSITIONS)
                for (i in handler.data.indices) {
                    val item = handler.item(i)
                    sendString(player, "<br><br><br><br><br>${handler.getName(item)}", interfaceId, if (i == 0) 7 else 11)
                    player.packetDispatch.sendItemZoomOnInterface(item.id, 180, interfaceId, 2 + i)
                }
            }

            override fun getIndex(handler: SkillDialogueHandler?, buttonId: Int): Int = when (buttonId) {
                in 3..6 -> 0
                else -> 1
            }

            override fun getAmount(handler: SkillDialogueHandler, buttonId: Int): Int = when (buttonId) {
                6, 10 -> 1
                5, 9 -> 5
                4, 8 -> 10
                3, 7 -> -1
                else -> 1
            }
        },

        THREE_OPTION(Components.SKILL_MAKE_304, 8, 3) {
            override fun display(player: Player, handler: SkillDialogueHandler) {
                reposition(player, interfaceId, THREE_OPTION_POSITIONS)
                for (i in 0..2) {
                    val item = handler.item(i)
                    player.packetDispatch.sendItemZoomOnInterface(item.id, 170, interfaceId, 2 + i)
                    sendString(player, "<br><br><br><br>${item.name}", interfaceId, 8 + i * 4)
                }
            }

            override fun getIndex(handler: SkillDialogueHandler?, buttonId: Int): Int = when (buttonId) {
                in 4..7 -> 0
                in 8..11 -> 1
                in 12..15 -> 2
                else -> 1
            }

            override fun getAmount(handler: SkillDialogueHandler, buttonId: Int): Int = when (buttonId) {
                7, 11, 15 -> 1
                6, 10, 14 -> 5
                5, 9, 13 -> 10
                4, 8, 12 -> -1
                else -> 1
            }
        },

        FOUR_OPTION(Components.SKILL_MAKE_305, 9, 4) {
            override fun display(player: Player, handler: SkillDialogueHandler) {
                reposition(player, interfaceId, STANDARD_POSITIONS)
                for (i in 0..3) {
                    val item = handler.item(i)
                    player.packetDispatch.sendItemZoomOnInterface(item.id, 160, interfaceId, 2 + i)
                    sendString(player, "<br><br><br><br>${item.name}", interfaceId, 9 + i * 4)
                }
            }

            override fun getIndex(handler: SkillDialogueHandler?, buttonId: Int): Int = when (buttonId) {
                in 5..8 -> 0
                in 9..12 -> 1
                in 13..16 -> 2
                in 17..20 -> 3
                else -> 0
            }

            override fun getAmount(handler: SkillDialogueHandler, buttonId: Int): Int = when (buttonId) {
                8, 12, 16, 20 -> 1
                7, 11, 15, 19 -> 5
                6, 10, 14, 18 -> 10
                5, 9, 13, 17 -> -1
                else -> 1
            }
        },

        FIVE_OPTION(Components.SKILL_MAKE_306, 7, 5) {
            override fun display(player: Player, handler: SkillDialogueHandler) {
                player.interfaceManager.openChatbox(interfaceId)
                for (i in handler.data.indices) {
                    val item = handler.item(i)
                    sendString(player, "<br><br><br><br>${handler.getName(item)}", interfaceId, 10 + 4 * i)
                    player.packetDispatch.sendItemZoomOnInterface(item.id, 160, interfaceId, 2 + i)
                    val (x, y) = POSITIONS[i]
                    PacketRepository.send(
                        RepositionChild::class.java,
                        ChildPositionContext(player, interfaceId, 2 + i, x, y)
                    )
                }
                reposition(player, interfaceId, STANDARD_POSITIONS)
            }

            override fun getIndex(handler: SkillDialogueHandler?, buttonId: Int): Int = when (buttonId) {
                in 6..9 -> 0
                in 10..13 -> 1
                in 14..17 -> 2
                in 18..21 -> 3
                in 22..25 -> 4
                else -> 0
            }

            override fun getAmount(handler: SkillDialogueHandler, buttonId: Int): Int = when (buttonId) {
                9, 13, 17, 21, 25 -> 1
                8, 12, 16, 20, 24 -> 5
                7, 11, 15, 19, 23 -> 10
                6, 10, 14, 18, 22 -> -1
                else -> 1
            }
        };

        open fun display(player: Player, handler: SkillDialogueHandler) = Unit

        open fun getAmount(handler: SkillDialogueHandler, buttonId: Int): Int {
            for (k in 0..3) {
                for (i in 0 until length) {
                    if (baseButton - k + 4 * i == buttonId) {
                        return 6
                    }
                }
            }
            return -1
        }

        open fun getIndex(handler: SkillDialogueHandler?, buttonId: Int): Int {
            var index = 0
            for (k in 0..3) {
                for (i in 1 until length) {
                    val value = baseButton + k + 4 * i
                    if (value == buttonId) return index + 1
                    if (value <= buttonId) index++
                }
                index = 0
            }
            return index
        }

        companion object {
            private val STANDARD_POSITIONS = intArrayOf(12, 15, 431, 15)
            private val THREE_OPTION_POSITIONS = intArrayOf(12, 13, 431, 13)
            private val ONE_OPTION_POSITIONS = intArrayOf(12, 15, 431, 15, 210, 32, 60, 35, 60, 35, 60, 35, 60, 35, 8, 12)
            private val MAKE_SET_POSITIONS = intArrayOf(12, 15, 431, 15, 207, 23, 60, 35, 60, 35, 60, 35, 8, 12)
            private val POSITIONS = arrayOf(10 to 30, 117 to 10, 217 to 20, 317 to 15, 408 to 15)

            private fun reposition(player: Player, interfaceId: Int, positions: IntArray) {
                for (i in positions.indices step 2) {
                    sendRepositionOnInterface(player, interfaceId, i / 2, positions[i], positions[i + 1])
                }
            }
        }
    }

    companion object {
        const val SKILL_DIALOGUE = 3 shl 16
    }
}
