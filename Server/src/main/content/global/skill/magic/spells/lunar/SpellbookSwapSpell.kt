package content.global.skill.magic.spells.lunar

import content.data.Quests

import content.global.skill.magic.spells.LunarSpells
import core.api.*
import core.game.component.CloseEvent
import core.game.component.Component
import core.game.dialogue.DialogueFile
import core.game.node.Node
import core.game.node.entity.Entity
import core.game.node.entity.combat.equipment.WeaponInterface
import core.game.node.entity.combat.spell.MagicSpell
import core.game.node.entity.combat.spell.Runes
import core.game.node.entity.combat.spell.SpellType
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.SpellBookManager.SpellBook
import core.game.node.item.Item
import core.game.system.task.Pulse
import core.game.world.GameWorld.Pulser
import core.game.world.update.flag.context.Animation
import core.game.world.update.flag.context.Graphics
import core.plugin.Initializable
import core.plugin.Plugin
import core.tools.RandomFunction
import shared.consts.Sounds

@Initializable
class SpellbookSwapSpell : MagicSpell(SpellBook.LUNAR, 96, 130.0, null, null, null, arrayOf(Item(Runes.LAW_RUNE.id, 1), Item(Runes.COSMIC_RUNE.id, 2), Item(Runes.ASTRAL_RUNE.id, 3))) {

    override fun newInstance(arg: SpellType?): Plugin<SpellType?> {
        SpellBook.LUNAR.register(LunarSpells.SPELLBOOK_SWAP, this)
        return this
    }

    override fun cast(entity: Entity, target: Node): Boolean {
        val player = entity as? Player ?: return false
        if (!super.meetsRequirements(player, true, true)) return false

        player.lock(5)

        visualize(player, Animation(6299), Graphics(shared.consts.Graphics.DREAM_SPELLBOOK_SPOTANIM_1062))
        playAudio(player, Sounds.LUNAR_CHANGE_SPELLBOOK_3613)

        onCastDuringSwap(player)

        player.properties.autocastSpell?.let {
            player.properties.autocastSpell = null
            val wif = player.getExtension<WeaponInterface>(WeaponInterface::class.java)
            wif?.selectAutoSpell(-1, true)
            wif?.openAutocastSelect()
        }

        if (!isQuestComplete(player, Quests.DESERT_TREASURE.questName)) {
            applySpellbook(player, SpellBook.MODERN)
        } else {
            openSpellbookDialogue(player)
        }

        return true
    }

    private fun openSpellbookDialogue(player: Player) {
        openDialogue(player, object : DialogueFile() {
            override fun handle(componentID: Int, buttonID: Int) {
                player?.let { p ->
                    when (stage) {
                        0 -> {
                            sendOptions(p, "Select a Spellbook:", "Regular spellbook", "Ancient spellbook")
                            stage = 1
                        }
                        1 -> {
                            val option = when (buttonID) {
                                1 -> SpellBook.MODERN
                                2 -> SpellBook.ANCIENT
                                else -> return
                            }
                            applySpellbook(p, option)
                            end()
                        }
                    }
                }
            }
        })
    }

    private fun applySpellbook(player: Player, book: SpellBook) {
        player.spellBookManager.setSpellBook(book)
        val comp = Component(book.interfaceId)
        player.interfaceManager.openTab(comp)

        comp.closeEvent = CloseEvent { p, _ ->
            val tabIndex = comp.definition?.tabIndex ?: -1
            val currentTab = p.interfaceManager.currentTabIndex

            if (tabIndex != 6 && currentTab != 6) {
                if (p.getAttribute("spell:swap", 0) != 0) {
                    removeTemporarySwap(p)
                }
            }

            return@CloseEvent true
        }

        val wif = player.getExtension<WeaponInterface>(WeaponInterface::class.java)
        wif?.selectAutoSpell(-1, true)
        startTemporarySwap(player)
    }

    private fun startTemporarySwap(player: Player) {
        val id = RandomFunction.random(1, 500_000)
        setAttribute(player, "spell:swap", id)
        setAttribute(player, "spell:swapStart", System.currentTimeMillis())
        sendMessage(player, "You have 2 minutes before your spellbook changes back to the Lunar Spellbook!")

        Pulser.submit(object : Pulse(100, player) {
            override fun pulse(): Boolean {
                val swapId = player.getAttribute("spell:swap", 0)
                if (swapId != id) return true

                val elapsed = player.getAttribute("spell:swapStart", 0L)
                if (elapsed != 0L && System.currentTimeMillis() - elapsed >= 120_000) {
                    removeTemporarySwap(player)
                    return true
                }

                return false
            }
        })
    }

    companion object {
        fun removeTemporarySwap(player: Player) {
            removeAttribute(player, "spell:swap")
            removeAttribute(player, "spell:swapStart")
            player.spellBookManager.setSpellBook(SpellBook.LUNAR)
            player.interfaceManager.openTab(Component(SpellBook.LUNAR.interfaceId))
            val wif = player.getExtension<WeaponInterface>(WeaponInterface::class.java)
            wif?.selectAutoSpell(-1, true)
        }

        fun onCastDuringSwap(player: Player) {
            if (player.getAttribute("spell:swap", 0) != 0) {
                removeTemporarySwap(player)
            }
        }
    }
}
