package content.global.plugins.`object`

import content.data.Quests

import core.api.*
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.Node
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.SpellBookManager.SpellBook
import core.game.node.entity.skill.Skills
import shared.consts.Animations
import shared.consts.Scenery
import shared.consts.Sounds

class MagicAltarOptionPlugin : InteractionListener {

    override fun defineListeners() {

        /*
         * Handles swapping the spellbooks on altar.
         */

        on(intArrayOf(ANCIENT_ALTAR, LUNAR_ALTAR), IntType.SCENERY, "pray-at", "pray") { player, node ->
            val altar = node as? Node ?: return@on true
            if (meetsRequirements(player, altar)) {
                swapSpellBook(player, altar)
            }
            return@on true
        }
    }

    private fun meetsRequirements(player: Player, altar: Node): Boolean {
        val isAncient = altar.id == ANCIENT_ALTAR
        val requiredQuest = if (isAncient) Quests.DESERT_TREASURE.questName else Quests.LUNAR_DIPLOMACY.questName
        val level = if (isAncient) 50 else 65

        if (!hasRequirement(player, requiredQuest)) return false
        if (!hasLevelStat(player, Skills.MAGIC, level)) {
            sendMessage(player, "You need a Magic level of at least $level in order to do this.")
            return false
        }

        return true
    }

    private fun swapSpellBook(player: Player, altar: Node) {
        lock(player, 3)
        try {
            playAudio(player, Sounds.PRAYER_RECHARGE_2674)
            animate(player, Animations.HUMAN_PRAY_645)

            val isAncient = altar.id == ANCIENT_ALTAR

            if (isAncient) {
                player.skills.decrementPrayerPoints(player.skills.prayerPoints)
            }

            val currentBook = SpellBook.forInterface(player.spellBookManager.spellBook) ?: SpellBook.MODERN

            if (currentBook == if (isAncient) SpellBook.ANCIENT else SpellBook.LUNAR) {
                val message = if (isAncient) {
                    "You feel a strange drain upon your memory..."
                } else {
                    "Modern spells activated!"
                }
                sendMessage(player, message)
                player.spellBookManager.setSpellBook(SpellBook.MODERN)
            } else {
                val message = if (isAncient) {
                    "You feel a strange wisdom fill your mind..."
                } else {
                    "Lunar spells activated!"
                }
                sendMessage(player, message)
                player.spellBookManager.setSpellBook(if (isAncient) SpellBook.ANCIENT else SpellBook.LUNAR)
            }

            player.spellBookManager.update(player)

        } finally {
            player.unlock()
        }
    }

    companion object {
        private const val ANCIENT_ALTAR = Scenery.ALTAR_6552
        private const val LUNAR_ALTAR = Scenery.ALTAR_17010
    }
}